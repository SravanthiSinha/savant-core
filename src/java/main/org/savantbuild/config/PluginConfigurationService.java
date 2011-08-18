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

import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Plugin;
import org.savantbuild.domain.Project;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines how plugins are loaded configured within the project.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultPluginConfigurationService.class)
public interface PluginConfigurationService {
  /**
   * Loads the given plugin.
   *
   * @param context  The context.
   * @param project  The project.
   * @param artifact The artifact of the plugin.
   * @param settings The plugin settings.
   * @return The plugin.
   */
  Plugin loadPlugin(Context context, Project project, Artifact artifact, Map settings);

  /**
   * Adds default targets for each of the plugins targets that haven't been defined inside the project.
   *
   * @param project The project
   */
  void addDefaultTargets(Project project);
}
