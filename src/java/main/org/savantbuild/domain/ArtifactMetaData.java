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
 * This class is the model for the artifact meta data XML file that is
 * published along with artifacts.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ArtifactMetaData {
  private final Dependencies dependencies;
  private final String compatibility;

  public ArtifactMetaData(Dependencies dependencies, String compatibility) {
    this.dependencies = dependencies;
    this.compatibility = compatibility;
  }

  public Dependencies getDependencies() {
    return dependencies;
  }

  public String getCompatibility() {
    return compatibility;
  }
}
