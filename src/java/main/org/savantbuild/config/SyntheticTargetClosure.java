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
package org.savantbuild.config;

import java.util.Map;

import org.savantbuild.domain.Plugin;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.run.TargetExecutor;

import groovy.lang.Closure;

/**
 * <p>
 * This class is a Groovy closure that provides a synthetic target.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class SyntheticTargetClosure extends Closure {
  private final TargetExecutor targetExecutor;
  private final String targetName;
  private final Project project;

  public SyntheticTargetClosure(TargetExecutor targetExecutor, String targetName, Project project) {
    super(project);
    this.targetExecutor = targetExecutor;
    this.targetName = targetName;
    this.project = project;
  }

  @Override
  public Object call() {
    for (String name : project.getPlugins().keySet()) {
      Plugin plugin = project.getPlugins().get(name);
      Target target = plugin.getTargets().get(targetName);
      if (target != null) {
        targetExecutor.run(project, name + ":" + targetName);
      }
    }

    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object call(Object args) {
    for (String name : project.getPlugins().keySet()) {
      Plugin plugin = project.getPlugins().get(name);
      Target target = plugin.getTargets().get(targetName);
      if (target != null) {
        targetExecutor.run(project, name + ":" + targetName, (Map) args);
      }
    }

    return null;
  }
}
