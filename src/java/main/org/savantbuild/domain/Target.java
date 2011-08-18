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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Options;

import groovy.lang.Closure;

/**
 * <p>
 * This class defines a target within the build file.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Target {
  private final Deque<Closure> preSteps = new ArrayDeque<Closure>();
  private final Deque<Closure> steps = new ArrayDeque<Closure>();
  private final Deque<Closure> postSteps = new ArrayDeque<Closure>();
  private final Map<String, List<Object>> params = new HashMap<String, List<Object>>();
  private final List<String> dependencies = new ArrayList<String>();
  private final String name;
  private String description;

  public Target(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, List<Object>> getParams() {
    return params;
  }

  public List<String> getDependencies() {
    return dependencies;
  }

  public Options getOptions() {
    if (params == null || params.isEmpty()) {
      return null;
    }

    Options options = new Options();
    for (String param : params.keySet()) {
      List<Object> values = params.get(param);
      options.addOption(null, param, (Boolean) values.get(0), (String) values.get(1));
    }
    return options;
  }

  public Deque<Closure> getSteps() {
    return steps;
  }

  public Deque<Closure> getPre() {
    return preSteps;
  }

  public Deque<Closure> getPost() {
    return postSteps;
  }

  public void leftShift(Closure closure) {
    steps.addLast(closure);
  }
}
