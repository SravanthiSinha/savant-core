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

import groovy.lang.Closure;

/**
 * This class is the Groovy MetaMethod that handles the publish processes information.
 *
 * @author Brian Pontarelli
 */
public class PublishProcessesMetaMethod extends AbstractMetaMethod {
  // Package protected reference to the current workflow
  static List<Process> current;

  private static final String MESSAGE = "Invalid publishProcesses definition. The definition should look like this:\n" +
    "  publishProcesses {\n" +
    "    process(...)\n" +
    "  }";

  public PublishProcessesMetaMethod(Class theClass) {
    super(theClass, List.class, "publishProcesses");
  }

  @Override
  public Object invoke(Object o, Object[] arguments) {
    if (arguments.length != 1) {
      throw new BuildException(MESSAGE);
    }

    Object[] argArray = (Object[]) arguments[0];
    if (argArray == null || argArray.length != 1) {
      throw new BuildException(MESSAGE);
    }

    if (!(argArray[0] instanceof Closure)) {
      throw new BuildException(MESSAGE);
    }

    if (WorkflowMetaMethod.current == null) {
      throw new BuildException("Invalid publishProcesses definition. It must defined inside a workflow definition");
    }

    current = WorkflowMetaMethod.current.getPublishProcesses();
    Closure closure = (Closure) argArray[0];
    closure.call();
    current = null;
    return WorkflowMetaMethod.current.getPublishProcesses();
  }
}
