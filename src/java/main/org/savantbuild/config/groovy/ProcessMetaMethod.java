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
import java.util.Map;

import org.savantbuild.BuildException;
import org.savantbuild.domain.Process;

/**
 * <p>
 * This class is the Groovy MetaMethod that handles the process information.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ProcessMetaMethod extends AbstractMetaMethod {
  private static final String MESSAGE = "Invalid processes definition. The definition should look like this:\n" +
    "  process(arg1: \"value1\", arg2: \"value2\" ...)";

  public ProcessMetaMethod(Class theClass) {
    super(theClass, Process.class, "process");
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

    if (!(argArray[0] instanceof Map)) {
      throw new BuildException(MESSAGE);
    }

    List<Process> list = findList();
    if (list == null) {
      throw new BuildException("Invalid process definition. It must defined inside a fetchProcesses, publishProcesses, " +
        "integrationWorkflow or releaseWorkflow definition");
    }

    Process process = new Process();
    process.getAttributes().putAll((Map<String, String>) argArray[0]);
    list.add(process);

    return process;
  }

  private List<Process> findList() {
    if (FetchProcessesMetaMethod.current != null) {
      return FetchProcessesMetaMethod.current;
    } else if (PublishProcessesMetaMethod.current != null) {
      return PublishProcessesMetaMethod.current;
    } else if (IntegrationWorkflowMetaMethod.current != null) {
      return IntegrationWorkflowMetaMethod.current;
    } else if (ReleaseWorkflowMetaMethod.current != null) {
      return ReleaseWorkflowMetaMethod.current;
    }

    return null;
  }
}
