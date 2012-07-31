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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.savantbuild.BuildException;
import org.savantbuild.config.groovy.PluginMetaClass;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.dep.tools.ArtifactFileTools;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Plugin;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.io.FileTools;
import org.savantbuild.run.Main;
import org.savantbuild.run.TargetExecutor;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.GroovyTools;
import org.savantbuild.util.GroovyTools.MetaClassBuilder;

import com.google.inject.Inject;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MetaClass;
import static org.savantbuild.util.CollectionTools.*;

/**
 * This class is the default plugin configuration service.
 *
 * @author Brian Pontarelli
 */
public class DefaultPluginConfigurationService implements PluginConfigurationService {
  private final DependencyManager manager;
  private final SyntheticTargetFactory factory;
  private final TargetExecutor targetExecutor;
  private final Output output;

  @Inject
  public DefaultPluginConfigurationService(DependencyManager manager, SyntheticTargetFactory factory,
                                           TargetExecutor targetExecutor, Output output) {
    this.manager = manager;
    this.factory = factory;
    this.targetExecutor = targetExecutor;
    this.output = output;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Plugin loadPlugin(Context context, Project project, Artifact artifact, Map settings) {
    output.debug("Starting plugin configuration");

    File pluginDir = context.getPluginCacheDirectory();
    if (!pluginDir.exists() && !pluginDir.mkdirs()) {
      throw new BuildException("Unable to create plugin cache directory [" + pluginDir.getAbsolutePath() + "]");
    }

    Map<Artifact, File> files = manager.getResolver().resolve(artifact, project.getWorkflow(), new HashSet<String>(), false);
    output.debug("Configuring plugin [" + artifact + "]");

    File file = files.get(artifact);
    File path = ArtifactFileTools.directory(pluginDir, artifact);
    boolean integration = artifact.isIntegrationBuild();
    if (!path.isDirectory() || integration) {
      // If this is an integration build, clean out the last version
      if (integration) {
        FileTools.prune(path);
      }

      try {
        FileTools.unzip(file, path);
      } catch (IOException e) {
        throw new BuildException("Unable to unzip plugin [" + artifact + "] after it was downloaded. Perhaps the " +
          "plugin is corrupt. Try using a different version.", e);
      }
    }

    // Generate the Dependency object for the plugin and add it to the project
    Dependencies dependencies = manager.getResolver().dependencies(artifact, project.getWorkflow());
    project.getDependencies().put("plugin['" + artifact.getName() + "']", dependencies);

    // The plugin is now exploded and we can load it up
    GroovyClassLoader pluginGCL = new GroovyClassLoader(Main.class.getClassLoader());
    pluginGCL.setShouldRecompile(true);
    pluginGCL.addClasspath(path.getAbsolutePath());

    // Add the plugins dependencies into the classloader
    Map<Artifact, File> pluginFiles = manager.getResolver().resolve(artifact, project.getWorkflow(), set("compile", "run"), true);
    for (Artifact depArtifact : pluginFiles.keySet()) {
      if (depArtifact.equals(artifact)) {
        continue;
      }

      File pluginDepFile = pluginFiles.get(depArtifact);
      pluginGCL.addClasspath(pluginDepFile.getAbsolutePath());
    }

    File pluginFile = new File(path, "plugin.savant");
    if (!pluginFile.isFile()) {
      throw new BuildException("Invalid plugin [" + artifact + "]. It doesn't contain a plugin.savant file");
    }

    Plugin plugin = loadPlugin(pluginGCL, pluginFile, context, project, settings);
    plugin.setName(artifact.getName());
    return plugin;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addDefaultTargets(Project project) {
    Set<String> names = new HashSet<String>();
    for (Plugin plugin : project.getPlugins().values()) {
      names.addAll(plugin.getTargets().keySet());
    }

    // Remove targets from the project that have bodies (not pre or post only)
    for (Target target : project.getTargets().values()) {
      if (target.getSteps().size() > 0) {
        names.remove(target.getName());
      }
    }

    // Add the default targets
    for (String name : names) {
      factory.build(name, project);
    }
  }

  private Plugin loadPlugin(GroovyClassLoader gcl, File file, final Context context, final Project project, final Map settings) {
    final Plugin plugin = new Plugin();
    GroovyTools.executeScript(gcl, file, new MetaClassBuilder() {
      public MetaClass build(Class<?> type) {
        return new PluginMetaClass(targetExecutor, type, project, plugin);
      }
    }, map("context", context, "project", project, "settings", settings));

    return plugin;
  }

}
