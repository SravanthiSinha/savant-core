/*
 * Copyright (c) 2008, Inversoft, All Rights Reserved.
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
package org.savantbuild.dep.cli;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.savantbuild.BuildException;
import org.savantbuild.config.ConfigurationService;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Workflow;
import org.savantbuild.run.guice.SavantModule;
import org.savantbuild.run.output.Level;
import org.savantbuild.run.output.Output;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * <p>
 * This class provides a command-line entry point for loading the
 * savant project.xml file and outputting the projects classpath.
 * This main takes these parameters:
 * </p>
 * <p/>
 * <pre>
 * --types &lt;types>:      A comma separated list of artifact group types to include
 *                          in the path. For example --types "run,compile". This defaults
 *                          to all the types.
 *
 * --dependencies &lt;id>:  The id of dependencies to use when building the path.
 *                          This defaults to the projects default dependencies list,
 *                          which if there is only one, is that one.
 * </pre>
 *
 * @author Brian Pontarelli
 */
public class DependencyPathMain {
  public static void main(String[] args) {
    Set<String> types = new HashSet<String>();
    String dependenciesId = null;
    if (args.length > 0) {
      if (args.length % 2 != 0) {
        usage();
      }

      for (int i = 0; i < args.length; i = i + 2) {
        if (args[i].equals("--types")) {
          String[] array = args[i + 1].split(",");
          types.addAll(Arrays.asList(array));
        } else if (args[i].equals("--dependencies")) {
          dependenciesId = args[i + 1];
        } else {
          usage();
        }
      }
    }

    Context context = new Context();
    Injector injector = Guice.createInjector(new SavantModule(context), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Output.class).toInstance(new NoOpOutput());
      }
    });

    ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);
    configurationService.configure(context);

    Dependencies deps = (dependenciesId == null) ? context.getProject().getDependencies().get(null) :
      context.getProject().getDependencies().get(dependenciesId);

    DependencyManager manager = injector.getInstance(DependencyManager.class);
    Workflow workflow = manager.determineProjectWorkflow(context.getWorkflows(), context.getProject());

    StringBuilder build = new StringBuilder();
    try {
      Map<Artifact, File> files = manager.getResolver().resolve(deps, workflow, types, true);
      for (File file : files.values()) {
        if (build.length() > 0) {
          build.append(File.pathSeparator);
        }

        build.append(file.getAbsolutePath());
      }
    } catch (BuildException e) {
      System.err.println("Unable to build dependency path");
      e.printStackTrace();
      System.exit(1);
    }

    System.out.println(build.toString());
  }

  private static void usage() {
    System.err.println("Usage: DependencyPathMain [options]");
    System.err.println("");
    System.err.println("Options\n" +
      " --types &lt;types>:      A comma separated list of artifact group types to include\n" +
      "                          in the path. For example --types \"run,compile\". This defaults\n" +
      "                          to all the types.\n" +
      "\n" +
      " --dependencies &lt;id>:  The id of dependencies to use when building the path.\n" +
      "                          This defaults to the projects default dependencies list,\n" +
      "                          which if there is only one, is that one.");
    System.exit(1);
  }

  private static class NoOpOutput implements Output {
    @Override
    public void debug(String message) {
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void warning(String message) {
    }

    @Override
    public void failure(String message) {
    }

    @Override
    public void print(Level level, String message) {
    }

    @Override
    public void println(Level level, String message) {
    }

    @Override
    public void println(Level level, Throwable t) {
    }

    @Override
    public void println(Level level, String message, Throwable t) {
    }

    @Override
    public Level getLevel() {
      return null;
    }

    @Override
    public void setLevel(Level level) {
    }
  }
}
