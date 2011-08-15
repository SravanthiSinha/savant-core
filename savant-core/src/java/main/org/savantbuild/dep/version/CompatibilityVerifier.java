/*
 * Copyright (c) 2001-2006, Inversoft, All Rights Reserved
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
package org.savantbuild.dep.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.savantbuild.dep.graph.ArtifactGraph;
import org.savantbuild.dep.graph.ArtifactLink;
import org.savantbuild.dep.graph.GraphLink;
import org.savantbuild.dep.graph.GraphNode;
import org.savantbuild.dep.graph.GraphPath;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactID;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.run.output.Level;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.ErrorList;

/**
 * <p>
 * This class is a compatibility verifier that is used to verify a
 * set of dependencies including all of the transitive dependencies.
 * This check is done to ensure that all artifacts in the entire set
 * are compatible and to make selections about which versions of
 * artifacts should be used over others.
 * </p>
 * <p/>
 * <p>
 * The interface that is used to determine compatibility as well as
 * upgrades when an artifact has two compatible versions is the
 * {@link VersionComparator} interface.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class CompatibilityVerifier {
  private final Output output;

  public CompatibilityVerifier(Output output) {
    this.output = output;
  }

  /**
   * This method performs a bredth first traversal of the artifact tree and verifies that the
   * artifacts in the tree are compatible. It also builds up a list of the artifacts that need
   * to be fetched.
   *
   * @param deps       The dependencies of this project (top level).
   * @param graph      The artifact graph that describes the entire dependencies tree for the project.
   * @param groupTypes (Optional) This is a set of group types that are currently being resolved.
   *                   This is used to reduce the compatibility check.
   * @return A result that contains the list of artifacts that should be fetched based on
   *         compatibility and an ErrorList that will contain any errors that were found.
   */
  public ErrorList verifyCompatibility(Dependencies deps, ArtifactGraph graph, Set<String> groupTypes) {
    ErrorList errors = new ErrorList();
    verifyCompatibilityTypes(deps, graph, groupTypes, errors);
    verifyCompatibility(deps, graph, groupTypes, errors);
    return errors;
  }

  /**
   * Verifies all of the artifacts that are identical but have different versions have the same
   * compatibility setting.
   *
   * @param deps       The dependencies of this project (top level).
   * @param graph      The ArtifactGraph.
   * @param groupTypes (Optional) This is a set of group types that are currently being resolved.
   *                   This is used to reduce the compatibility check.
   * @param errors     The ErrorList to add any errors to.
   */
  protected void verifyCompatibilityTypes(final Dependencies deps, final ArtifactGraph graph,
                                          final Set<String> groupTypes, final ErrorList errors) {
    output.println(Level.DEBUG, "Verifying compatTypes for artifacts");

    Set<GraphNode<ArtifactID, ArtifactLink>> graphNodes = graph.getAllGraphNodes();
    for (GraphNode<ArtifactID, ArtifactLink> graphNode : graphNodes) {
      List<GraphLink<ArtifactID, ArtifactLink>> graphLinks = graphNode.getInboundLinksList();

      GraphLink<ArtifactID, ArtifactLink> linkToCompare = null;
      for (GraphLink<ArtifactID, ArtifactLink> graphLink : graphLinks) {
        // If the group type is not in the list, skip this link
        if (groupTypes != null && groupTypes.size() > 0 && !groupTypes.contains(graphLink.value.getType())) {
          continue;
        }

        String current = graphLink.value.getCompatibility();
        if (current != null && linkToCompare == null) {
          linkToCompare = graphLink;
        } else if (current != null && !current.equals(linkToCompare.value.getCompatibility())) {
          String error = "Artifact [" + graphLink.origin.getValue() + "] has two different " +
            "compatibility type properties in different locations. One is [" + current +
            "] and the other is [" + linkToCompare.value.getCompatibility() +
            "]. Below are the pathes to the two locations\n" +
            makeCompatibilityString(deps, graph, linkToCompare, graphLink);
          errors.addError(error);
        }
      }
    }
  }

  /**
   * Verifies the compatibility of the artifacts in the graph.
   *
   * @param deps       The project dependencies to start from.
   * @param graph      The graph to traverse to find all the artifacts and verify compatibility.
   * @param groupTypes (Optional) This is a set of group types that are currently being resolved.
   *                   This is used to reduce the compatibility check.
   * @param errors     Collects the errors.
   */
  protected void verifyCompatibility(final Dependencies deps, final ArtifactGraph graph,
                                     final Set<String> groupTypes, final ErrorList errors) {
    output.println(Level.DEBUG, "Verifying compatibility of artifacts in groups with type " + groupTypes);

    Set<GraphNode<ArtifactID, ArtifactLink>> graphNodes = graph.getAllGraphNodes();
    for (GraphNode<ArtifactID, ArtifactLink> graphNode : graphNodes) {
      String compatType = determineCompatType(graphNode);
      VersionComparator versionComparator = VersionComparatorRegistry.lookup(compatType);

      output.println(Level.DEBUG, "Using compatibility checker of type [" + compatType + "] with checker class [" +
        versionComparator.getClass() + "]");

      List<GraphLink<ArtifactID, ArtifactLink>> inboundLinks = graphNode.getInboundLinksList();

      // Only process them if there might be multiple versions
      if (inboundLinks.size() <= 1) {
        output.println(Level.DEBUG, "Skipping artifact [" + graphNode.getValue() + "] because there is only a single inbound link");
        continue;
      }

      // Process away
      GraphLink<ArtifactID, ArtifactLink> bestLinkSoFar = null;
      String bestVersionSoFar = null;
      boolean twoVersions = false;
      boolean errorFound = false;
      for (GraphLink<ArtifactID, ArtifactLink> inboundLink : inboundLinks) {
        // If the group type is not in the list, skip this link
        if (groupTypes != null && groupTypes.size() > 0 && !groupTypes.contains(inboundLink.value.getType())) {
          continue;
        }

        output.println(Level.DEBUG, "Checking [" + graphNode.getValue() + "] for compatibility");

        String version = inboundLink.value.getDependencyVersion();
        output.println(Level.DEBUG, "Checking version [" + version + "] of artifact [" + graphNode.getValue() + "]");

        if (bestVersionSoFar == null) {
          bestVersionSoFar = version;
          bestLinkSoFar = inboundLink;
          output.println(Level.DEBUG, "First version for artifact");
          continue;
        } else if (version.equals(bestVersionSoFar)) {
          output.println(Level.DEBUG, "Identical version for artifact");
          continue;
        }

        twoVersions = true;
        output.println(Level.DEBUG, "Comparing versions [" + bestVersionSoFar + "] and [" + version + "] of artifact [" +
          graphNode.getValue() + "]");

        String result = versionComparator.determineBestVersion(bestVersionSoFar, version);
        if (result == null) {
          // This means there was an error
          output.println(Level.DEBUG, "Making error string");
          String error = makeCompatibilityString(deps, graph, bestLinkSoFar, inboundLink);
          errors.addError(error);
          errorFound = true;
        } else if (result.equals(version)) {
          bestVersionSoFar = version;
          bestLinkSoFar = inboundLink;
        }
      }

      // Update all the links to the best version
      if (twoVersions && !errorFound) {
        output.println(Level.DEBUG, "Artifact [" + graphNode.getValue() + "] had multiple versions and no errors");

        inboundLinks = graphNode.getInboundLinksList();
        for (GraphLink<ArtifactID, ArtifactLink> inboundLink : inboundLinks) {
          ArtifactLink link = inboundLink.value;
          String dependentVersion = link.getDependentVersion();
          String type = link.getType();

          ArtifactLink newLink = new ArtifactLink(dependentVersion, bestLinkSoFar.value.getDependencyVersion(),
            bestLinkSoFar.value.getDependencyIntegrationVersion(), type, compatType);
          graph.removeLink(inboundLink.origin, inboundLink.destination, link);
          output.println(Level.DEBUG, "Breaking bad link from [" + inboundLink.origin.getValue() + "] to [" +
            inboundLink.destination.getValue() + "] version [" + link.getDependencyVersion() + "]");

          graph.addLink(inboundLink.origin, inboundLink.destination, newLink);
          output.println(Level.DEBUG, "Add better link from [" + inboundLink.origin.getValue() + "] to [" +
            inboundLink.destination.getValue() + "] version [" + bestVersionSoFar + "]");
        }

        output.println(Level.DEBUG, "Removing outbound links for other versions");
        List<GraphLink<ArtifactID, ArtifactLink>> outboundLinks = graphNode.getOutboundLinksList();
        for (GraphLink<ArtifactID, ArtifactLink> outboundLink : outboundLinks) {
          if (!outboundLink.value.getDependentVersion().equals(bestVersionSoFar)) {
            output.println(Level.DEBUG, "Removing link from [" + outboundLink.origin.getValue() + "] to [" +
              outboundLink.destination.getValue() + "] because it was for version [" +
              outboundLink.value.getDependentVersion() + "] which is older than the " +
              "best version found of [" + bestVersionSoFar + "]");

            graph.removeLink(outboundLink.origin, outboundLink.destination, outboundLink.value);
          }
        }
      }

    }
  }

  /**
   * Given the GraphNode, this method looks at all the inbound links that have compatType values
   * and finds the first one that isn't null. At this point the compatType values should all be
   * the same or null.
   *
   * @param node The GraphNode to look at the links for.
   * @return The compatType to use or null.
   */
  protected String determineCompatType(GraphNode<ArtifactID, ArtifactLink> node) {
    List<GraphLink<ArtifactID, ArtifactLink>> inboundLinks = node.getInboundLinksList();
    for (GraphLink<ArtifactID, ArtifactLink> inboundLink : inboundLinks) {
      String compatType = inboundLink.value.getCompatibility();
      output.println(Level.DEBUG, "Determining compatType for artifact [" + node.getValue() + "]");

      if (compatType != null) {
        output.println(Level.DEBUG, "Found compatType [" + compatType + "]");
        return compatType;
      }
    }

    return null;
  }

  protected String makeCompatibilityString(Dependencies dependencies, ArtifactGraph graph,
                                           GraphLink<ArtifactID, ArtifactLink> first, GraphLink<ArtifactID, ArtifactLink> second) {
    Set<Artifact> rootArtifacts = dependencies.getAllArtifacts();
    List<GraphPath<ArtifactID>> artifactPaths1 = makeAllPaths(rootArtifacts, graph, first.destination.getValue());
    List<GraphPath<ArtifactID>> artifactPaths2 = makeAllPaths(rootArtifacts, graph, second.destination.getValue());

    StringBuffer buf = new StringBuffer();
    buf.append("Artifact [").append(artifactString(first)).append("] not compatible with [").
      append(artifactString(second)).append("]\n");
    makePathString(buf, first, artifactPaths1);
    makePathString(buf, second, artifactPaths2);

    return buf.toString();
  }

  private List<GraphPath<ArtifactID>> makeAllPaths(Set<Artifact> rootArtifacts, ArtifactGraph graph,
                                                   ArtifactID id) {
    List<GraphPath<ArtifactID>> artifactPaths = new ArrayList<GraphPath<ArtifactID>>();
    for (Artifact rootArtifact : rootArtifacts) {
      List<GraphPath<ArtifactID>> paths = graph.getPaths(rootArtifact.getId(), id);
      if (paths != null) {
        artifactPaths.addAll(paths);
      }

      output.println(Level.DEBUG, "Calculating path from [" + rootArtifact + "] to [" + id + "]");
    }

    return artifactPaths;
  }

  private void makePathString(StringBuffer buf, GraphLink<ArtifactID, ArtifactLink> link,
                              List<GraphPath<ArtifactID>> paths) {
    buf.append("\tPaths to artifact [").append(artifactString(link)).append("] are:\n");
    if (paths.size() == 0) {
      buf.append("\t\tInside this project.\n");
    } else {
      for (GraphPath<ArtifactID> path : paths) {
        buf.append("\t\t");

        List<ArtifactID> artifactPath = path.getPath();
        for (int j = 0; j < artifactPath.size(); j++) {
          ArtifactID artInPath = artifactPath.get(j);
          buf.append("[").append(artInPath).append("]");
          if (j + 1 < artifactPath.size()) {
            buf.append(" -> ");
          }
        }

        buf.append("\n");
      }
    }
  }

  private String artifactString(GraphLink<ArtifactID, ArtifactLink> link) {
    ArtifactID id = link.destination.getValue();
    return id.getGroup() + "|" + id.getProject() + "|" + id.getName() + "-" + link.value.getDependencyVersion() + "." + id.getType();
  }
}
