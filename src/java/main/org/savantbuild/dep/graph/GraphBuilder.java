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
package org.savantbuild.dep.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.savantbuild.BuildException;
import org.savantbuild.dep.ResolutionContext;
import org.savantbuild.dep.version.ArtifactVersionTools;
import org.savantbuild.dep.workflow.WorkflowHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.ArtifactID;
import org.savantbuild.domain.ArtifactMetaData;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.run.output.Level;
import org.savantbuild.run.output.Output;

/**
 * <p>
 * This class is used to build out an {@link Graph} that contains a map of all the dependencies
 * between artifacts. This is done by traversing the transitive dependencies for each artifact
 * in a {@link Dependencies} object.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class GraphBuilder {
  private Output output;
  private Dependencies dependencies;
  private WorkflowHandler workflowHandler;
  private boolean transitive = true;

  /**
   * Constructs a new graph builder.
   *
   * @param output          The output.
   * @param dependencies    The dependencies that will be used to build the dependency graph. This will be used to
   *                        resolve all transitive dependencies.
   * @param workflowHandler The workflowHandler used to fetch and publish the dependencies of artifacts during
   *                        transitive graph building.
   * @param transitive      Determines if when building the graph, this class should include transitive dependencies.
   * @throws BuildException If the graph population encountered any errors.
   */
  public GraphBuilder(Output output, Dependencies dependencies, WorkflowHandler workflowHandler, boolean transitive) {
    if (dependencies == null || workflowHandler == null) {
      throw new BuildException("A Dependencies and WorkflowHandler are required for " +
        "constructing a GraphBuilder");
    }

    this.output = output;
    this.dependencies = dependencies;
    this.workflowHandler = workflowHandler;
    this.transitive = transitive;
  }

  /**
   * Constructs the graph using all of the configuration given in the constructor.
   *
   * @param resolutionContext The resolution context.
   * @return The graph.
   */
  public ArtifactGraph buildGraph(ResolutionContext resolutionContext) {
    Artifact projectArtifact = new Artifact("__PROJECT__GROUP__", "__PROJECT__NAME__", "__PROJECT__ARTIFACT__",
      "__PROJECT__VERSION__", "__ARTIFACT__TYPE__");
    ArtifactGraph graph = new ArtifactGraph(projectArtifact);

    // There must be a project artifact so that the version of the project's direct dependencies
    // is stored in the graph
    populateGraph(graph, projectArtifact, dependencies, new HashSet<Artifact>(), resolutionContext);
    dependencies.setGraph(graph);

    return graph;
  }

  /**
   * Adds the artifact dependencies to the {@link Graph} if the origin artifact is given.
   * Otherwise, this simply adds the artifacts to the graph without any links (edges).
   *
   * @param graph             The Graph to populate.
   * @param originArtifact    The origin artifact that is dependent on the Dependencies given.
   * @param dependencies      The list of dependencies to extract the artifacts from.
   * @param artifactsRecursed The set of artifacts already resolved and recursed for.
   * @param resolutionContext The resolution context.
   */
  protected void populateGraph(ArtifactGraph graph, Artifact originArtifact, Dependencies dependencies,
                               Set<Artifact> artifactsRecursed, ResolutionContext resolutionContext) {

    output.println(Level.DEBUG, "Running integration build resolver");
    ArtifactVersionTools.resolve(dependencies, workflowHandler);

    Map<String, ArtifactGroup> groups = dependencies.getArtifactGroups();
    for (String type : groups.keySet()) {
      ArtifactGroup ag = groups.get(type);
      List<Artifact> artifacts = ag.getArtifacts();
      for (Artifact artifact : artifacts) {

        GraphNode<ArtifactID, ArtifactLink> existing = graph.getGraphNode(artifact.getId());
        if (existing == null) {
          existing = graph.addGraphNode(artifact.getId());
        }

        // Create a link using nodes so that we can be explicit
        ArtifactMetaData amd = workflowHandler.getFetchWorkflowHandler().fetchMetaData(artifact, workflowHandler.getPublishWorkflowHandler(),
          resolutionContext);

        String compatibility = (amd != null) ? amd.getCompatibility() : null;
        GraphNode<ArtifactID, ArtifactLink> origin = graph.addGraphNode(originArtifact.getId());
        ArtifactLink link = new ArtifactLink(originArtifact.getVersion(), artifact.getVersion(), artifact.getIntegrationVersion(), type, compatibility);
        graph.addLink(origin, existing, link);

        // If we have already recursed this artifact, skip it.
        if (artifactsRecursed.contains(artifact)) {
          continue;
        }

        // Recurse
        if (amd != null && amd.getDependencies() != null && transitive) {
          populateGraph(graph, artifact, amd.getDependencies(), artifactsRecursed, resolutionContext);
        }

        // Add the artifact to the list
        artifactsRecursed.add(artifact);
      }
    }
  }
}
