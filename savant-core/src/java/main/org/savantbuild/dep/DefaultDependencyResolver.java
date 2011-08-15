/*
 * Copyright (c) 2008, Inversoft, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.dep;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.savantbuild.BuildException;
import org.savantbuild.dep.graph.ArtifactGraph;
import org.savantbuild.dep.graph.ArtifactLink;
import org.savantbuild.dep.graph.GraphBuilder;
import org.savantbuild.dep.graph.GraphLink;
import org.savantbuild.dep.graph.GraphNode;
import org.savantbuild.dep.version.CompatibilityVerifier;
import org.savantbuild.dep.workflow.FetchWorkflowHandler;
import org.savantbuild.dep.workflow.PublishWorkflowHandler;
import org.savantbuild.dep.workflow.WorkflowHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.ArtifactID;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Workflow;
import org.savantbuild.run.output.Level;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.ErrorList;

import com.google.inject.Inject;
import static org.savantbuild.util.CollectionTools.*;

/**
 * <p>
 * This is used to iterate over dependency lists (artifacts
 * and artifact groups) and call out to interested listeners.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultDependencyResolver implements DependencyResolver {
  private final Output output;

  @Inject
  public DefaultDependencyResolver(Output output) {
    this.output = output;
  }

  @Override
  public Map<Artifact, File> resolve(Artifact artifact, Workflow workflow, Set<String> artifactGroupTypes,
                                     boolean transitive, DependencyListener... listeners) {
    Dependencies deps = new Dependencies();
    deps.getArtifactGroups().put("run", new ArtifactGroup("run"));
    deps.getArtifactGroups().get("run").getArtifacts().add(artifact);
    return resolve(deps, workflow, artifactGroupTypes, transitive, listeners);
  }

  @Override
  public Map<Artifact, File> resolve(Dependencies dependencies, Workflow workflow, Set<String> artifactGroupTypes,
                                     boolean transitive, DependencyListener... listeners) {
    // If there are no types, just assume they want everything
    if (artifactGroupTypes == null || artifactGroupTypes.size() == 0) {
      artifactGroupTypes = new HashSet<String>();
    }

    WorkflowHandler handler = WorkflowHandler.build(workflow, output);

    output.println(Level.DEBUG, "Running dependency mediator");
    ResolutionContext resolutionContext = new ResolutionContext();
    ArtifactGraph graph = dependencies.getGraph();
    if (graph == null) {
      GraphBuilder builder = new GraphBuilder(output, dependencies, handler, transitive);
      graph = builder.buildGraph(resolutionContext);
    }

    CompatibilityVerifier verifier = new CompatibilityVerifier(output);
    ErrorList errors = verifier.verifyCompatibility(dependencies, graph, artifactGroupTypes);
    if (errors != null && !errors.isEmpty()) {
      throw new BuildException("Artifact compatibility error", errors);
    }

    // Perform depth first traversal and download
    output.println(Level.DEBUG, "Fetching artifacts");
    errors = new ErrorList();
    Map<Artifact, File> results = new HashMap<Artifact, File>();
    Set<GraphNode<ArtifactID, ArtifactLink>> nodes = graph.getAllGraphNodes();
    for (GraphNode<ArtifactID, ArtifactLink> node : nodes) {
      ArtifactID id = node.getValue();

      // Skip the root node because it is the project and therefore not resolvable
      if (id.equals(graph.getRoot().getId())) {
        continue;
      }

      // Determine the version of the artifact
      ArtifactLink bestLink = null;
      List<GraphLink<ArtifactID, ArtifactLink>> links = node.getInboundLinksList();
      for (GraphLink<ArtifactID, ArtifactLink> link : links) {
        // If this version is in a group that we shouldn't use, skip it
        if (artifactGroupTypes.size() > 0 && !artifactGroupTypes.contains(link.value.getType())) {
          continue;
        }

        if (bestLink == null) {
          bestLink = link.value;
        } else if (!bestLink.getDependencyVersion().equals(link.value.getDependencyVersion())) {
          throw new BuildException("Savant was unable to determine the single version of the artifact [" + id +
            "] and therefore could not resolve that artifact. This is generally an internal Savant bug and " +
            "should be reported and fixed.");
        }
      }

      // If we found a suitable version, resolve it
      if (bestLink != null) {
        Artifact artifact = bestLink.toArtifact(id);
        File file = resolveSingleArtifact(handler, artifact, errors, resolutionContext, listeners);
        if (file != null) {
          results.put(artifact, file);
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new BuildException("Savant encountered an error(s) while attempting to resolve the dependencies.", errors);
    }

    // Handle all the negatives
    Map<Artifact, Set<String>> missingItems = resolutionContext.getMissingItems();
    PublishWorkflowHandler pw = handler.getPublishWorkflowHandler();
    for (Artifact artifact : missingItems.keySet()) {
      Set<String> items = missingItems.get(artifact);
      for (String item : items) {
        if (item.equals("AMD_FILE")) {
          pw.publishNegativeMetaData(artifact);
        } else {
          pw.publishNegative(artifact, item);
        }
      }
    }

    return results;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Dependencies dependencies(Artifact artifact, Workflow workflow) {
    Dependencies deps = new Dependencies();
    deps.getArtifactGroups().put("run", new ArtifactGroup("run"));
    deps.getArtifactGroups().get("run").getArtifacts().add(artifact);
    resolve(deps, workflow, set("run"), true);
    return deps.getGraph().getDependencies(artifact);
  }


  /**
   * Handles the fetching of a single artifact.
   *
   * @param handler           The workflow handler.
   * @param artifact          The artifact to fetch and store
   * @param errors            The ErrorList to add any errors to.
   * @param resolutionContext The resolution context.
   * @param listeners         The listeners.
   * @return The file for the artifact in the local cache (if found and cached).
   */
  protected File resolveSingleArtifact(WorkflowHandler handler, Artifact artifact, ErrorList errors,
                                       ResolutionContext resolutionContext, DependencyListener... listeners) {
    FetchWorkflowHandler fw = handler.getFetchWorkflowHandler();
    PublishWorkflowHandler pw = handler.getPublishWorkflowHandler();

    // Let the fetchWorkflow handle the resolution from both the local cache and
    // the process objects.
    File file = fw.fetchItem(artifact, artifact.getArtifactFile(), pw, resolutionContext);
    if (file == null) {
      errors.addError("Unable to locate dependency [" + artifact.toString() + "]");
      return null;
    }

    // Fetch the source JAR for the artifact, if it exists. If it doesn't that's okay.
    fw.fetchItem(artifact, artifact.getArtifactSourceFile(), pw, resolutionContext);

    output.println(Level.DEBUG, "Done resolving artifact [" + artifact + "]");

    for (DependencyListener listener : listeners) {
      listener.artifactFound(file, artifact);
    }

    return file;
  }
}
