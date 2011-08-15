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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.PosixParser;
import org.codehaus.groovy.runtime.MethodClosure;
import org.savantbuild.dep.DefaultDependencyDeleter;
import org.savantbuild.dep.DefaultDependencyManager;
import org.savantbuild.dep.DefaultDependencyPublisher;
import org.savantbuild.dep.DefaultDependencyResolver;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Plugin;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.domain.Workflow;
import org.savantbuild.io.FileTools;
import org.savantbuild.run.DefaultArgumentParser;
import org.savantbuild.run.DefaultHelpOutputer;
import org.savantbuild.run.DefaultTargetExecutor;
import org.savantbuild.run.StringBuilderOutput;
import org.savantbuild.run.TargetExecutor;
import org.savantbuild.run.guice.SavantModule;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.GroovyTools;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import static org.savantbuild.TestTools.*;
import static org.testng.Assert.*;

/**
 * <p>
 * This tests the default plugin configuration service.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultPluginConfigurationServiceTest {
  public StringBuilderOutput output = new StringBuilderOutput();
  public Context context;

  @BeforeMethod
  public void setupGuice() {
    context = new Context();
    GroovyTools.initialize(Guice.createInjector(new SavantModule(context) {
      @Override
      protected void configure() {
        super.configure();
        bind(Output.class).toInstance(output);
      }
    }));
  }

  @Test
  public void javaPlugin() throws Exception {
    FileTools.prune(new File("target/test/deps"));
    FileTools.prune(new File("target/test/plugins"));

    context.setPluginCacheDirectory(new File("target/test/plugins"));
    context.setProject(new Project());

    Project project = context.getProject();

    Workflow w = new Workflow();
    w.getFetchProcesses().add(new org.savantbuild.domain.Process(map("type", "url", "url", new File("test-deps/plugins").toURI().toURL().toString())));
    w.getPublishProcesses().add(new org.savantbuild.domain.Process(map("type", "cache", "dir", "target/test/deps")));
    project.setWorkflow(w);

    DependencyManager manager = new DefaultDependencyManager(new DefaultDependencyResolver(output), new DefaultDependencyPublisher(output), new DefaultDependencyDeleter(output));
    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    SyntheticTargetFactory factory = new DefaultSyntheticTargetFactory(targetExecutor);
    DefaultPluginConfigurationService service = new DefaultPluginConfigurationService(manager, factory, targetExecutor, output);

    Plugin plugin = service.loadPlugin(context, project, new Artifact("org.savantbuild.plugins", "java", "java", "1.0", "jar"), new HashMap());
    assertNotNull(plugin);
    assertEquals(plugin.getName(), "java");
    assertEquals(plugin.getTargets().size(), 3);
    assertEquals(plugin.getTargets().get("compile").getSteps().size(), 1);

    File file = new File("target/test/plugins/org/savantbuild/plugins/java/1.0/plugin.savant");
    assertTrue(file.isFile());
  }

  @Test
  public void settings() throws Exception {
    FileTools.prune(new File("target/test/deps"));
    FileTools.prune(new File("target/test/plugins"));

    context.setPluginCacheDirectory(new File("target/test/plugins"));
    context.setProject(new Project());

    Project project = context.getProject();

    Workflow w = new Workflow();
    w.getFetchProcesses().add(new org.savantbuild.domain.Process(map("type", "url", "url", new File("test-deps/plugins").toURI().toURL().toString())));
    w.getPublishProcesses().add(new org.savantbuild.domain.Process(map("type", "cache", "dir", "target/test/deps")));
    project.setWorkflow(w);

    DependencyManager manager = new DefaultDependencyManager(new DefaultDependencyResolver(output), new DefaultDependencyPublisher(output), new DefaultDependencyDeleter(output));
    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    SyntheticTargetFactory factory = new DefaultSyntheticTargetFactory(targetExecutor);
    DefaultPluginConfigurationService service = new DefaultPluginConfigurationService(manager, factory, targetExecutor, output);

    AtomicBoolean container = new AtomicBoolean(false);
    Map settings = new HashMap();
    settings.put("container", container);

    Plugin plugin = service.loadPlugin(context, project, new Artifact("org.savantbuild.plugins", "settings", "settings", "1.0", "jar"), settings);
    assertTrue(container.get());
    assertNotNull(plugin);
    assertEquals(plugin.getName(), "settings");
    assertEquals(plugin.getTargets().size(), 1);
    assertEquals(plugin.getTargets().get("compile").getSteps().size(), 1);

    File file = new File("target/test/plugins/org/savantbuild/plugins/settings/1.0/plugin.savant");
    assertTrue(file.isFile());
  }

  @Test
  public void integration() throws Exception {
    FileTools.prune(new File("target/test/deps"));
    FileTools.prune(new File("target/test/plugins"));

    testIntegrationSingle();
    testIntegrationSingle();
  }

  private void testIntegrationSingle() throws Exception {
    context.setPluginCacheDirectory(new File("target/test/plugins"));
    context.setProject(new Project());

    Project project = context.getProject();

    Workflow w = new Workflow();
    w.getFetchProcesses().add(new org.savantbuild.domain.Process(map("type", "url", "url", new File("test-deps/plugins").toURI().toURL().toString())));
    w.getPublishProcesses().add(new org.savantbuild.domain.Process(map("type", "cache", "dir", "target/test/deps")));
    project.setWorkflow(w);

    DependencyManager manager = new DefaultDependencyManager(new DefaultDependencyResolver(output), new DefaultDependencyPublisher(output), new DefaultDependencyDeleter(output));
    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    SyntheticTargetFactory factory = new DefaultSyntheticTargetFactory(targetExecutor);
    DefaultPluginConfigurationService service = new DefaultPluginConfigurationService(manager, factory, targetExecutor, output);

    Plugin plugin = service.loadPlugin(context, project, new Artifact("org.savantbuild.plugins", "integration", "integration", "1.0-{integration}", "jar"), new HashMap());
    assertNotNull(plugin);
    assertEquals(plugin.getTargets().size(), 3);
    assertEquals(plugin.getTargets().get("compile").getSteps().size(), 1);

    File file = new File("target/test/plugins/org/savantbuild/plugins/integration/1.0-{integration}/plugin.savant");
    assertTrue(file.isFile());
  }

  @Test
  public void addDefaultTargets() throws Exception {
    Project project = new Project();
    Target compile = new Target("compile");
    project.getTargets().put("compile", compile);
    project.getTargets().get("compile").leftShift(new MethodClosure(this, "foo"));
    project.getTargets().get("compile").getPre().add(new MethodClosure(this, "foo"));
    project.getTargets().get("compile").getPost().add(new MethodClosure(this, "foo"));

    Target jar = new Target("jar");
    project.getTargets().put("jar", jar);
    project.getTargets().get("jar").getPre().add(new MethodClosure(this, "foo"));
    project.getTargets().get("jar").getPost().add(new MethodClosure(this, "foo"));

    project.getPlugins().put("java", new Plugin());
    project.getPlugins().get("java").getTargets().put("compile", new Target("compile"));
    project.getPlugins().get("java").getTargets().put("jar", new Target("jar"));

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    SyntheticTargetFactory factory = new DefaultSyntheticTargetFactory(targetExecutor);
    DefaultPluginConfigurationService service = new DefaultPluginConfigurationService(null, factory, targetExecutor, output);
    service.addDefaultTargets(project);

    assertSame(project.getTargets().get("compile"), compile);
    assertNotNull(project.getTargets().get("jar"));
  }

  @Test
  public void defaultTargetsWithDependencies() throws Exception {
    Project project = new Project();

    Target javaJar = new Target("jar");
    javaJar.getDependencies().add("compile");

    Target groovyJar = new Target("jar");
    groovyJar.getDependencies().add("compile");

    project.getPlugins().put("java", new Plugin());
    project.getPlugins().put("groovy", new Plugin());
    project.getPlugins().get("java").getTargets().put("jar", javaJar);
    project.getPlugins().get("groovy").getTargets().put("jar", groovyJar);

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    SyntheticTargetFactory factory = new DefaultSyntheticTargetFactory(targetExecutor);
    DefaultPluginConfigurationService service = new DefaultPluginConfigurationService(null, factory, targetExecutor, output);
    service.addDefaultTargets(project);

    assertEquals(project.getTargets().get("jar").getDependencies().get(0), "compile");
  }

  // For closure
  public void foo() {
  }
}
