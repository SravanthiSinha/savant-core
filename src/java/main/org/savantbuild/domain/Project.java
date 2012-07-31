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
package org.savantbuild.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class defines the project.
 *
 * @author Brian Pontarelli
 */
public class Project extends AbstractTargetable {
  private final Map<String, Dependencies> dependencies = new LinkedHashMap<String, Dependencies>();
  private final List<Publication> publications = new ArrayList<Publication>();
  private final Map<String, Plugin> plugins = new LinkedHashMap<String, Plugin>();
  private final Set<String> executedTargets = new HashSet<String>();
  private Workflow workflow;
  private PublishWorkflow integrationWorkflow;
  private PublishWorkflow releaseWorkflow;
  private String name;
  private String group;
  private String version;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Workflow getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Workflow workflow) {
    this.workflow = workflow;
  }

  public PublishWorkflow getIntegrationWorkflow() {
    return integrationWorkflow;
  }

  public void setIntegrationWorkflow(PublishWorkflow integrationWorkflow) {
    this.integrationWorkflow = integrationWorkflow;
  }

  public PublishWorkflow getReleaseWorkflow() {
    return releaseWorkflow;
  }

  public void setReleaseWorkflow(PublishWorkflow releaseWorkflow) {
    this.releaseWorkflow = releaseWorkflow;
  }

  public Map<String, Dependencies> getDependencies() {
    return dependencies;
  }

  public List<Publication> getPublications() {
    return publications;
  }

  public Map<String, Plugin> getPlugins() {
    return plugins;
  }

  public Set<String> getExecutedTargets() {
    return executedTargets;
  }
}
