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

import org.savantbuild.dep.version.ArtifactVersionTools;

/**
 * <p>
 * This class defines a target within the build file.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Artifact {
  private ArtifactID id = new ArtifactID();
  private String version;
  private String compatibility;
  private String integrationVersion;

  public Artifact() {
  }

  /**
   * Called for when compatibility is null
   *
   * @param group the group
   * @param project the project
   * @param name the name
   * @param version the version
   * @param type the type
   */
  public Artifact(String group, String project, String name, String version, String type) {
    this(group, project, name, version, type, null);
  }

  public Artifact(String group, String project, String name, String version, String type, String compatibility) {
    this.id = new ArtifactID(group, project, name, type);
    this.version = version;
    this.compatibility = compatibility;
  }

  public Artifact(ArtifactID id, String version, String integrationVersion) {
    this.id = id;
    this.version = version;
    this.integrationVersion = integrationVersion;
  }

  public ArtifactID getId() {
    return id;
  }

  public String getGroup() {
    return id.getGroup();
  }

  public void setGroup(String group) {
    id.setGroup(group);
  }

  public String getProject() {
    return id.getProject();
  }

  public void setProject(String project) {
    id.setProject(project);
  }

  public String getName() {
    return id.getName();
  }

  public void setName(String name) {
    id.setName(name);
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getType() {
    return id.getType();
  }

  public void setType(String type) {
    id.setType(type);
  }

  public String getCompatibility() {
    return compatibility;
  }

  public void setCompatibility(String compatibility) {
    this.compatibility = compatibility;
  }

  public String getIntegrationVersion() {
    return integrationVersion;
  }

  public void setIntegrationVersion(String integrationVersion) {
    this.integrationVersion = integrationVersion;
  }

  /**
   * @return Whether or not the version of this artifact is an integration build version.
   */
  public boolean isIntegrationBuild() {
    return version.endsWith(ArtifactVersionTools.INTEGRATION);
  }

  /**
   * @return Whether or not the version of this artifact is a latest build version.
   */
  public boolean isLatestBuild() {
    return version.equals(ArtifactVersionTools.LATEST);
  }

  /**
   * <p>
   * Returns the artifact file name. This does not include any path information at all and would
   * look something like this:
   * </p>
   * <p/>
   * <pre>
   * common-collections-2.1.jar
   * </pre>
   *
   * @return The file name.
   */
  public String getArtifactFile() {
    return prefix() + "." + id.getType();
  }

  /**
   * <p>
   * Returns the artifact MetaData file name. This does not include any path information at all and would
   * look something like this:
   * </p>
   * <p/>
   * <pre>
   * common-collections-2.1.jar.amd
   * </pre>
   *
   * @return The MetaData file name.
   */
  public String getArtifactMetaDataFile() {
    return prefix() + "." + id.getType() + ".amd";
  }

  /**
   * <p>
   * Returns the artifact negative MetaData file name. This does not include any path information at all and would
   * look something like this:
   * </p>
   * <p/>
   * <pre>
   * common-collections-2.1.jar.amd.neg
   * </pre>
   *
   * @return The negative MetaData file name.
   */
  public String getArtifactNegativeMetaDataFile() {
    return getArtifactMetaDataFile() + ".neg";
  }

  /**
   * <p>
   * Returns the artifact source file name. This does not include any path information at all
   * and would look something like this:
   * </p>
   * <p/>
   * <pre>
   * common-collections-2.1-src.jar
   * </pre>
   *
   * @return The source file name.
   */
  public String getArtifactSourceFile() {
    return prefix() + "-src." + id.getType();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Artifact artifact = (Artifact) o;
    return id.equals(artifact.id) && version.equals(artifact.version);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + version.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return id.getGroup() + ":" + id.getProject() + ":" + prefix() + "." + id.getType();
  }

  private String prefix() {
    if (isIntegrationBuild() && integrationVersion != null) {
      return id.getName() + "-" + integrationVersion;
    }

    return id.getName() + "-" + version;
  }
}
