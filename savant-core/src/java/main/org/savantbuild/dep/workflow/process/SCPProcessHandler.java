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
import java.util.Map;
import java.util.concurrent.Callable;

import org.savantbuild.BuildException;
import org.savantbuild.dep.workflow.PublishWorkflowHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.io.IOTools;
import org.savantbuild.net.SCP;
import org.savantbuild.net.SSHOptions;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.ErrorList;
import org.savantbuild.util.StringTools;

/**
 * <p>
 * This is an implementation of the ProcessHandler that uses the JSCH to publish artifacts
 * to a server using SSH and SCP.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class SCPProcessHandler extends AbstractProcessHandler {
  private final Output output;
  private final SSHOptions options = new SSHOptions();
  private final String location;

  public SCPProcessHandler(Output output, Map<String, String> attributes) {
    this.output = output;
    options.server = attributes.get("server");
    options.port = StringTools.toInt(attributes.get("port"), 25);
    options.cipher = attributes.get("cipher");
    options.identity = attributes.get("identity");
    options.passphrase = attributes.get("passphrase");
    options.knownHosts = attributes.get("knownHosts");
    options.username = attributes.get("username");
    options.password = attributes.get("password");
    options.trustUnknownHosts = StringTools.toBoolean(attributes.get("trustKnownHosts"), false);
    location = attributes.get("location");

    ErrorList errors = new ErrorList();
    if (options.server == null) {
      errors.addError("The [server] attribute is required for the [scp] workflow process");
    }

    if (location == null) {
      errors.addError("The [location] attribute is required for the [scp] workflow process");
    }

    if ((options.username != null && options.password == null) || (options.username == null && options.password != null)) {
      errors.addError("You must specify both the [username] and [password] attributes to turn on authentication " +
        "for the [scp] workflow process.");
    }

    if (!errors.isEmpty()) {
      throw new BuildException(errors);
    }
  }

  /**
   * Not supported right now.
   */
  @Override
  public File fetch(Artifact artifact, String item, PublishWorkflowHandler publishWorkflowHandler) {
    throw new BuildException("The [scp] workflow process doesn't support fetching at this time.");
  }

  /**
   * Not supported right now.
   */
  public String determineVersion(Artifact artifact) {
    throw new BuildException("The [scp] workflow process doesn't support fetching at this time.");
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
    String path = StringTools.join("/", location, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item);
    upload(path, file);
    output.info("Published via SCP to [" + options.server + ":" + options.port + location + "/" + path + "]");
    return null;
  }

  /**
   * Not implemented yet.
   */
  @Override
  public boolean delete(Artifact artifact, String item) throws BuildException {
    throw new BuildException("The [scp] process doesn't allow deleting yet.");
  }

  /**
   * Not implemented yet.
   */
  @Override
  public void deleteIntegrationBuilds(Artifact artifact) {
    throw new BuildException("The [scp] process doesn't allow deleting of integration builds yet.");
  }

  private void upload(final String path, final File file) {
    IOTools.protectIO(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SCP scp = new SCP(options);
        scp.upload(file, path);
        return null;
      }
    });
  }
}
