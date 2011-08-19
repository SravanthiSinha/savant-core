/*
 * Copyright (c) 2001-2011, Inversoft, All Rights Reserved
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
package org.savantbuild.dep.ant;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.savantbuild.dep.DependencyListener;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Workflow;
import org.savantbuild.io.FileTools;

import com.google.inject.Inject;

import static java.util.Arrays.asList;

/**
 * <p>
 * This class is used to copy artifacts from the local cache
 * store to a directory. This class can be used the same as
 * a normal copy task, but also supports nested dependencies
 * elements.
 * </p>
 *
 * <p>
 * By default this class copies all artifacts and transitive
 * artifacts in the compile and runtime groups only. For more
 * information on artifact groups, see the {@link ArtifactGroup}
 * class.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ArtifactCopyTask extends Copy {
  private String dependencies;
  private boolean transitive = true;
  private String[] types;

  private static DependencyManager manager;
  private static Context context;

  @Inject
  public static void initialize(DependencyManager manager, Context context) {
    ArtifactCopyTask.manager = manager;
    ArtifactCopyTask.context = context;
  }

  public void setDependencies(String dependencies) {
    this.dependencies = dependencies;
  }

  /**
   * Indicates whether or not the dependencies should be copied or not.
   *
   * @param   transitive The flag.
   */
  public void setTransitive(boolean transitive) {
    this.transitive = transitive;
  }

  /**
   * Sets the list of types. This defaults to compile and run time if not specified.
   *
   * @param   types The list of types.
   */
  public void setTypes(String types) {
    this.types = types.split(",\\W*");
  }

  /**
   * Performs the copy of all the artifacts.
   */
  public void execute() {
    Logger savantLog = Logger.getLogger("org.savantbuild");
    Level oldLevel = savantLog.getLevel();
    savantLog.setLevel(Level.SEVERE);

    CopyListener copyListener = new CopyListener();
    try {

      Set<String> artifactGroupTypes = null;
      if (types != null) {
        artifactGroupTypes = new HashSet<String>(asList(types));
      }

      Workflow workflow = manager.determineProjectWorkflow(context.getWorkflows(), context.getProject());
      Dependencies deps = context.getProject().getDependencies().get(dependencies);

      manager.getResolver().resolve(deps, workflow, artifactGroupTypes, transitive, copyListener);
    } catch (Exception e) {
      throw new BuildException(e);
    } finally {
      savantLog.setLevel(oldLevel);
    }

    if (copyListener.added) {
      super.execute();
    }
  }

  private class CopyListener implements DependencyListener {
    boolean added = false;
    Map basedOnRoot = new HashMap();

    @Override
    public void artifactFound(File file, Artifact artifact) {
      added = true;

      File root = FileTools.getRoot(file);
      FileSet fs = (FileSet) basedOnRoot.get(root);
      if (fs == null) {
        fs = new FileSet();
        fs.setProject(getProject());

        ArtifactCopyTask.super.addFileset(fs);
        fs.setDir(root);
      }

      String name = file.getAbsolutePath();
      String rootPath = root.getAbsolutePath();
      int index = name.indexOf(rootPath);
      if (index >= 0) {
        name = name.substring(rootPath.length());
      }

      PatternSet.NameEntry ne = fs.createInclude();
      ne.setName(name);
    }

    @Override
    public void artifactCleaned(Artifact artifact) {
    }

    @Override
    public void artifactPublished(Artifact artifact) {
    }
  }
}
