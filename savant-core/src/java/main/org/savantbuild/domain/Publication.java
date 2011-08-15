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
package org.savantbuild.domain;

/**
 * <p>
 * This class is a publishable artifact for a project. This is similar to
 * an artifact, but doesn't have the group, project and version, since
 * those are controlled by the project and also has a file reference and
 * dependencies reference.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Publication {
  private String name;
  private String type;
  private String file;
  private String compatibility;
  private String dependencies;

  public Publication() {
  }

  public Publication(String name, String type, String file, String compatibility, String dependencies) {
    this.name = name;
    this.type = type;
    this.file = file;
    this.compatibility = compatibility;
    this.dependencies = dependencies;
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

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getCompatibility() {
    return compatibility;
  }

  public void setCompatibility(String compatibility) {
    this.compatibility = compatibility;
  }

  public String getDependencies() {
    return dependencies;
  }

  public void setDependencies(String dependencies) {
    this.dependencies = dependencies;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Publication that = (Publication) o;
    return name.equals(that.name) && type.equals(that.type);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
