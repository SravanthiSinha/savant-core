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
package org.savantbuild.dep.version;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.savantbuild.BuildException;
import org.savantbuild.dep.workflow.FetchWorkflowHandler;
import org.savantbuild.dep.workflow.WorkflowHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Version;
import org.savantbuild.run.output.Level;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.ErrorList;

import com.google.inject.Inject;

/**
 * <p>
 * This class is a toolkit with helper methods for working with artifacts
 * versions.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ArtifactVersionTools {
  public static final String INTEGRATION = "-{integration}";
  public static final String LATEST = "{latest}";
  private static Output output;

  @Inject
  public static void initialize(Output output) {
    ArtifactVersionTools.output = output;
  }

  /**
   * Resolve all of the integration build versions for each artifact in the dependencies. This is not transitive.
   *
   * @param dependencies    The dependencies to update.
   * @param workflowHandler The workflowHandler used to resolve the integration build versions.
   */
  public static void resolve(Dependencies dependencies, WorkflowHandler workflowHandler) {
    FetchWorkflowHandler fw = workflowHandler.getFetchWorkflowHandler();

    Map<String, ArtifactGroup> artifactGroups = dependencies.getArtifactGroups();
    ErrorList errors = new ErrorList();
    for (ArtifactGroup group : artifactGroups.values()) {
      Set<Artifact> artifacts = new HashSet<Artifact>(group.getArtifacts());
      for (Artifact artifact : artifacts) {
        if (artifact.isLatestBuild()) {
          String version = fw.determineVersion(artifact);
          if (version == null) {
            errors.addError("Artifact [" + artifact + "] is set to the latest version, but no versions exists");
            continue;
          }

          artifact.setVersion(version);
        }

        if (artifact.isIntegrationBuild()) {
          String version = fw.determineVersion(artifact);
          if (version == null) {
            errors.addError("Artifact [" + artifact + "] is set to use an integration or latest build, but no builds exists");
            continue;
          }

          artifact.setIntegrationVersion(version);
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new BuildException(errors);
    }
  }

  /**
   * Strips the integration version from the artifact to produce a base version.
   *
   * @param artifact The artifact.
   * @return The base version.
   * @throws BuildException If the artifact isn't an integration version.
   */
  public static String baseVersion(Artifact artifact) {
    String version = artifact.getVersion();
    if (!version.endsWith(INTEGRATION)) {
      throw new BuildException("The version [" + version + "] is not an integration build version");
    }

    return version.substring(0, version.length() - INTEGRATION.length());
  }

  /**
   * Using the list of names, this determines the best integration version.
   *
   * @param artifact The artifact whose version is an integration version.
   * @param names    The list of artifact names.
   * @return The best version or null if there aren't any integration builds in the list of names.
   */
  public static String bestIntegration(Artifact artifact, Set<String> names) {
    String baseVersion = baseVersion(artifact);
    String ib = artifact.getId().getName() + "-" + baseVersion + "-IB";
    Long best = null;
    for (String name : names) {
      if (name.startsWith(ib)) {
        int index = name.indexOf(".", ib.length());
        String fileVersion = index >= ib.length() ? name.substring(ib.length(), index) : name.substring(ib.length());
        try {
          Long num = Long.valueOf(fileVersion);
          if (best == null || num > best) {
            best = num;
          }
        } catch (NumberFormatException e) {
          output.println(Level.DEBUG, "Invalid integration build version for artifact with name [" +
            name + "]. The part after IB is not a number. Skipping that file.");
        }
      }
    }

    if (best == null) {
      return null;
    }

    return baseVersion + "-IB" + best;
  }

  /**
   * Determines the latest version from the list of names given. This list is normally the list of
   * directories under the project directory.
   *
   * @param artifact The artifact.
   * @param names    The list of version directory names.
   * @return The best version.
   */
  public static String latest(Artifact artifact, Set<String> names) {
    String artifactName = artifact.getName();
    Version best = null;
    String bestStr = null;
    for (String name : names) {
      // This is the old layout where the files are all in the same directory
      if (name.startsWith(artifactName)) {
        name = name.substring(artifactName.length() + 1, name.length() - artifact.getType().length() - 1);
      }

      try {
        Version ver = new Version(name);
        if (best == null || best.compareTo(ver) < 0) {
          best = ver;
          bestStr = name;
        }
      } catch (Exception e) {
        // Ignoring bad version
      }
    }

    return bestStr;
  }
}
