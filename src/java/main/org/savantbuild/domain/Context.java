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

import java.io.File;

import org.apache.commons.cli.CommandLine;

/**
 * This is the main context that is created when the build starts and passed through the entire build process.
 *
 * @author Brian Pontarelli
 */
public class Context {
  private Project project;
  private Workflows workflows;
  private CommandLine cli;
  private File buildFile = new File("build.savant");
  private File workflowFile = new File(System.getProperty("user.home") + "/.savant/workflows.savant");
  private File pluginCacheDirectory = new File(System.getProperty("user.home") + "/.savant/plugins");

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public Workflows getWorkflows() {
    return workflows;
  }

  public void setWorkflows(Workflows workflows) {
    this.workflows = workflows;
  }

  public CommandLine getCli() {
    return cli;
  }

  public void setCli(CommandLine cli) {
    this.cli = cli;
  }

  public File getBuildFile() {
    return buildFile;
  }

  public void setBuildFile(File buildFile) {
    this.buildFile = buildFile;
  }

  public File getWorkflowFile() {
    return workflowFile;
  }

  public void setWorkflowFile(File workflowFile) {
    this.workflowFile = workflowFile;
  }

  public File getPluginCacheDirectory() {
    return pluginCacheDirectory;
  }

  public void setPluginCacheDirectory(File pluginCacheDirectory) {
    this.pluginCacheDirectory = pluginCacheDirectory;
  }
}
