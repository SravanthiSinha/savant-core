/*
 * Copyright (c) 2001-2011, Inversoft, All Rights Reserved
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

import java.util.Map;

/**
 * <p>
 * This interface defines a domain object that contains targets.
 * </p>
 *
 * @author Brian Pontarelli
 */
public interface Targetable {
  /**
   * @return All of the targets (modifiable).
   */
  Map<String, Target> getTargets();

  /**
   * Creates a new target if it hasn't already been created. If it has been created, the existing target is returned.
   *
   * @param name The name of the target.
   * @return The target and never null.
   */
  Target createTarget(String name);

  /**
   * Returns the target with the given name. If the target doesn't exist, this returns null.
   *
   * @param name The name of the target.
   * @return The target or null.
   */
  Target lookupTarget(String name);
}
