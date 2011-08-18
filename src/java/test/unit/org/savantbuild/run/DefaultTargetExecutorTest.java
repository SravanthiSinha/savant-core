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

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.cli.PosixParser;
import org.codehaus.groovy.runtime.MethodClosure;
import org.savantbuild.domain.Plugin;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p>
 * This class tests the target executor.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultTargetExecutorTest {
  @Test
  public void argumentParse() {
    TestClosureTarget tester = new TestClosureTarget();

    Target target = new Target("test");
    target.leftShift(new MethodClosure(tester, "test"));
    target.getParams().put("testParam", Arrays.<Object>asList(true, "Description"));
    target.getParams().put("testParam2", Arrays.<Object>asList(false, "Description2"));

    Project project = new Project();
    project.getTargets().put("test", target);

    StringBuilderOutput output = new StringBuilderOutput();
    DefaultTargetExecutor executor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    executor.run(project, "test", Arrays.asList("--testParam=paramValue", "--testParam2"));

    assertTrue(tester.called);
    assertEquals(tester.parameters.get("testParam"), "paramValue");
    assertTrue(tester.parameters.containsKey("testParam2"));
  }

  @Test
  public void preAndPost() {
    TestClosureTarget tester = new TestClosureTarget();

    Target target = new Target("test");
    target.leftShift(new MethodClosure(tester, "test"));
    target.getPre().add(new MethodClosure(tester, "pre"));
    target.getPost().add(new MethodClosure(tester, "post"));

    Project project = new Project();
    project.getTargets().put("test", target);

    StringBuilderOutput output = new StringBuilderOutput();
    DefaultTargetExecutor executor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    executor.run(project, "test");

    assertTrue(tester.called);
    assertTrue(tester.pre);
    assertTrue(tester.post);
  }

  @Test
  public void dependencies() {
    TestClosureTarget tester = new TestClosureTarget();
    TestClosureTarget depTester = new TestClosureTarget();

    Target target = new Target("test");
    target.leftShift(new MethodClosure(tester, "test"));
    target.getPre().add(new MethodClosure(tester, "pre"));
    target.getPost().add(new MethodClosure(tester, "post"));
    target.getDependencies().add("dep");

    Target dep = new Target("dep");
    dep.leftShift(new MethodClosure(depTester, "test"));
    dep.getPre().add(new MethodClosure(depTester, "pre"));
    dep.getPost().add(new MethodClosure(depTester, "post"));

    Project project = new Project();
    project.getTargets().put("test", target);
    project.getTargets().put("dep", dep);

    StringBuilderOutput output = new StringBuilderOutput();
    DefaultTargetExecutor executor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    executor.run(project, "test");

    assertTrue(tester.called);
    assertTrue(tester.pre);
    assertTrue(tester.post);

    assertTrue(depTester.called);
    assertTrue(depTester.pre);
    assertTrue(depTester.post);
  }

  @Test
  public void plugin() {
    TestClosureTarget tester = new TestClosureTarget();
    TestClosureTarget depTester = new TestClosureTarget();

    Target target = new Target("test");
    target.leftShift(new MethodClosure(tester, "test"));
    target.getPre().add(new MethodClosure(tester, "pre"));
    target.getPost().add(new MethodClosure(tester, "post"));
    target.getDependencies().add("dep");

    Target dep = new Target("dep");
    dep.leftShift(new MethodClosure(depTester, "test"));
    dep.getPre().add(new MethodClosure(depTester, "pre"));
    dep.getPost().add(new MethodClosure(depTester, "post"));

    Project project = new Project();
    project.getPlugins().put("plugin", new Plugin());
    project.getPlugins().get("plugin").getTargets().put("test", target);
    project.getTargets().put("dep", dep);

    StringBuilderOutput output = new StringBuilderOutput();
    DefaultTargetExecutor executor = new DefaultTargetExecutor(new DefaultArgumentParser(new PosixParser()), new DefaultHelpOutputer(output), output);
    executor.run(project, "plugin:test");

    assertTrue(tester.called);
    assertTrue(tester.pre);
    assertTrue(tester.post);

    assertTrue(depTester.called);
    assertTrue(depTester.pre);
    assertTrue(depTester.post);
  }

  public static class TestClosureTarget {
    public boolean called;
    public Map<String, String> parameters;
    public boolean pre;
    public boolean post;

    public void test(Map<String, String> parameters) {
      this.called = true;
      this.parameters = parameters;
    }

    public void pre(Map<String, String> parameters) {
      this.pre = true;
    }

    public void post(Map<String, String> parameters) {
      this.post = true;
    }
  }
}
