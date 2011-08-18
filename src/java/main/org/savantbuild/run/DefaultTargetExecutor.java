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
package org.savantbuild.run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.savantbuild.BuildException;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.domain.Targetable;
import org.savantbuild.run.output.Output;

import com.google.inject.Inject;
import groovy.lang.Closure;

/**
 * <p>
 * This class is the default target executor. It handles local and plugin targets, target
 * dependencies, target argument parsing, target help output.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultTargetExecutor implements TargetExecutor {
  private final ArgumentParser parser;
  private final HelpOutputer helpOutputer;
  private final Output output;

  @Inject
  public DefaultTargetExecutor(ArgumentParser parser, HelpOutputer helpOutputer, Output output) {
    this.parser = parser;
    this.helpOutputer = helpOutputer;
    this.output = output;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(Project project, String name) {
    run(project, name, new ArrayList<String>());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(Project project, String name, List<String> arguments) {
    Target target = findTarget(project, name);

    // Call the target
    Map<String, String> parameterValues = new HashMap<String, String>();
    if (arguments.size() > 0) {
      Options options = target.getOptions();
      CommandLine cli = null;
      try {
        cli = parser.parseTarget(options, arguments.toArray(new String[arguments.size()]));
        for (Option option : cli.getOptions()) {
          parameterValues.put(option.getLongOpt(), option.getValue());
        }
      } catch (ParseException e) {
        output.failure("Invalid parameter passed to the target [" + name + "]");
        output.failure(e.getMessage());
        helpOutputer.print(target);
        System.exit(1);
      }

      if (cli.hasOption("help")) {
        helpOutputer.print(target);
        return;
      }
    }

    run(project, name, parameterValues);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(Project project, String name, Map<String, String> arguments) {
    if (project.getExecutedTargets().contains(name)) {
      return;
    }

    Target target = findTarget(project, name);

    // Call the dependencies
    List<String> dependencies = target.getDependencies();
    for (String dependency : dependencies) {
      run(project, dependency, arguments);
    }

    output.info("\n[" + name + "]");
    for (Closure step : target.getPre()) {
      step.call(arguments);
    }
    for (Closure step : target.getSteps()) {
      step.call(arguments);
    }
    for (Closure step : target.getPost()) {
      step.call(arguments);
    }
    project.getExecutedTargets().add(name);
  }

  private Target findTarget(Project project, String name) {
    String targetName = name;
    Targetable targetable = project;

    int index = name.indexOf(":");
    if (index > 0) {
      String pluginName = name.substring(0, index);
      targetName = name.substring(index + 1);
      targetable = project.getPlugins().get(pluginName);
      if (targetable == null) {
        throw new BuildException("Invalid plugin [" + pluginName + "] for the target call [" + name + "].");
      }
    }

    Target target = targetable.lookupTarget(targetName);
    if (target == null) {
      throw new BuildException("Invalid target [" + name + "].");
    }

    return target;
  }
}
