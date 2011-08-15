/*
 * Copyright (c) 2001-2011, Inversoft, All Rights Reserved
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
package org.savantbuild.config;

import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Options;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.run.TargetExecutor;

import groovy.lang.Closure;

/**
 * <p>
 * This class is a proxy to a Target instance that provides hooks for invoking targets
 * correctly (i.e. handling dependencies and such).
 * </p>
 *
 * @author Brian Pontarelli
 */
public class TargetProxy {
  private final TargetExecutor targetExecutor;
  private final Project project;
  private final Target target;
  private final String name;

  public TargetProxy(TargetExecutor targetExecutor, Project project, Target target, String name) {
    this.targetExecutor = targetExecutor;
    this.project = project;
    this.target = target;
    this.name = name;
  }

  public String getName() {
    return target.getName();
  }

  public String getDescription() {
    return target.getDescription();
  }

  public void setDescription(String description) {
    target.setDescription(description);
  }

  public Map<String, List<Object>> getParams() {
    return target.getParams();
  }

  public List<String> getDependencies() {
    return target.getDependencies();
  }

  public Options getOptions() {
    return target.getOptions();
  }

  public Deque<Closure> getSteps() {
    return target.getSteps();
  }

  public Deque<Closure> getPre() {
    return target.getPre();
  }

  public Deque<Closure> getPost() {
    return target.getPost();
  }

  public void leftShift(Closure closure) {
    target.leftShift(closure);
  }

  public void run() {
    targetExecutor.run(project, name);
  }

  public void run(Map<String, String> params) {
    targetExecutor.run(project, name, params);
  }
}
