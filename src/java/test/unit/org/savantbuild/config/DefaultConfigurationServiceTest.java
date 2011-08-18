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
package org.savantbuild.config;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.PosixParser;
import org.savantbuild.BuildException;
import org.savantbuild.dep.DefaultDependencyDeleter;
import org.savantbuild.dep.DefaultDependencyManager;
import org.savantbuild.dep.DefaultDependencyPublisher;
import org.savantbuild.dep.DefaultDependencyResolver;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Publication;
import org.savantbuild.domain.Workflows;
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
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * <p>
 * This tests the default configuration workflow.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultConfigurationServiceTest {
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
  public void verifyDefaultWorkflow() {
    PluginConfigurationService pcs = createStrictMock(PluginConfigurationService.class);
    pcs.addDefaultTargets(isA(Project.class));
    replay(pcs);

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    DefaultConfigurationService service = new DefaultConfigurationService(new DefaultDependencyManager(null, null, null), pcs, targetExecutor, output);

    context.setBuildFile(new File("src/java/test/unit/org/savantbuild/config/simpletest.savant"));
    context.setWorkflowFile(new File("use-default-workflows"));
    service.configure(context);

    Workflows workflows = context.getWorkflows();
    assertEquals(workflows.getIntegrationWorkflows().size(), 1);
    assertEquals(workflows.getIntegrationWorkflows().get(null).getProcesses().size(), 1);
    assertEquals(workflows.getIntegrationWorkflows().get(null).getProcesses().get(0).getAttributes().get("type"), "cache");
    assertEquals(workflows.getWorkflows().size(), 1);
    assertEquals(workflows.getWorkflows().get(null).getFetchProcesses().size(), 2);
    assertEquals(workflows.getWorkflows().get(null).getFetchProcesses().get(0).getAttributes().get("type"), "cache");
    assertEquals(workflows.getWorkflows().get(null).getFetchProcesses().get(1).getAttributes().get("type"), "url");
    assertEquals(workflows.getWorkflows().get(null).getFetchProcesses().get(1).getAttributes().get("url"), "http://repository.savantbuild.org");
    assertEquals(workflows.getWorkflows().get(null).getPublishProcesses().size(), 1);
    assertEquals(workflows.getWorkflows().get(null).getPublishProcesses().get(0).getAttributes().get("type"), "cache");
    assertEquals(workflows.getReleaseWorkflows().size(), 0);

    verify(pcs);
  }

  @Test
  public void configure() {
    PluginConfigurationService pcs = createStrictMock(PluginConfigurationService.class);
    pcs.addDefaultTargets(isA(Project.class));
    replay(pcs);

    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    DefaultConfigurationService service = new DefaultConfigurationService(new DefaultDependencyManager(null, null, null), pcs, targetExecutor, output);

    context.setBuildFile(new File("src/java/test/unit/org/savantbuild/config/simpletest.savant"));
    context.setWorkflowFile(new File("src/java/test/unit/org/savantbuild/config/workflows.savant"));
    service.configure(context);

    Project project = context.getProject();
    assertEquals(project.getGroup(), "org.savantbuild.test");
    assertEquals(project.getName(), "savant-core-test");
    assertEquals(project.getVersion(), "2.0-A1");

    List<Publication> publications = project.getPublications();
    assertEquals(publications.size(), 2);
    assertEquals(publications.get(0).getCompatibility(), "patch");
    assertEquals(publications.get(0).getDependencies(), "deps");
    assertEquals(publications.get(0).getFile(), "target/jars/savant-core-test.jar");
    assertEquals(publications.get(0).getName(), "publication1");
    assertEquals(publications.get(0).getType(), "jar");
    assertNull(publications.get(1).getCompatibility());
    assertNull(publications.get(1).getDependencies());
    assertEquals(publications.get(1).getFile(), "target/jars/savant-core-test.xml");
    assertEquals(publications.get(1).getName(), "publication2");
    assertEquals(publications.get(1).getType(), "xml");

    Dependencies dependencies = project.getDependencies().get(null);
    assertEquals(dependencies.getArtifactGroups().size(), 1);

    ArtifactGroup artifactGroup = dependencies.getArtifactGroups().get("compile");
    assertEquals(artifactGroup.getArtifacts().size(), 1);

    Artifact artifact = artifactGroup.getArtifacts().get(0);
    assertEquals(artifact.getGroup(), "org.apache.commons");
    assertEquals(artifact.getProject(), "commons-io");
    assertEquals(artifact.getName(), "commons-io");
    assertEquals(artifact.getVersion(), "1.0");

    assertNotNull(project.lookupTarget("simple"));
    assertNull(project.lookupTarget("bad"));
    assertNotNull(project.lookupTarget("params"));

    Workflows workflows = context.getWorkflows();
    assertEquals(workflows.getWorkflows().size(), 2);
    assertEquals(workflows.getWorkflows().get(null).getFetchProcesses().size(), 2);
    assertEquals(workflows.getWorkflows().get(null).getFetchProcesses().get(0).getAttributes().get("type"), "cache");
    assertEquals(workflows.getWorkflows().get(null).getFetchProcesses().get(1).getAttributes().get("type"), "url");
    assertEquals(workflows.getWorkflows().get(null).getFetchProcesses().get(1).getAttributes().get("url"), "http://repository.savantbuild.org");
    assertEquals(workflows.getWorkflows().get(null).getPublishProcesses().size(), 1);
    assertEquals(workflows.getWorkflows().get(null).getPublishProcesses().get(0).getAttributes().get("type"), "cache");

    assertEquals(workflows.getWorkflows().get("com.example").getFetchProcesses().size(), 3);
    assertEquals(workflows.getWorkflows().get("com.example").getFetchProcesses().get(0).getAttributes().get("type"), "cache");
    assertEquals(workflows.getWorkflows().get("com.example").getFetchProcesses().get(1).getAttributes().get("type"), "url");
    assertEquals(workflows.getWorkflows().get("com.example").getFetchProcesses().get(1).getAttributes().get("url"), "http://repository.savantbuild.org");
    assertEquals(workflows.getWorkflows().get("com.example").getFetchProcesses().get(2).getAttributes().get("type"), "url");
    assertEquals(workflows.getWorkflows().get("com.example").getFetchProcesses().get(2).getAttributes().get("url"), "https://secure.mycompany.com");
    assertEquals(workflows.getWorkflows().get("com.example").getFetchProcesses().get(2).getAttributes().get("username"), "frank");
    assertEquals(workflows.getWorkflows().get("com.example").getFetchProcesses().get(2).getAttributes().get("password"), "sinatra");
    assertEquals(workflows.getWorkflows().get("com.example").getPublishProcesses().size(), 1);
    assertEquals(workflows.getWorkflows().get("com.example").getPublishProcesses().get(0).getAttributes().get("type"), "cache");

    assertEquals(workflows.getIntegrationWorkflows().size(), 1);
    assertEquals(workflows.getIntegrationWorkflows().get(null).getProcesses().size(), 1);
    assertEquals(workflows.getIntegrationWorkflows().get(null).getProcesses().get(0).getAttributes().get("type"), "cache");

    assertEquals(workflows.getReleaseWorkflows().size(), 2);
    assertEquals(workflows.getReleaseWorkflows().get("org.opensource").getProcesses().size(), 1);
    assertEquals(workflows.getReleaseWorkflows().get("org.opensource").getProcesses().get(0).getAttributes().get("type"), "svn");
    assertEquals(workflows.getReleaseWorkflows().get("org.opensource").getProcesses().get(0).getAttributes().get("repository"), "https://svn.savantbuild.org/savant-repository");
    assertEquals(workflows.getReleaseWorkflows().get("org.opensource").getProcesses().get(0).getAttributes().get("username"), "frank");
    assertEquals(workflows.getReleaseWorkflows().get("org.opensource").getProcesses().get(0).getAttributes().get("password"), "sinatra");
    assertEquals(workflows.getReleaseWorkflows().get("com.example").getProcesses().get(0).getAttributes().get("type"), "svn");
    assertEquals(workflows.getReleaseWorkflows().get("com.example").getProcesses().get(0).getAttributes().get("repository"), "https://svn.mycompany.com/savant-repository");
    assertEquals(workflows.getReleaseWorkflows().get("com.example").getProcesses().get(0).getAttributes().get("username"), "frank");
    assertEquals(workflows.getReleaseWorkflows().get("com.example").getProcesses().get(0).getAttributes().get("password"), "sinatra");

    assertSame(project.getWorkflow(), workflows.getWorkflows().get(null));
    assertSame(project.getIntegrationWorkflow(), workflows.getIntegrationWorkflows().get(null));
    assertNull(project.getReleaseWorkflow());

    verify(pcs);
  }

  @Test
  public void withPlugins() {
    FileTools.prune(new File("target/test/deps"));
    FileTools.prune(new File("target/test/plugins"));

    DependencyManager dm = new DefaultDependencyManager(new DefaultDependencyResolver(output),
      new DefaultDependencyPublisher(output), new DefaultDependencyDeleter(output));
    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    SyntheticTargetFactory factory = new DefaultSyntheticTargetFactory(targetExecutor);
    DefaultPluginConfigurationService pcs = new DefaultPluginConfigurationService(dm, factory, targetExecutor, output);
    DefaultConfigurationService service = new DefaultConfigurationService(dm, pcs, targetExecutor, output);

    context.setPluginCacheDirectory(new File("target/test/plugins"));
    context.setBuildFile(new File("src/java/test/unit/org/savantbuild/config/withplugins.savant"));
    context.setWorkflowFile(new File("src/java/test/unit/org/savantbuild/config/localworkflows.savant"));
    service.configure(context);

    Project project = context.getProject();
    assertEquals(project.getGroup(), "org.savantbuild.test");
    assertEquals(project.getName(), "savant-core-test");
    assertEquals(project.getVersion(), "2.0-A1");
    assertNotNull(project.getTargets().get("compile"));
    assertEquals(project.getTargets().get("compile").getPre().size(), 1);
    assertEquals(project.getTargets().get("compile").getSteps().size(), 1);
    assertEquals(project.getTargets().get("compile").getPost().size(), 1);
    assertNotNull(project.getTargets().get("jar"));

    assertEquals(project.getTargets().get("jar").getPre().size(), 0);
    assertEquals(project.getTargets().get("jar").getSteps().size(), 1);
    assertEquals(project.getTargets().get("jar").getPost().size(), 0);

    assertNotNull(project.getPlugins().get("java").getTargets().get("compile"));
    assertEquals(project.getPlugins().get("java").getTargets().get("compile").getPre().size(), 1);
    assertEquals(project.getPlugins().get("java").getTargets().get("compile").getSteps().size(), 1);
    assertEquals(project.getPlugins().get("java").getTargets().get("compile").getPost().size(), 1);
    assertNotNull(project.getPlugins().get("java").getTargets().get("jar"));
    assertEquals(project.getPlugins().get("java").getTargets().get("jar").getPre().size(), 1);
    assertEquals(project.getPlugins().get("java").getTargets().get("jar").getSteps().size(), 1);
    assertEquals(project.getPlugins().get("java").getTargets().get("jar").getPost().size(), 1);
    assertNotNull(project.getPlugins().get("java").getTargets().get("int"));
    assertEquals(project.getPlugins().get("java").getTargets().get("int").getPre().size(), 0);
    assertEquals(project.getPlugins().get("java").getTargets().get("int").getSteps().size(), 1);
    assertEquals(project.getPlugins().get("java").getTargets().get("int").getPost().size(), 0);

    assertNotNull(project.getPlugins().get("groovy").getTargets().get("compile"));
    assertEquals(project.getPlugins().get("groovy").getTargets().get("compile").getPre().size(), 0);
    assertEquals(project.getPlugins().get("groovy").getTargets().get("compile").getSteps().size(), 1);
    assertEquals(project.getPlugins().get("groovy").getTargets().get("compile").getPost().size(), 0);
    assertNotNull(project.getPlugins().get("groovy").getTargets().get("jar"));
    assertEquals(project.getPlugins().get("groovy").getTargets().get("jar").getPre().size(), 0);
    assertEquals(project.getPlugins().get("groovy").getTargets().get("jar").getSteps().size(), 1);
    assertEquals(project.getPlugins().get("groovy").getTargets().get("jar").getPost().size(), 0);
    assertNotNull(project.getPlugins().get("groovy").getTargets().get("int"));
    assertEquals(project.getPlugins().get("groovy").getTargets().get("int").getPre().size(), 0);
    assertEquals(project.getPlugins().get("groovy").getTargets().get("int").getSteps().size(), 1);
    assertEquals(project.getPlugins().get("groovy").getTargets().get("int").getPost().size(), 0);
  }

  @Test
  public void duplicatePlugins() throws Exception {
    FileTools.prune(new File("target/test/deps"));
    FileTools.prune(new File("target/test/plugins"));

    DependencyManager dm = new DefaultDependencyManager(new DefaultDependencyResolver(output),
      new DefaultDependencyPublisher(output), new DefaultDependencyDeleter(output));
    TargetExecutor targetExecutor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    SyntheticTargetFactory factory = new DefaultSyntheticTargetFactory(targetExecutor);
    DefaultPluginConfigurationService pcs = new DefaultPluginConfigurationService(dm, factory, targetExecutor, output);
    DefaultConfigurationService service = new DefaultConfigurationService(dm, pcs, targetExecutor, output);

    context.setBuildFile(new File("src/java/test/unit/org/savantbuild/config/duplicateplugins.savant"));
    context.setWorkflowFile(new File("src/java/test/unit/org/savantbuild/config/localworkflows.savant"));

    try {
      service.configure(context);
      fail("Should have failed with a duplicate plugin");
    } catch (BuildException e) {
      System.out.println("SHOULD BE DUPLICATE PLUGIN " + e);
      // Expected
    }
  }
}
