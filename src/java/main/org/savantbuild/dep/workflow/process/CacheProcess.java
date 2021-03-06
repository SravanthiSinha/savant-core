/*
 * Copyright (c) 2001-2010, Inversoft, All Rights Reserved
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
package org.savantbuild.dep.workflow.process;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.savantbuild.BuildException;
import org.savantbuild.dep.NegativeCacheException;
import org.savantbuild.dep.version.ArtifactVersionTools;
import org.savantbuild.dep.workflow.PublishWorkflowHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.io.DoesNotExistException;
import org.savantbuild.io.FileTools;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.StringTools;

/**
 * <p>
 * This is an implementation of the ProcessHandler that uses the a local cache to fetch and
 * publish artifacts.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class CacheProcess extends AbstractProcessHandler {
  private final Output output;
  private String dir;

  public CacheProcess(Output output, Map<String, String> attributes) {
    this.output = output;
    this.dir = attributes.get("dir");
    if (dir == null) {
      dir = System.getProperty("user.home") + "/.savant/cache";
    }
  }

  /**
   * Checks the cache directory for the item. If it exists it is returned. If not, either a NegativeCacheException or
   * a DoesNotExistException is thrown (depending on if there is a negative cache record or not).
   *
   * @param artifact               The artifact that the item is associated with.
   * @param item                   The name of the item being fetched.
   * @param publishWorkflowHandler The PublishWorkflowHandler that is used to store the item if it can be found.
   * @return The File from the cache.
   * @throws DoesNotExistException  If the file doesn't exist.
   * @throws NegativeCacheException If there is a negative cache record of the file, meaning it doesn't exist anywhere
   *                                in the world.
   */
  @Override
  public File fetch(Artifact artifact, String item, PublishWorkflowHandler publishWorkflowHandler)
    throws DoesNotExistException, NegativeCacheException {
    String path = StringTools.join("/", dir, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item);
    File file = new File(path);
    if (!file.isFile()) {
      file = new File(path + ".neg");
      if (file.isFile()) {
        throw new NegativeCacheException();
      } else {
        throw new DoesNotExistException();
      }
    }

    return file;
  }

  /**
   * Finds the latest or integration build of the artifact inside the cache.
   *
   * @param artifact The artifact to get the version for.
   * @return The version or null if it couldn't be found.
   */
  @Override
  public String determineVersion(Artifact artifact) {
    String version = artifact.getVersion();
    if (version.equals(ArtifactVersionTools.LATEST)) {
      File dir = new File(StringTools.join("/", this.dir, artifact.getGroup().replace('.', '/'), artifact.getProject()));
      Set<String> names = listFiles(dir);
      version = ArtifactVersionTools.latest(artifact, names);
    } else if (version.endsWith(ArtifactVersionTools.INTEGRATION)) {
      File dir = new File(StringTools.join("/", this.dir, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion()));
      Set<String> names = listFiles(dir);
      version = ArtifactVersionTools.bestIntegration(artifact, names);
    }

    return version;
  }

  /**
   * Publishes the given artifact item into the cache.
   *
   * @param artifact The artifact that the item might be associated with.
   * @param item     The name of the item to publish.
   * @param file     The file that is the item.
   * @return Always null.
   * @throws BuildException If the publish fails.
   */
  @Override
  public File publish(Artifact artifact, String item, File file) throws BuildException {
    String path = StringTools.join("/", dir, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item);
    File cacheFile = new File(path);
    if (cacheFile.isDirectory()) {
      throw new BuildException("Cache location is for an artifact to be stored is a directory [" + cacheFile.getAbsolutePath() + "]");
    }

    if (cacheFile.isFile() && !cacheFile.delete()) {
      throw new BuildException("Unable to clean out old file to replace [" + cacheFile.getAbsolutePath() + "]");
    } else if (!cacheFile.exists()) {
      File dir = cacheFile.getParentFile();
      if (!dir.exists() && !dir.mkdirs()) {
        throw new BuildException("Unable to create cache directory [" + cacheFile.getParent() + "]");
      }
    }

    try {
      if (!cacheFile.createNewFile()) {
        throw new BuildException("Unable to create cache file [" + cacheFile.getAbsolutePath() + "]");
      }
    } catch (IOException ioe) {
      throw new BuildException(ioe);
    }

    try {
      FileTools.copy(file, cacheFile);
    } catch (IOException e) {
      throw new BuildException(e);
    }

    if (!item.endsWith("md5")) {
      output.info("Cached at [" + dir + "/" + path + "]");
    }

    return cacheFile;
  }

  /**
   * Deletes the artifact item.
   *
   * @param artifact The artifact if needed.
   * @param item     The item to delete.
   * @return True if the item was deleted, false otherwise.
   * @throws BuildException If the delete failed.
   */
  @Override
  public boolean delete(Artifact artifact, String item) throws BuildException {
    String path = StringTools.join("/", dir, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item);
    File file = new File(path);
    boolean deleted = false;
    if (file.isFile()) {
      deleted = file.delete();
    }

    return deleted;
  }

  /**
   * Deletes out the integration builds from the cache.
   *
   * @param artifact The artifact. This artifacts version is the next integration build version.
   */
  @Override
  public void deleteIntegrationBuilds(Artifact artifact) {
    String path = StringTools.join("/", dir, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion() + "-{integration}");
    File dir = new File(path);
    if (!dir.isDirectory()) {
      return;
    }

    FileTools.prune(dir);
  }

  private Set<String> listFiles(File dir) {
    Set<String> names = new HashSet<String>();
    File[] files = dir.listFiles();
    if (files == null || files.length == 0) {
      return names;
    }

    for (File file : files) {
      String fileName = file.getName();
      names.add(fileName);
    }

    return names;
  }
}
