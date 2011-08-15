/*
 * Copyright (c) 2008, Inversoft, All Rights Reserved.
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
package org.savantbuild.dep.workflow;

import java.util.List;

import org.savantbuild.dep.workflow.process.ProcessHandler;
import org.savantbuild.dep.workflow.process.ProcessHandlerFactory;
import org.savantbuild.domain.Process;
import org.savantbuild.domain.Workflow;
import org.savantbuild.run.output.Output;

/**
 * <p>
 * This class models a grouping of a fetch and publish workflow.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class WorkflowHandler {
  public static WorkflowHandler build(Workflow workflow, Output output) {
    List<Process> processes = workflow.getFetchProcesses();
    FetchWorkflowHandler fetch = new FetchWorkflowHandler(output);
    for (Process process : processes) {
      ProcessHandler fetchProcess = ProcessHandlerFactory.buildProcess(output, process);
      fetch.getProcesses().add(fetchProcess);
    }

    processes = workflow.getPublishProcesses();
    PublishWorkflowHandler publish = new PublishWorkflowHandler();
    for (Process process : processes) {
      ProcessHandler publishProcess = ProcessHandlerFactory.buildProcess(output, process);
      publish.getProcesses().add(publishProcess);
    }

    return new WorkflowHandler(fetch, publish);
  }

  private final FetchWorkflowHandler fetchWorkflowHandler;
  private final PublishWorkflowHandler publishWorkflowHandler;

  public WorkflowHandler(FetchWorkflowHandler fetchWorkflowHandler, PublishWorkflowHandler publishWorkflowHandler) {
    this.fetchWorkflowHandler = fetchWorkflowHandler;
    this.publishWorkflowHandler = publishWorkflowHandler;
  }

  public FetchWorkflowHandler getFetchWorkflowHandler() {
    return fetchWorkflowHandler;
  }

  public PublishWorkflowHandler getPublishWorkflowHandler() {
    return publishWorkflowHandler;
  }
}
