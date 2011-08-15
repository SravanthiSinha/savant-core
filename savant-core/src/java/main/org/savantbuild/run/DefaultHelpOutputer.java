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

import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.run.output.Level;
import org.savantbuild.run.output.Output;
import org.savantbuild.run.output.OutputWriter;

import com.google.inject.Inject;

/**
 * <p>
 * This is the default help outputer.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultHelpOutputer implements HelpOutputer {
  private final Output output;

  @Inject
  public DefaultHelpOutputer(Output output) {
    this.output = output;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void print(Project project) {
    printHelp("sb", null, Main.options);

    output.info("");
    output.info("------ Main Targets ------");

    Map<String, Target> targets = project.getTargets();
    for (String name : targets.keySet()) {
      Target target = targets.get(name);
      Options options = target.getOptions();
      if (options == null) {
        options = new Options();
      }

      printHelp(name, target.getDescription(), options);
      output.info("");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void print(Target target) {
    printHelp(target.getName(), target.getDescription(), target.getOptions());
  }

  private void printHelp(String command, String header, Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(new PrintWriter(new OutputWriter(output, Level.INFO)), HelpFormatter.DEFAULT_WIDTH, command, header,
      options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null, true);
  }
}
