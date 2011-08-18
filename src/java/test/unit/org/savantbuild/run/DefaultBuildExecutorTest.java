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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.codehaus.groovy.runtime.MethodClosure;
import org.savantbuild.config.DefaultConfigurationService;
import org.savantbuild.config.DefaultPluginConfigurationService;
import org.savantbuild.config.DefaultSyntheticTargetFactory;
import org.savantbuild.dep.DefaultDependencyDeleter;
import org.savantbuild.dep.DefaultDependencyManager;
import org.savantbuild.dep.DefaultDependencyPublisher;
import org.savantbuild.dep.DefaultDependencyResolver;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.run.guice.SavantModule;
import org.savantbuild.run.output.Output;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import static org.testng.Assert.*;

/**
 * <p>
 * This class tests the default execution workflow.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultBuildExecutorTest {
  public static StringBuilderOutput output = new StringBuilderOutput();
  private boolean calledJar;
  private Object[] args;
  private boolean calledCompile;

  @BeforeMethod
  public void init() {
    output.build = new StringBuilder();
  }

  @Test
  public void helpGlobal() throws ParseException, IOException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine cli = parser.parse(Main.options, "--help");

    Context context = new Context();
    context.setCli(cli);
    context.setProject(new Project());

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    DefaultBuildExecutor executor = new DefaultBuildExecutor(targetExecutor, new DefaultHelpOutputer(output), output);
    executor.process(context);

    String output = DefaultBuildExecutorTest.output.build.toString();
    assertTrue(output.contains("usage: sb"));
  }

  @Test
  public void helpTargets() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine cli = parser.parse(Main.options, "--help");

    Context context = new Context();
    context.setCli(cli);
    context.setProject(new Project());

    Target params = context.getProject().createTarget("params");
    params.setDescription("testing description");
    params.getParams().put("param1", Arrays.<Object>asList(true, "Parameter help"));

    Target simple = context.getProject().createTarget("simple");
    simple.setDescription("No description");

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    DefaultBuildExecutor executor = new DefaultBuildExecutor(targetExecutor, new DefaultHelpOutputer(output), output);
    executor.process(context);

    String output = DefaultBuildExecutorTest.output.build.toString();
    assertTrue(output.contains("usage: sb"));
    assertTrue(output.contains("usage: simple"));
    assertTrue(output.contains("usage: params"));
    assertTrue(output.contains("param1"));
    assertTrue(output.contains("Parameter help"));
    assertTrue(output.contains("No description"));
  }

  @Test
  public void singleRun() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine cli = parser.parse(Main.options, "compile");

    Context context = new Context();
    context.setCli(cli);
    context.setBuildFile(new File("project.xml"));
    context.setProject(new Project());

    Target target = context.getProject().createTarget("compile");
    target.leftShift(new MethodClosure(this, "invokeCompile"));

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    DefaultBuildExecutor executor = new DefaultBuildExecutor(targetExecutor, new DefaultHelpOutputer(output), output);
    executor.process(context);

    String output = DefaultBuildExecutorTest.output.build.toString();
    assertTrue(output.contains("[compile]"));
    assertTrue(calledCompile);
  }

  @Test
  public void singleRunParams() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine cli = parser.parse(Main.options, "compile", "--set=main");

    Context context = new Context();
    context.setCli(cli);
    context.setBuildFile(new File("project.xml"));
    context.setProject(new Project());

    Target target = context.getProject().createTarget("compile");
    target.getParams().put("set", Arrays.<Object>asList(true, "Compilation set"));
    target.leftShift(new MethodClosure(this, "invokeCompile"));

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    DefaultBuildExecutor executor = new DefaultBuildExecutor(targetExecutor, new DefaultHelpOutputer(output), output);
    executor.process(context);

    String output = DefaultBuildExecutorTest.output.build.toString();
    assertTrue(output.contains("[compile]"));
    assertTrue(calledCompile);
    assertEquals(args.length, 1);
    assertTrue(args[0] instanceof Map);
    assertEquals(((Map) args[0]).get("set"), "main");
  }

  @Test
  public void dependencies() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine cli = parser.parse(Main.options, "jar");

    Context context = new Context();
    context.setCli(cli);
    context.setBuildFile(new File("project.xml"));
    context.setProject(new Project());

    Target compile = context.getProject().createTarget("compile");
    compile.leftShift(new MethodClosure(this, "invokeCompile"));

    Target jar = context.getProject().createTarget("jar");
    jar.getDependencies().add("compile");
    jar.leftShift(new MethodClosure(this, "invokeJar"));

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    DefaultBuildExecutor executor = new DefaultBuildExecutor(targetExecutor, new DefaultHelpOutputer(output), output);
    executor.process(context);

    String output = DefaultBuildExecutorTest.output.build.toString();
    assertTrue(output.contains("[compile]"));
    assertTrue(output.contains("[jar]"));
    assertTrue(calledCompile);
    assertTrue(calledJar);
  }

  @Test
  public void full() throws ParseException {
    Context context = new Context();

    // This is used to initialize the GroovyTools and ArtifactVersionTools classes statically
    Injector injector = Guice.createInjector(new SavantModule(context), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Output.class).toInstance(output);
      }
    });

    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine cli = parser.parse(Main.options, "jar");

    context.setCli(cli);
    context.setBuildFile(new File("src/java/test/unit/org/savantbuild/config/withplugins.savant"));
    context.setWorkflowFile(new File("src/java/test/unit/org/savantbuild/config/localworkflows.savant"));

    DefaultHelpOutputer help = new DefaultHelpOutputer(output);
    DefaultDependencyManager dm = new DefaultDependencyManager(new DefaultDependencyResolver(output),
      new DefaultDependencyPublisher(output), new DefaultDependencyDeleter(output));
    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), help, output);
    DefaultConfigurationService config = new DefaultConfigurationService(dm,
      new DefaultPluginConfigurationService(dm, new DefaultSyntheticTargetFactory(targetExecutor), targetExecutor, output), targetExecutor, output);

    config.configure(context);

    output.build = new StringBuilder();
    DefaultBuildExecutor executor = new DefaultBuildExecutor(targetExecutor, help, output);
    executor.process(context);

    String result = output.build.toString();
    System.out.println("Output is " + result);
    assertEquals(result,
      "\n[compile]\n" +
        "Pre compiling\n" +
        "\n[groovy:compile]\n" +
        "Compiling from Groovy plugin\n" +
        "\n[java:compile]\n" +
        "Pre Java compiling\n" +
        "Compiling from Java plugin\n" +
        "Post Java compiling\n" +
        "Post compiling\n" +
        "\n[jar]\n" +
        "\n[java:jar]\n" +
        "Pre Java jaring\n" +
        "Jaring from Java plugin\n" +
        "Post Java jaring\n" +
        "\n[groovy:jar]\n" +
        "Jaring from Groovy plugin\n"
    );
  }

  /**
   * This is a closure method.
   *
   * @param args The closure parameters.
   */
  public void invokeCompile(Object... args) {
    this.calledCompile = true;
    this.args = args;
  }

  /**
   * This is the closure method.
   *
   * @param args The closure parameters.
   */
  public void invokeJar(Object... args) {
    this.calledJar = true;
    this.args = args;
  }
}
