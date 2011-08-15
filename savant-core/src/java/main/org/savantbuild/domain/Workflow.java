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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This class defines a workflow process that is used for dependency management.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Workflow {
  private final List<Process> fetchProcesses = new ArrayList<Process>();
  private final List<Process> publishProcesses = new ArrayList<Process>();

  public List<Process> getFetchProcesses() {
    return fetchProcesses;
  }

  public List<Process> getPublishProcesses() {
    return publishProcesses;
  }
}
