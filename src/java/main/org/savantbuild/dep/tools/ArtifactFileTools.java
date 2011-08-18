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
package org.savantbuild.dep.tools;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.savantbuild.dep.ResolutionContext;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.io.FileTools;
import org.savantbuild.util.StringTools;

/**
 * <p>
 * This is a toolkit for helping copy artifact files around.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ArtifactFileTools {
  /**
   * Builds a directory for the given artifact. This directory starts with the absolute path of the directory passed
   * into the method followed by the directory parts of the artifact given (group, project, version).
   *
   * @param dir      The root directory.
   * @param artifact The artifact.
   * @return The directory.
   */
  public static File directory(File dir, Artifact artifact) {
    String path = StringTools.join("/", dir.getAbsolutePath(), artifact.getGroup().replace('.', '/'), artifact.getProject(),
      artifact.getVersion());
    return new File(path);
  }

  public static void copy(ResolutionContext context, Artifact artifact, File to) throws IOException {
    File f = context.getArtifactFiles().get(artifact);
    FileTools.copy(f, to);
  }

  public static void copy(ResolutionContext context, Dependencies dependencies, String groupType, File to) throws IOException {
    ArtifactGroup ag = dependencies.getArtifactGroups().get(groupType);
    if (ag == null) {
      return;
    }

    List<Artifact> artifacts = ag.getArtifacts();
    for (Artifact artifact : artifacts) {
      copy(context, artifact, to);
    }
  }

  public static void copyAll(ResolutionContext context, Dependencies dependencies, File to) throws IOException {
    Collection<ArtifactGroup> groups = dependencies.getArtifactGroups().values();
    for (ArtifactGroup group : groups) {
      List<Artifact> artifacts = group.getArtifacts();
      for (Artifact artifact : artifacts) {
        copy(context, artifact, to);
      }
    }
  }
}
