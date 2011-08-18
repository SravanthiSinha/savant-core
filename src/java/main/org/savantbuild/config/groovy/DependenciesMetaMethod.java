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

import org.savantbuild.BuildException;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Project;

import groovy.lang.Closure;
import static org.savantbuild.util.StringTools.*;

/**
 * <p>
 * This class is the Groovy MetaMethod that handles the dependency information.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class DependenciesMetaMethod extends AbstractMetaMethod {
  // Package protected reference to the current dependencies.
  static Dependencies current;

  private static final String MESSAGE = "Invalid dependencies definition. You can optionally specify a name for the " +
    "dependencies and must specify a block that defines the artifact groups and artifacts like this:\n" +
    "  dependencies {\n" +
    "    artifactGroup(\"compile\") {\n" +
    "      artifact(group: \"org.apache.commons\", name: \"commons-io\", version: \"1.0\")\n" +
    "    }\n" +
    "  }";
  private final Project project;

  public DependenciesMetaMethod(Class theClass, Project project) {
    super(theClass, Dependencies.class, "dependencies");
    this.project = project;
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

    Dependencies dependencies = new Dependencies();
    int index = 0;
    if (argArray.length == 2) {
      dependencies.setName(safe(argArray[index++]));
    }

    if (!(argArray[index] instanceof Closure)) {
      throw new BuildException(MESSAGE);
    }

    current = dependencies;
    Closure closure = (Closure) argArray[index];
    closure.call();
    current = null;

    if (project.getDependencies().containsKey(dependencies.getName())) {
      throw new BuildException("A dependencies named [" + dependencies.getName() + "] is already defined.");
    }

    project.getDependencies().put(dependencies.getName(), dependencies);
    return dependencies;
  }
}
