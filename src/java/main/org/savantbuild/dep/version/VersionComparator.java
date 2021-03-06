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

/**
 * <p>
 * This interface defines a mechanism that can be used to determine if
 * two artifacts of the same, group, project, name and type are compatible
 * based on version alone.
 * </p>
 *
 * @author Brian Pontarelli
 */
public interface VersionComparator {
  /**
   * Determines if the two artifact versions are compatible or not. This method will only ever be
   * called with artifacts that are identical except for version, that is why only the version
   * strings are passed. However, the versions are not guaranteed to be different because this
   * will be used to check compatibility for all artifacts that have the same group, project, name
   * and type.
   *
   * @param previousVersion The previous version number to compare.
   * @param currentVersion  The current version being checked.
   * @return Null should be returned if the artifacts are not compatible. If they are compatible
   *         than the artifact that is the best version should be returned.
   */
  String determineBestVersion(String previousVersion, String currentVersion);
}
