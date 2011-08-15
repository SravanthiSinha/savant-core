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

import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactID;

/**
 * <p>
 * This class stores the information for links between artifacts in the graph.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ArtifactLink {
  private final String dependentVersion;
  private final String dependencyVersion;
  private final String dependencyIntegrationVersion;
  private final String type;
  private final String compatibility;

  public ArtifactLink(String dependentVersion, String dependencyVersion, String dependencyIntegrationVersion, String type, String compatibility) {
    this.dependentVersion = dependentVersion;
    this.dependencyVersion = dependencyVersion;
    this.dependencyIntegrationVersion = dependencyIntegrationVersion;
    this.type = type;
    this.compatibility = compatibility;
  }

  public String getDependentVersion() {
    return dependentVersion;
  }

  public String getDependencyVersion() {
    return dependencyVersion;
  }

  public String getDependencyIntegrationVersion() {
    return dependencyIntegrationVersion;
  }

  public String getType() {
    return type;
  }

  public String getCompatibility() {
    return compatibility;
  }

  public Artifact toArtifact(ArtifactID id) {
    return new Artifact(id, dependencyVersion, dependencyIntegrationVersion);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArtifactLink that = (ArtifactLink) o;

    if (compatibility != null ? !compatibility.equals(that.compatibility) : that.compatibility != null)
      return false;
    if (dependencyVersion != null ? !dependencyVersion.equals(that.dependencyVersion) : that.dependencyVersion != null)
      return false;
    if (dependentVersion != null ? !dependentVersion.equals(that.dependentVersion) : that.dependentVersion != null)
      return false;
    if (!type.equals(that.type)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (dependentVersion != null ? dependentVersion.hashCode() : 0);
    result = 31 * result + (dependencyVersion != null ? dependencyVersion.hashCode() : 0);
    result = 31 * result + type.hashCode();
    result = 31 * result + (compatibility != null ? compatibility.hashCode() : 0);
    return result;
  }
}
