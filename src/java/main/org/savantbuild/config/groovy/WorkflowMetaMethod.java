/*
 * Copyright (c) 2001-2010, Inversoft, All Rights Reserved
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

import org.savantbuild.BuildException;
import org.savantbuild.domain.Workflow;
import org.savantbuild.domain.Workflows;

import groovy.lang.Closure;

/**
 * <p>
 * This class is the Groovy MetaMethod that handles the workflow information.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class WorkflowMetaMethod extends AbstractMetaMethod {
  // Package protected reference to the current workflow
  static Workflow current;

  private static final String MESSAGE = "Invalid workflow definition. You can optional supply a name for the workflow like this:\n" +
    "  workflow(\"name\") {\n" +
    "    fetchProcesses {\n" +
    "      process(...)\n" +
    "    }\n" +
    "    publishProcesses {\n" +
    "      process(...)\n" +
    "    }\n" +
    "  }";
  private final Workflows workflows;

  public WorkflowMetaMethod(Class theClass, Workflows workflows) {
    super(theClass, Workflow.class, "workflow");
    this.workflows = workflows;
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

    String name = null;
    int index = 0;
    if (argArray.length == 2) {
      name = argArray[index++].toString();
    }

    if (!(argArray[index] instanceof Closure)) {
      throw new BuildException(MESSAGE);
    }

    if (workflows.getWorkflows().containsKey(name)) {
      if (name == null) {
        throw new BuildException("A default workflow definition (i.e. without a name specified) already exists. " +
          "You can only define one default workflow");
      } else {
        throw new BuildException("Duplicate workflow definition with the name [" + name + "]");
      }
    }

    Workflow workflow = new Workflow();
    workflows.getWorkflows().put(name, workflow);

    current = workflow;
    Closure closure = (Closure) argArray[index];
    closure.call();
    current = null;
    return workflow;
  }
}
