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
package org.savantbuild.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class stores all of the workflows.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Workflows {
  private final Map<String, Workflow> workflows = new HashMap<String, Workflow>();
  private final Map<String, PublishWorkflow> integrationWorkflows = new HashMap<String, PublishWorkflow>();
  private final Map<String, PublishWorkflow> releaseWorkflows = new HashMap<String, PublishWorkflow>();

  public Map<String, Workflow> getWorkflows() {
    return workflows;
  }

  public Map<String, PublishWorkflow> getIntegrationWorkflows() {
    return integrationWorkflows;
  }

  public Map<String, PublishWorkflow> getReleaseWorkflows() {
    return releaseWorkflows;
  }
}
