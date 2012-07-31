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

import org.savantbuild.domain.Workflows;

import groovy.lang.MetaClassImpl;

/**
 * This is the meta class that supports the Savant Workflows DSL.
 *
 * @author Brian Pontarelli
 */
public class WorkflowsMetaClass extends MetaClassImpl {
  public WorkflowsMetaClass(final Class<?> theClass, Workflows workflows) {
    super(theClass);
    super.addMetaMethod(new WorkflowMetaMethod(theClass, workflows));
    super.addMetaMethod(new IntegrationWorkflowMetaMethod(theClass, workflows));
    super.addMetaMethod(new ReleaseWorkflowMetaMethod(theClass, workflows));
    super.addMetaMethod(new FetchProcessesMetaMethod(theClass));
    super.addMetaMethod(new PublishProcessesMetaMethod(theClass));
    super.addMetaMethod(new ProcessMetaMethod(theClass));
  }
}
