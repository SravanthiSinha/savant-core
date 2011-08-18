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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.savantbuild.BuildException;
import org.savantbuild.dep.graph.ArtifactGraph;
import org.savantbuild.dep.graph.ArtifactLink;
import org.savantbuild.dep.graph.GraphBuilder;
import org.savantbuild.dep.graph.GraphLink;
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

/**
 * <p>
 * This is used to iterate over dependency lists (artifacts and artifact groups) and deletes the artifacts
 * that have been published locally.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultDependencyDeleter implements DependencyDeleter {
  private final Output output;

  @Inject
  public DefaultDependencyDeleter(Output output) {
    this.output = output;
  }

  @Override
  public void delete(Artifact artifact, Workflow workflow, boolean transitive, DependencyListener... listeners) {
    Dependencies deps = new Dependencies();
    deps.getArtifactGroups().put("run", new ArtifactGroup("run"));
    deps.getArtifactGroups().get("run").getArtifacts().add(artifact);
    delete(deps, workflow, transitive, listeners);
  }

  @Override
  public void delete(Dependencies dependencies, Workflow workflow, boolean transitive, DependencyListener... listeners) {
    output.println(Level.DEBUG, "Running dependency deleter");
    WorkflowHandler handler = WorkflowHandler.build(workflow, output);
    ArtifactGraph graph = dependencies.getGraph();
    if (graph == null) {
      GraphBuilder builder = new GraphBuilder(output, dependencies, handler, transitive);
      graph = builder.buildGraph(new ResolutionContext());
    }

    // The graph contains everything right now. We should be able to dump the graph and remove
    // everything.
    ErrorList errors = new ErrorList();
    output.println(Level.DEBUG, "Deleting artifacts");
    deleteArtifacts(graph, handler, errors, listeners);
    if (!errors.isEmpty()) {
      throw new BuildException("Errors found while deleting", errors);
    }
  }

  /**
   * Handles the deleting of the artifacts.
   *
   * @param graph     The artifact graph for the dependencies.
   * @param handler   The workflow to use.
   * @param errors    The error list to store any errors that occur.
   * @param listeners The listeners.
   */
  protected void deleteArtifacts(ArtifactGraph graph, WorkflowHandler handler, ErrorList errors, DependencyListener... listeners) {
    Set<ArtifactID> ids = graph.getAllGraphNodesValues();
    for (ArtifactID id : ids) {
      // Find all the versions of the artifact that this project uses and delete them all
      Set<String> versions = new HashSet<String>();
      List<GraphLink<ArtifactID, ArtifactLink>> inboundLinks = graph.getGraphNode(id).getInboundLinksList();
      for (GraphLink<ArtifactID, ArtifactLink> inboundLink : inboundLinks) {
        versions.add(inboundLink.value.getDependencyVersion());
      }

      for (String version : versions) {
        Artifact artifact = new Artifact(id.getGroup(), id.getProject(), id.getName(), version, id.getType());

        // Let the fetchWorkflow handle the resolution from both the local cache and
        // the process objects.
        boolean deleted = false;
        try {
          // Do compat and not and just clean up everything!
          deleted = handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactMetaDataFile());
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactMetaDataFile() + ".md5");
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactNegativeMetaDataFile());
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactNegativeMetaDataFile() + ".md5");
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactFile());
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactFile() + ".md5");
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactSourceFile());
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactSourceFile() + ".md5");
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactSourceFile() + ".neg");
          deleted |= handler.getPublishWorkflowHandler().delete(artifact, artifact.getArtifactSourceFile() + ".neg.md5");
        } catch (BuildException sbe) {
          errors.addError("Error while cleaning artifact [" + artifact + "] - " + sbe.toString());
          output.println(Level.DEBUG, sbe);
        }

        if (deleted) {
          for (DependencyListener listener : listeners) {
            listener.artifactCleaned(artifact);
          }

          output.info("Cleaned out artifact [" + artifact.toString() + "]");
        }
      }
    }
  }
}
