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
package org.savantbuild.config.groovy;

import java.util.Collections;
import java.util.Map;

import org.savantbuild.BuildException;
import org.savantbuild.config.PluginConfigurationService;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Plugin;
import org.savantbuild.domain.Project;

import static org.savantbuild.util.StringTools.*;

/**
 * <p>
 * This class is the Groovy MetaMethod that handles the publications.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class PluginMetaMethod extends AbstractMetaMethod {
  private static final String MESSAGE = "Invalid plugin definition. You must specify the group, name, and version of the " +
    "plugin like this:\n" +
    "  plugin(group: \"org.savantbuild.plugin\", name: \"foo\", version: \"1.0\")\n\n" +
    "You can also provide plugin settings using a Map like this:\n" +
    "  plugin(group: \"org.savantbuild.plugin\", name: \"foo\", version: \"1.0\", settings: [\"name\": \"value\"])";

  private final PluginConfigurationService pluginService;
  private final Context context;
  private final Project project;

  public PluginMetaMethod(PluginConfigurationService pluginService, Class theClass, Context context, Project project) {
    super(theClass, Artifact.class, "plugin");
    this.pluginService = pluginService;
    this.context = context;
    this.project = project;
  }

  @Override
  public Object invoke(Object o, Object[] arguments) {
    if (arguments.length != 1) {
      throw new BuildException(MESSAGE);
    }

    Object[] argArray = (Object[]) arguments[0];
    if (argArray == null || argArray.length != 1 || !(argArray[0] instanceof Map)) {
      throw new BuildException(MESSAGE);
    }

    Map values = (Map) argArray[0];
    if (!values.containsKey("group") || !values.containsKey("name") || !values.containsKey("version")) {
      throw new BuildException(MESSAGE);
    }

    Artifact artifact = new Artifact(safe(values.get("group")), safe(values.get("name")), safe(values.get("name")), safe(values.get("version")), "jar");
    Dependencies d = project.getDependencies().get("plugins");
    if (d == null) {
      d = new Dependencies("plugins");
      d.getArtifactGroups().put("run", new ArtifactGroup("run"));
      project.getDependencies().put("plugins", d);
    }

    d.getArtifactGroups().get("run").getArtifacts().add(artifact);

    Map settings;
    if (values.containsKey("settings")) {
      try {
        settings = (Map) values.get("settings");
      } catch (ClassCastException e) {
        throw new BuildException(MESSAGE);
      }
    } else {
      settings = Collections.emptyMap();
    }

    if (project.getPlugins().get(artifact.getName()) != null) {
      throw new BuildException("Project already has a plugin with the name [" + artifact.getName() + "]. Since " +
        "Savant places plugins into a Map using the name attribute, you can only use a single plugin with a " +
        "given name.");
    }

    Plugin plugin = pluginService.loadPlugin(context, project, artifact, settings);
    project.getPlugins().put(plugin.getName(), plugin);

    return artifact;
  }
}
