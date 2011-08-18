/*
 * Copyright (c) 2001-2010, Inversoft, All Rights Reserved
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
package org.savantbuild.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.savantbuild.dep.graph.ArtifactGraph;

/**
 * <p>
 * This class defines a target within the build file.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Dependencies {
  private final Map<String, ArtifactGroup> artifactGroups = new HashMap<String, ArtifactGroup>();
  private ArtifactGraph graph;
  private String name;

  public Dependencies() {
  }

  public Dependencies(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArtifactGraph getGraph() {
    return graph;
  }

  public void setGraph(ArtifactGraph graph) {
    this.graph = graph;
  }

  public Map<String, ArtifactGroup> getArtifactGroups() {
    return artifactGroups;
  }

  /**
   * Collects all of the artifacts from all of the groups.
   *
   * @return All of the artifacts.
   */
  public Set<Artifact> getAllArtifacts() {
    Set<Artifact> set = new HashSet<Artifact>();
    for (ArtifactGroup group : artifactGroups.values()) {
      List<Artifact> artifacts = group.getArtifacts();
      for (Artifact artifact : artifacts) {
        set.add(artifact);
      }
    }

    return set;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Dependencies that = (Dependencies) o;

    if (!artifactGroups.equals(that.artifactGroups)) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = artifactGroups.hashCode();
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }
}
