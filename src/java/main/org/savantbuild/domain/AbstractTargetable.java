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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class provides the default implementation of the Targetable methods.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractTargetable implements Targetable {
  private final Map<String, Target> targets = new LinkedHashMap<String, Target>();

  /**
   * {@inheritDoc}
   */
  public Map<String, Target> getTargets() {
    return targets;
  }

  /**
   * {@inheritDoc}
   */
  public Target createTarget(String name) {
    Target target = targets.get(name);
    if (target == null) {
      target = new Target(name);
      targets.put(name, target);
    }

    return target;
  }

  /**
   * {@inheritDoc}
   */
  public Target lookupTarget(String name) {
    return targets.get(name);
  }
}
