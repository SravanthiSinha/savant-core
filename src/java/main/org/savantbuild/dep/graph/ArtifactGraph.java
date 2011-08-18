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
package org.savantbuild.dep.graph;

import java.util.List;

import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.ArtifactID;
import org.savantbuild.domain.Dependencies;

/**
 * <p>
 * This class is a artifact and dependency version of the Graph.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ArtifactGraph extends HashGraph<ArtifactID, ArtifactLink> {
  private final Artifact root;

  public ArtifactGraph(Artifact root) {
    this.root = root;
    addGraphNode(root.getId());
  }

  public Artifact getRoot() {
    return root;
  }

  public Dependencies getDependencies(Artifact artifact) {
    List<GraphLink<ArtifactID, ArtifactLink>> links = getOutboundLinks(artifact.getId());

    Dependencies deps = new Dependencies();
    if (links != null && links.size() > 0) {
      for (GraphLink<ArtifactID, ArtifactLink> link : links) {
        ArtifactGroup group = deps.getArtifactGroups().get(link.value.getType());
        ArtifactID id = link.destination.getValue();
        Artifact dep = new Artifact(id, link.value.getDependencyVersion(), link.value.getDependencyIntegrationVersion());
        dep.setCompatibility(link.value.getCompatibility());
        if (group == null) {
          // The group cannot be empty during the add to the Dependencies object, therefor
          // I have to add the artifact here first and then add the group.
          group = new ArtifactGroup(link.value.getType());
          group.getArtifacts().add(dep);
          deps.getArtifactGroups().put(group.getType(), group);
        } else {
          group.getArtifacts().add(dep);
        }
      }
    }

    return deps;
  }
}
