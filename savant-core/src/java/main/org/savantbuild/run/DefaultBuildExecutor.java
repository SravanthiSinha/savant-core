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
package org.savantbuild.run;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.savantbuild.BuildException;
import org.savantbuild.domain.Context;
import org.savantbuild.run.output.Output;

import com.google.inject.Inject;

/**
 * <p>
 * This is the build executor for Savant. It determines which targets to call and then invokes them.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultBuildExecutor implements BuildExecutor {
  private final TargetExecutor targetExecutor;
  private final HelpOutputer helpOutputer;
  private final Output output;

  @Inject
  public DefaultBuildExecutor(TargetExecutor targetExecutor, HelpOutputer helpOutputer, Output output) {
    this.targetExecutor = targetExecutor;
    this.helpOutputer = helpOutputer;
    this.output = output;
  }

  @Override
  public void process(Context context) {
    output.debug("Starting build execution");

    CommandLine cli = context.getCli();

    if (cli.hasOption("help")) {
      helpOutputer.print(context.getProject());
    } else if (cli.hasOption("version")) {
      output.info("Savant Build System Version " + Main.class.getPackage().getImplementationVersion());
    } else {
      if (!context.getBuildFile().isFile()) {
        throw new BuildException("Invalid or missing build script [" + context.getBuildFile().toString() + "]");
      }

      List<String> targetCallArgs = new ArrayList<String>();
      String target = null;
      for (String arg : cli.getArgs()) {
        if (arg.startsWith("-")) {
          targetCallArgs.add(arg);
        } else {
          if (target != null) {
            output.debug("Executing target [" + target + "]");
            targetExecutor.run(context.getProject(), target, targetCallArgs);
          }

          target = arg;
        }
      }

      if (target != null) {
        output.debug("Executing target [" + target + "]");
        targetExecutor.run(context.getProject(), target, targetCallArgs);
      }
    }
  }
}
