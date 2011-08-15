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
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

import org.savantbuild.BuildException;
import org.savantbuild.dep.workflow.PublishWorkflowHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.io.DoesNotExistException;
import org.savantbuild.io.FileTools;
import org.savantbuild.io.IOTools;
import org.savantbuild.io.MD5;
import org.savantbuild.io.MD5Exception;
import org.savantbuild.io.PermanentIOException;
import org.savantbuild.io.TemporaryIOException;
import org.savantbuild.net.NetTools;
import org.savantbuild.net.SubVersion;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.ErrorList;

/**
 * <p>
 * This is an implementation of the ProcessHandler that uses the SVNKit SubVersion library to
 * fetch and publish artifacts from/to a SubVersion repository using SubVersion export and import
 * commands.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class SVNProcessHandler extends AbstractProcessHandler {
  private final Output output;
  private final String repository;
  private final String username;
  private final String password;

  public SVNProcessHandler(Output output, Map<String, String> attributes) {
    this.output = output;
    this.repository = attributes.get("repository");
    this.username = attributes.get("username");
    this.password = attributes.get("password");

    ErrorList errors = new ErrorList();
    if (repository == null) {
      errors.addError("The [repository] attribute is required for the [svn] workflow process");
    }

    if ((username != null && password == null) || (username == null && password != null)) {
      errors.addError("You must specify both the [username] and [password] attributes to turn on authentication " +
        "for the [svn] workflow process.");
    }

    if (!errors.isEmpty()) {
      throw new BuildException(errors);
    }
  }

  /**
   * Fetches the artifact from the SubVersion repository by performing an export to a temporary file and checking the
   * MD5 sum if it exists.
   *
   * @param artifact               The artifact to fetch and store.
   * @param item                   The item to fetch.
   * @param publishWorkflowHandler The publish workflow used to publish the artifact after it has been
   *                               successfully fetched.
   * @return The File if downloaded and stored.
   * @throws BuildException        If there was an unrecoverable error during SVN fetch.
   * @throws DoesNotExistException If the file doesn't exist in the SVN repository.
   */
  @Override
  public File fetch(Artifact artifact, String item, PublishWorkflowHandler publishWorkflowHandler)
    throws TemporaryIOException, PermanentIOException, DoesNotExistException {
    URI md5URI = NetTools.build(artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item + ".md5");
    File md5File = export(md5URI, null);
    MD5 md5 = IOTools.parseMD5(md5File);

    URI itemURI = NetTools.build(repository, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item);
    File itemFile = export(itemURI, md5);
    if (itemFile == null) {
      throw new DoesNotExistException();
    }

    output.info("Downloaded from SubVersion at [" + itemURI + "]");

    publishWorkflowHandler.publish(artifact, item + ".md5", md5File);
    return publishWorkflowHandler.publish(artifact, item, itemFile);
  }

  /**
   * I'm totally punting on SVN fetching of integration and latest versions. This could be a long and messy method that
   * is hard to test. I'll implement this later.
   *
   * @param artifact The artifact.
   * @return Always null.
   */
  public String determineVersion(Artifact artifact) {
    return null;
  }

  /**
   * Publishes the given artifact item into the SubVersion repository.
   *
   * @param artifact The artifact that the item might be associated with.
   * @param item     The name of the item to publish.
   * @param file     The file that is the item.
   * @return Always null.
   * @throws BuildException If the publish fails.
   */
  @Override
  public File publish(Artifact artifact, String item, File file) throws BuildException {
    SubVersion svn = new SubVersion(repository, username, password);
    try {
      if (!svn.isExists()) {
        throw new BuildException("Repository URL [" + repository + "] doesn't exist on the SubVersion server");
      } else if (svn.isFile()) {
        throw new BuildException("Repository URL [" + repository + "] points to a file and must point to a directory");
      }

      URI uri = NetTools.build(artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item);
      svn.doImport(uri.toString(), file);
      output.info("Published to SubVersion at [" + repository + "/" + uri + "]");
      return null;
    } finally {
      svn.close();
    }
  }

  /**
   * Not implemented yet.
   */
  @Override
  public boolean delete(Artifact artifact, String item) throws BuildException {
    throw new BuildException("The [svn] process doesn't allow deleting yet.");
  }

  /**
   * Not implemented yet.
   */
  @Override
  public void deleteIntegrationBuilds(Artifact artifact) {
//        throw new BuildException("The [svn] process doesn't allow deleting of integration builds yet.");
  }

  private File export(final URI uri, final MD5 md5) {
    return IOTools.protectIO(new Callable<File>() {
      @Override
      public File call() throws Exception {
        File file = File.createTempFile("savant-svn-process", "export");
        file.deleteOnExit();
        SubVersion svn = new SubVersion(repository, username, password);
        try {
          if (!svn.isExists()) {
            throw new BuildException("Repository [" + repository + "] doesn't exist on the SubVersion server");
          } else if (svn.isFile()) {
            throw new BuildException("Repository [" + repository + "] points to a file and must point to a directory");
          }

          if (!svn.doExport(uri.toString(), file)) {
            throw new DoesNotExistException();
          }

          if (md5 != null && md5.bytes != null) {
            MD5 exportedMD5 = FileTools.md5(file);
            if (!Arrays.equals(exportedMD5.bytes, md5.bytes)) {
              throw new MD5Exception("MD5 mismatch.");
            }
          }

          return file;
        } finally {
          svn.close();
        }
      }
    });
  }
}
