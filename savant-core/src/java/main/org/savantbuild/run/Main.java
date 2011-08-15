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

import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.savantbuild.BuildException;
import org.savantbuild.config.ConfigurationService;
import org.savantbuild.domain.Context;
import org.savantbuild.run.guice.SavantModule;
import org.savantbuild.run.output.Level;
import org.savantbuild.run.output.Output;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * <p>
 * This class is the main entry point for Savant.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Main {
  public final static Options options = new Options();

  static {
    options.addOption("d", "debug", false, "turns on debugging output");
    options.addOption("f", "file", true, "the build file (defaults to build.savant)");
    options.addOption("w", "workflows", true, "the workflows file (defaults to ~/.savant/workflows.savant)");
    options.addOption("v", "version", false, "print the version of Savant");
    options.addOption("h", "help", false, "print this help message");
  }

  public static void main(String... args) {
    Context context = new Context();
    Injector injector = Guice.createInjector(new SavantModule(context));
    ArgumentParser parser = injector.getInstance(ArgumentParser.class);
    CommandLine cli = null;
    try {
      cli = parser.parse(options, args);
    } catch (ParseException e) {
      Logger.getLogger("savant").severe(e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("sb", options, true);
      System.exit(1);
    }

    Output output = injector.getInstance(Output.class);
    boolean debug = cli.hasOption("debug");
    if (debug) {
      output.setLevel(Level.DEBUG);
    }

    try {
      context.setCli(cli);

      output.info("Initializing Savant");
      ConfigurationService service = injector.getInstance(ConfigurationService.class);
      service.configure(context);

      if (context.getProject() != null) {
        output.info("Running build for [" + context.getProject().getGroup() + ":" + context.getProject().getName() +
          ":" + context.getProject().getVersion() + "]");
      }

      BuildExecutor execution = injector.getInstance(BuildExecutor.class);
      execution.process(context);
    } catch (Exception e) {
      System.err.println();
      System.err.println();
      System.err.println("BUILD FAILED");

      String fileName = null;
      if (e instanceof BuildException) {
        fileName = ((BuildException) e).getFileName();
      }

      StackTraceElement[] elements = e.getStackTrace();
      boolean found = false;
      for (StackTraceElement element : elements) {
        if (element.getClassName().startsWith("build")) {
          System.err.println("\tFILE: " + (fileName != null ? fileName : "build.savant"));
          System.err.println("\tLOCATION: line " + element.getLineNumber());
          System.err.println("\tREASON: " + e.getMessage().replace("\n", "\n\t        "));
          found = true;
          break;
        } else if (element.getClassName().startsWith("plugin")) {
          System.err.println("\tFILE: " + (fileName != null ? fileName : "plugin.savant"));
          System.err.println("\tLOCATION: line " + element.getLineNumber());
          System.err.println("\tREASON: " + e.getMessage().replace("\n", "\n\t        "));
          found = true;
          break;
        } else if (element.getClassName().startsWith("workflows")) {
          System.err.println("\tFILE: " + (fileName != null ? fileName : "workflow.savant"));
          System.err.println("\tLOCATION: line " + element.getLineNumber());
          System.err.println("\tREASON: " + e.getMessage().replace("\n", "\n\t        "));
          found = true;
          break;
        }
      }

      if (!found) {
        System.err.println(e.getMessage());
      }

      if (debug) {
        e.printStackTrace();
      }

      System.exit(1);
    }
  }
}
