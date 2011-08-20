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
package org.savantbuild.util;

import java.io.File;
import java.util.Map;

import org.savantbuild.BuildException;

import com.google.inject.Inject;
import com.google.inject.Injector;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MetaClass;
import groovy.lang.Script;
import groovy.util.AntBuilder;
import static org.savantbuild.util.CollectionTools.*;

/**
 * <p>
 * This class is a toolkit that provides assistance when executing Savant's Groovy DSL files
 * (project build script, workflows scripts, plugin scripts).
 * </p>
 *
 * @author Brian Pontarelli
 */
public class GroovyTools {
  private static Injector injector;

  @Inject
  public static void initialize(Injector injector) {
    GroovyTools.injector = injector;
  }

  /**
   * Executes the given Groovy script using the given GroovyClassLoader, MetaClass (from the builder), and bindings.
   *
   * @param gcl        The class loader.
   * @param scriptFile The script file.
   * @param builder    The meta class builder.
   * @param bindings   The bindings.
   */
  public static void executeScript(GroovyClassLoader gcl, File scriptFile, MetaClassBuilder builder, Map<?, ?> bindings) {
    try {
      Class groovyClass = gcl.parseClass(scriptFile);
      Script script = (Script) groovyClass.newInstance();

      MetaClass metaClass = builder.build(groovyClass);
      metaClass.initialize();
      script.setMetaClass(metaClass);

      AntBuilder ant = new AntBuilder();
      ant.invokeMethod("taskdef", map("name", "dependencypath", "classname", "org.savantbuild.dep.ant.DependencyPathTask"));
      ant.invokeMethod("taskdef", map("name", "artifactcopy", "classname", "org.savantbuild.dep.ant.ArtifactCopyTask"));

      Binding binding = new Binding();
      binding.setVariable("ant", ant);
      for (Object key : bindings.keySet()) {
        binding.setVariable(key.toString(), bindings.get(key));
      }

      script.setBinding(binding);

      injector.injectMembers(script);
      script.run();
    } catch (BuildException e) {
      if (e.getFileName() == null) {
        e.setFileName(scriptFile.getAbsolutePath());
      }
      throw e;
    } catch (Exception e) {
      BuildException be = new BuildException("Error loading build/plugin script [" + scriptFile.getAbsolutePath() + "]\n\n" + e.getMessage(), e);
      be.setFileName(scriptFile.getAbsolutePath());
      throw be;
    }
  }

  public static interface MetaClassBuilder {
    public MetaClass build(Class<?> type);
  }
}
