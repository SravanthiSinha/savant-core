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
package org.savantbuild.dep.ant;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Workflow;

import com.google.inject.Inject;
import static java.util.Arrays.*;

/**
 * <p>
 * This class is the dependency path task that helps integration the Savant dependency management
 * system with Ant.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DependencyPathTask extends Task {
  private static DependencyManager manager;
  private static Context context;

  private String id;
  private String dependencies;
  private String[] types;
  private boolean transitive = true;

  @Inject
  public static void initialize(DependencyManager manager, Context context) {
    DependencyPathTask.manager = manager;
    DependencyPathTask.context = context;
  }

  public void setDependencies(String dependencies) {
    this.dependencies = dependencies;
  }

  public void setTransitive(boolean transitive) {
    this.transitive = transitive;
  }

  /**
   * Sets the id of the path that is being created.
   *
   * @param id The id
   */
  public void setPathId(String id) {
    this.id = id;
  }

  /**
   * Sets the list of types. This defaults to compile (only) if not specified.
   *
   * @param types The list of types.
   */
  public void setTypes(String types) {
    this.types = types.split(",\\W*");
  }

  /**
   * Executes this task that builds up the dependencies and such.
   *
   * @throws org.apache.tools.ant.BuildException
   *          If the task fails
   */
  public void execute() throws BuildException {
    if (id == null) {
      throw new BuildException("pathid required for dependencypath");
    }

    Object obj = getProject().getReference(id);
    Path path = null;
    if (obj != null) {
      if (obj instanceof Path) {
        path = (Path) obj;
      } else {
        throw new BuildException("Found [" + obj.getClass().getName() + "] an non-Path Ant type for the ID [" +
          id + "]. It must be a Path type or nothing should be defined for that ID");
      }
    }

    if (path == null) {
      path = new Path(getProject());
      getProject().addReference(id, path);
    }

    Workflow workflow = manager.determineProjectWorkflow(context.getWorkflows(), context.getProject());
    if (workflow == null) {
      throw new BuildException("Unable to locate a workflow to use for resolving the dependencies. Did you create " +
        "a ~/workflows.savant file and leave it blank? Consult the Savant documentation to determine how to " +
        "configure workflows for dependency resolution.");
    }

    try {
      Set<String> typeSet = new HashSet<String>();
      if (types != null) {
        typeSet.addAll(asList(types));
      }

      Dependencies deps = context.getProject().getDependencies().get(dependencies);
      if (deps == null) {
        throw new BuildException("Invalid dependencies [" + (dependencies == null ? "DEFAULT" : dependencies) + "]");
      }

      Map<Artifact, File> files = manager.getResolver().resolve(deps, workflow, typeSet, transitive);
      for (File file : files.values()) {
        Path.PathElement pe = path.createPathElement();
        pe.setLocation(file);
      }
    } catch (org.savantbuild.BuildException be) {
      throw new BuildException(be);
    }
  }
}
