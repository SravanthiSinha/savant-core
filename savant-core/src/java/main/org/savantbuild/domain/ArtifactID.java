/*
 * Copyright (c) 2001-2010, Inversoft, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.domain;

/**
 * <p>
 * This class is contains the properties that define an artifacts
 * identity. Any two artifacts whose identity match are considered
 * the same artifact. All other properties associated with the
 * artifact usually determine the artifacts variant (such as version).
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ArtifactID {
  private String group;
  private String project;
  private String name;
  private String type;

  public ArtifactID() {
  }

  public ArtifactID(String group, String project, String name, String type) {
    this.group = group;
    this.project = project;
    this.name = name;
    this.type = type;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArtifactID that = (ArtifactID) o;
    return group.equals(that.group) && name.equals(that.name) && project.equals(that.project) && type.equals(that.type);
  }

  public int hashCode() {
    int result;
    result = group.hashCode();
    result = 31 * result + project.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }

  public String toString() {
    return group + ":" + project + ":" + name + ":" + type;
  }
}
