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
package org.savantbuild.config.groovy;

import java.util.Map;

import org.savantbuild.BuildException;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Workflows;

import groovy.lang.Closure;

/**
 * This class is the Groovy MetaMethod that handles the project information.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ProjectMetaMethod extends AbstractMetaMethod {
  private static final String MESSAGE = "Invalid project definition. You must supply the name, group and version of the project like this:\n" +
    "  project(name: \"name\", group: \"group\", version: \"version\")\n\n" +
    "You can also supply dependency information using a block like this:\n" +
    "  project(name: \"name\", group: \"group\", version: \"version\") {\n" +
    "    dependencies {\n" +
    "      artifactGroup(\"compile\") {\n" +
    "        artifact(group: \"org.apache.commons\", name: \"commons-io\", version: \"1.0\")\n" +
    "      }\n" +
    "    }\n" +
    "  }";

  private final DependencyManager manager;
  private final Context context;
  private final Project project;

  public ProjectMetaMethod(DependencyManager manager, Class theClass, Context context, Project project) {
    super(theClass, Project.class, "project");
    this.manager = manager;
    this.project = project;
    this.context = context;
  }

  @Override
  public Object invoke(Object o, Object[] arguments) {
    if (arguments.length != 1) {
      throw new BuildException(MESSAGE);
    }

    Object[] argArray = (Object[]) arguments[0];
    if (argArray == null || argArray.length < 1 || argArray.length > 2) {
      throw new BuildException(MESSAGE);
    }

    Map<String, String> values = (Map<String, String>) argArray[0];
    project.setName(values.get("name"));
    project.setGroup(values.get("group"));
    project.setVersion(values.get("version"));
    if (project.getName() == null || project.getGroup() == null || project.getVersion() == null) {
      throw new BuildException(MESSAGE);
    }

    // Determine the workflows for the project
    Workflows workflows = context.getWorkflows();
    project.setWorkflow(manager.determineProjectWorkflow(workflows, project));
    project.setIntegrationWorkflow(manager.determineProjectPublishWorkflow(context.getWorkflows(), project, true));
    project.setReleaseWorkflow(manager.determineProjectPublishWorkflow(context.getWorkflows(), project, false));

    if (argArray.length == 2) {
      if (!(argArray[1] instanceof Closure)) {
        throw new BuildException(MESSAGE);
      }

      Closure closure = (Closure) argArray[1];
      closure.call();
    }

    return project;
  }
}
