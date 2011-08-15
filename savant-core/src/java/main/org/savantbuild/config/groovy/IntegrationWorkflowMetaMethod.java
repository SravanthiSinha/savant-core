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

import java.util.List;

import org.savantbuild.BuildException;
import org.savantbuild.domain.Process;
import org.savantbuild.domain.PublishWorkflow;
import org.savantbuild.domain.Workflows;

import groovy.lang.Closure;

/**
 * <p>
 * This class is the Groovy MetaMethod that handles the integration publish workflow definition.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class IntegrationWorkflowMetaMethod extends AbstractMetaMethod {
  // Package protected reference to the current workflow
  static List<Process> current;

  private static final String MESSAGE = "Invalid integrationWorkflow definition. You can optional supply a name for the workflow like this:\n" +
    "  integrationWorkflow(\"name\") {\n" +
    "    process(...)\n" +
    "  }";
  private final Workflows workflows;

  public IntegrationWorkflowMetaMethod(Class theClass, Workflows workflows) {
    super(theClass, PublishWorkflow.class, "integrationWorkflow");
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

    if (workflows.getIntegrationWorkflows().containsKey(name)) {
      if (name == null) {
        throw new BuildException("A default integrationWorkflow definition (i.e. without a name specified) already exists. " +
          "You can only define one default integrationWorkflow");
      } else {
        throw new BuildException("Duplicate integrationWorkflow definition with the name [" + name + "]");
      }
    }

    PublishWorkflow publishWorkflow = new PublishWorkflow();
    workflows.getIntegrationWorkflows().put(name, publishWorkflow);

    current = publishWorkflow.getProcesses();
    Closure closure = (Closure) argArray[index];
    closure.call();
    current = null;
    return publishWorkflow;
  }
}
