/*
 * Copyright (c) 2001-2010, Inversoft, All Rights Reserved
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
package org.savantbuild.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.savantbuild.config.groovy.BuildMetaClass;
import org.savantbuild.config.groovy.WorkflowsMetaClass;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Process;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Publication;
import org.savantbuild.domain.PublishWorkflow;
import org.savantbuild.domain.Workflow;
import org.savantbuild.domain.Workflows;
import org.savantbuild.run.Main;
import org.savantbuild.run.TargetExecutor;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.GroovyTools;
import org.savantbuild.util.GroovyTools.MetaClassBuilder;

import com.google.inject.Inject;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MetaClass;
import static java.util.Collections.*;
import static org.savantbuild.util.CollectionTools.*;

/**
 * This class loads the build script and uses Groovy Meta-Programming to setup the targets, build graph, etc.
 *
 * @author Brian Pontarelli
 */
public class DefaultConfigurationService implements ConfigurationService {
  private final DependencyManager manager;
  private final PluginConfigurationService pluginService;
  private final TargetExecutor targetExecutor;
  private final Output output;

  @Inject
  public DefaultConfigurationService(DependencyManager manager, PluginConfigurationService pluginService,
                                     TargetExecutor targetExecutor, Output output) {
    this.manager = manager;
    this.pluginService = pluginService;
    this.targetExecutor = targetExecutor;
    this.output = output;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void configure(Context context) {
    output.debug("Starting configuration");

    GroovyClassLoader gcl = new GroovyClassLoader(Main.class.getClassLoader());

    // Load the workflows
    Workflows workflows;
    determineWorkflowFile(context);
    if (context.getWorkflowFile().isFile()) {
      workflows = loadWorkflows(gcl, context.getWorkflowFile());
    } else {
      workflows = defaultWorkflows();
    }
    context.setWorkflows(workflows);

    // Load the project and the plugins definitions
    Project project = null;
    determineBuildFile(context);
    if (context.getBuildFile().isFile()) {
      project = loadProject(gcl, context.getBuildFile(), context);
      addProjectDefaults(project);
      context.setProject(project);
    }

    if (project != null) {
      pluginService.addDefaultTargets(project);
    }
  }

  private Project loadProject(GroovyClassLoader gcl, File file, final Context context) {
    output.debug("Parsing build file [" + file.getAbsolutePath() + "]");

    final Project project = new Project();
    GroovyTools.executeScript(gcl, file, new MetaClassBuilder() {
      public MetaClass build(Class<?> type) {
        return new BuildMetaClass(manager, pluginService, targetExecutor, type, context, project);
      }
    }, map("context", context, "project", project));

    return project;
  }

  private Workflows loadWorkflows(GroovyClassLoader gcl, File file) {
    output.debug("Parsing workflows file [" + file.getAbsolutePath() + "]");

    final Workflows workflows = new Workflows();
    GroovyTools.executeScript(gcl, file, new MetaClassBuilder() {
      public MetaClass build(Class<?> type) {
        return new WorkflowsMetaClass(type, workflows);
      }
    }, emptyMap());

    return workflows;
  }

  private void addProjectDefaults(Project project) {
    if (project.getPublications().isEmpty()) {
      String file = "target/jars/" + project.getName() + "-" + project.getVersion() + ".jar";
      Publication publication = new Publication(project.getName(), "jar", file, "minor", null);
      project.getPublications().add(publication);
    }
  }

  private Workflows defaultWorkflows() {
    Map<String, String> url = new HashMap<String, String>();
    url.put("type", "url");
    url.put("url", "http://repository.savantbuild.org");

    Map<String, String> cache = new HashMap<String, String>();
    cache.put("type", "cache");

    Workflows workflows = new Workflows();
    Workflow workflow = new Workflow();
    workflow.getFetchProcesses().add(new Process(cache));
    workflow.getFetchProcesses().add(new Process(url));
    workflow.getPublishProcesses().add(new Process(cache));
    workflows.getWorkflows().put(null, workflow);

    PublishWorkflow local = new PublishWorkflow();
    local.getProcesses().add(new Process(cache));
    workflows.getIntegrationWorkflows().put(null, local);

    return workflows;
  }

  private void determineBuildFile(Context context) {
    if (context.getCli() != null && context.getCli().hasOption("file")) {
      File file = new File(context.getCli().getOptionValue("file"));
      context.setBuildFile(file);
    }
  }

  private void determineWorkflowFile(Context context) {
    if (context.getCli() != null && context.getCli().hasOption("workflows")) {
      File file = new File(context.getCli().getOptionValue("workflows"));
      context.setWorkflowFile(file);
    }
  }
}
