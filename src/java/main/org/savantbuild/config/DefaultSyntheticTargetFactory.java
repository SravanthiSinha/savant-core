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

import java.util.ArrayList;
import java.util.List;

import org.savantbuild.domain.Plugin;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.run.TargetExecutor;

import com.google.inject.Inject;

/**
 * <p>
 * This is the default synthetic target factory. It might create and add the target if the project
 * doesn't already have one for the name and then adds a closure to the target that will handle
 * invocation of all the plugin targets.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultSyntheticTargetFactory implements SyntheticTargetFactory {
  private final TargetExecutor targetExecutor;

  @Inject
  public DefaultSyntheticTargetFactory(TargetExecutor targetExecutor) {
    this.targetExecutor = targetExecutor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void build(String name, Project project) {
    Target target = project.getTargets().get(name);
    if (target == null) {
      target = new Target(name);
      project.getTargets().put(name, target);
    }

    List<Plugin> matches = new ArrayList<Plugin>();
    for (Plugin plugin : project.getPlugins().values()) {
      Target pluginTarget = plugin.getTargets().get(name);
      if (pluginTarget != null) {
        matches.add(plugin);
      }
    }

    // Attempt to infer the targets dependencies by iterating over the plugin targets and using a exact match
    // for all the dependencies of those targets
    for (Plugin match : matches) {
      Target pluginTarget = match.getTargets().get(name);
      if (target.getDependencies().isEmpty()) {
        target.getDependencies().addAll(pluginTarget.getDependencies());
      } else if (!target.getDependencies().equals(pluginTarget.getDependencies())) {
        target.getDependencies().clear();
        break;
      }
    }

    if (matches.size() == 1) {
      Target pluginTarget = matches.get(0).getTargets().get(name);
      target.setDescription("{Synthetic Target} " + pluginTarget.getDescription());
      target.getParams().putAll(pluginTarget.getParams());
    } else {
      StringBuilder build = new StringBuilder("{Synthetic Target} Multiple descriptions from each plugin:\n");
      for (Plugin match : matches) {
        Target pluginTarget = match.getTargets().get(name);
        build.append(match.getName()).append(":").append(name).append(" ").append(pluginTarget.getDescription());
      }
      target.setDescription(build.toString());
    }

    target.leftShift(new SyntheticTargetClosure(targetExecutor, name, project));
  }
}
