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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.savantbuild.BuildException;
import org.savantbuild.dep.version.ArtifactVersionTools;
import org.savantbuild.dep.workflow.PublishWorkflowHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.io.DoesNotExistException;
import org.savantbuild.io.IOTools;
import org.savantbuild.io.MD5;
import org.savantbuild.io.PermanentIOException;
import org.savantbuild.io.TemporaryIOException;
import org.savantbuild.net.NetTools;
import org.savantbuild.run.output.Output;
import org.savantbuild.util.ErrorList;

import static java.util.Arrays.*;

/**
 * <p>
 * This class is a workflow process that attempts to download
 * artifacts from the internet using the Savant scheme via HTTP.
 * </p>
 * <p/>
 * <p>
 * Savant's URL scheme is
 * <b>domain</b>/<b>group</b>/<b>project</b>/<b>name</b>-<b>version</b>.<b>type</b>
 * </p>
 * <p/>
 * <p>
 * Savant's scheme allows a local repository to be setup and
 * still contain artifacts from 3rd party groups.
 * </p>
 * <p/>
 * <p>
 * In order to determine the domain in the URL, either a
 * properties file can be read for group/domain pairs. Or
 * a standard location can be setup in this class.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class URLProcessHandler extends AbstractProcessHandler {
  private final static Pattern HTML = Pattern.compile("a href=\"([^/]+?)/?\"");

  private final Output output;
  private final String url;
  private final String username;
  private final String password;

  public URLProcessHandler(Output output, Map<String, String> attributes) {
    this.output = output;
    this.url = attributes.get("url");
    this.username = attributes.get("username");
    this.password = attributes.get("password");

    ErrorList errors = new ErrorList();
    if (url == null) {
      errors.addError("The [url] attribute is required for the [url] workflow process");
    }

    if ((username != null && password == null) || (username == null && password != null)) {
      errors.addError("You must specify both the [username] and [password] attributes to turn on authentication " +
        "for the [url] workflow process.");
    }

    if (!errors.isEmpty()) {
      throw new BuildException(errors);
    }
  }

  /**
   * Using the URL spec given, this method connects to the URL, reads the file
   * from the URL and stores the file in the local cache store. The artifact is
   * used to determine the local cache store directory and file name.
   *
   * @param artifact               The artifact being fetched and stored
   * @param publishWorkflowHandler The publishWorkflowHandler to publish the artifact if found.
   * @param item                   The item to fetch.
   * @return The File of the artifact after it has been published.
   */
  @Override
  public File fetch(Artifact artifact, String item, PublishWorkflowHandler publishWorkflowHandler)
    throws TemporaryIOException, PermanentIOException, DoesNotExistException {
    URI md5URI = NetTools.build(url, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item + ".md5");
    File md5File = NetTools.downloadToFile(md5URI, username, password, null);
    MD5 md5 = IOTools.parseMD5(md5File);

    URI itemURI = NetTools.build(url, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion(), item);
    File itemFile = NetTools.downloadToFile(itemURI, username, password, md5);
    if (itemFile == null) {
      throw new DoesNotExistException("Artifact item doesn't exist [" + itemURI + "]");
    }

    output.info("Downloaded from " + itemURI);

    publishWorkflowHandler.publish(artifact, item + ".md5", md5File);
    return publishWorkflowHandler.publish(artifact, item, itemFile);
  }

  /**
   * This makes a large assumption! This assumes that the artifacts are fetched from an Apache or other HTTP server
   * that provides index listings.
   *
   * @param artifact The artifact.
   * @return The version, if it exists.
   */
  @Override
  public String determineVersion(Artifact artifact) {
    // Handle the latest version case
    String version = artifact.getVersion();
    if (version.equals(ArtifactVersionTools.LATEST)) {
      // Get all the versions
      URI uri = NetTools.build(url, artifact.getGroup().replace('.', '/'), artifact.getProject());
      Set<String> names = parseNames(uri);
      if (names == null || names.isEmpty()) {
        return null;
      }

      version = ArtifactVersionTools.latest(artifact, names);

      // Handle the integration version case
    } else if (version.endsWith(ArtifactVersionTools.INTEGRATION)) {
      // Get all the integration files
      URI uri = NetTools.build(url, artifact.getGroup().replace('.', '/'), artifact.getProject(), artifact.getVersion());
      Set<String> names = parseNames(uri);
      if (names == null || names.isEmpty()) {
        return null;
      }

      version = ArtifactVersionTools.bestIntegration(artifact, names);
    }

    return version;
  }

  /**
   * Throws an exception. This isn't supported yet.
   */
  @Override
  public File publish(Artifact artifact, String item, File file) throws BuildException {
    throw new BuildException("The [url] process doesn't allow publishing yet.");
  }

  /**
   * Throws an exception. This isn't supported yet.
   */
  @Override
  public boolean delete(Artifact artifact, String item) throws BuildException {
    throw new BuildException("The [url] process doesn't allow publishing yet.");
  }

  /**
   * Throws an exception. This isn't supported yet.
   */
  @Override
  public void deleteIntegrationBuilds(Artifact artifact) {
    throw new BuildException("The [url] process doesn't allow publishing yet.");
  }

  private Set<String> parseNames(URI uri) {
    try {
      String result = NetTools.downloadToString(uri, username, password);
      Set<String> names = new HashSet<String>();
      if (result.contains("<html")) {
        Matcher matcher = HTML.matcher(result);
        while (matcher.find()) {
          try {
            names.add(URLDecoder.decode(matcher.group(1), "UTF-8"));
          } catch (UnsupportedEncodingException e) {
            throw new BuildException("Unable to decode version URLs inside the HTML returned from the remote " +
              "Savant repository [" + uri.toString() + "]", e);
          }
        }
      } else {
        names.addAll(asList(result.split("\n")));
      }

      return names;
    } catch (DoesNotExistException e) {
      return null;
    } catch (TemporaryIOException e) {
      return null;
    } catch (PermanentIOException e) {
      throw new BuildException(e);
    }
  }
}
