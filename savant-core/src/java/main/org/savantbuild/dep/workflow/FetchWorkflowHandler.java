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
package org.savantbuild.dep.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.savantbuild.BuildException;
import org.savantbuild.dep.NegativeCacheException;
import org.savantbuild.dep.ResolutionContext;
import org.savantbuild.dep.workflow.process.ProcessHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactMetaData;
import org.savantbuild.domain.Version;
import org.savantbuild.io.DoesNotExistException;
import org.savantbuild.io.PermanentIOException;
import org.savantbuild.io.TemporaryIOException;
import org.savantbuild.run.output.Level;
import org.savantbuild.run.output.Output;

/**
 * <p>
 * This class is the workflow that is used when attempting to fetch artifacts.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class FetchWorkflowHandler {
  private final Output output;
  private List<ProcessHandler> processes = new ArrayList<ProcessHandler>();

  public FetchWorkflowHandler(Output output) {
    this.output = output;
  }

  /**
   * @return The process list.
   */
  public List<ProcessHandler> getProcesses() {
    return processes;
  }

  /**
   * This loops over all the processes until the item is found or not. Each process must call
   * to the PublishWorkflowHandler if it finds the artifact and the publish workflow must be able to return
   * a File that can be used for future reference.
   *
   * @param artifact               The artifact if needed.
   * @param item                   The name of the item being fetched. This item name should NOT include the path
   *                               information. This will be handled by the processes so that flattened namespacing and
   *                               other types of handling can be performed. This item should only be the name of the
   *                               item being fetched. For example, if the artifact MD5 file is being fetched this would
   *                               look like this: common-collections-2.1.jar.md5.
   * @param publishWorkflowHandler The PublishWorkflowHandler that is used to store the item if it can be found.
   * @param context                The resolution context used to store missing items.
   * @return A file that contains the item contents or null if the item was not found.
   */
  public File fetchItem(Artifact artifact, String item, PublishWorkflowHandler publishWorkflowHandler, ResolutionContext context) {
    output.println(Level.DEBUG, "Running [" + processes.size() + "] to fetch [" + item + "]");

    boolean temporaryError = false;
    for (ProcessHandler process : processes) {
      output.println(Level.DEBUG, "Executing fetch process [" + process.getClass().getName() + "] to resolve artifact");

      try {
        return process.fetch(artifact, item, publishWorkflowHandler);
      } catch (TemporaryIOException e) {
        temporaryError = true;
      } catch (PermanentIOException e) {
        throw new BuildException(e);
      } catch (DoesNotExistException e) {
        // Ignore
      } catch (NegativeCacheException e) {
        output.println(Level.DEBUG, "Item [" + item + "] for artifact [" + artifact + "] was negatively cached");
        return null;
      }
    }

    if (!temporaryError) {
      context.addMissingItem(artifact, item);
    }

    return null;
  }

  /**
   * This loops over all the processes until the MetaData file for the artifact given is found
   * or not. Each process must call to the PublishWorkflowHandler if it finds the artifact and the publish
   * workflow must be able to return a File that can be used for future reference.
   *
   * @param artifact               The artifact to fetch the MetaData for.
   * @param publishWorkflowHandler The PublishWorkflowHandler that is used to store the MetaData if it can
   *                               be found.
   * @param context                The resolution context used to store missing items.
   * @return The ArtifactMetaData of the artifact or null if the artifact doesn't have any.
   */
  public ArtifactMetaData fetchMetaData(Artifact artifact, PublishWorkflowHandler publishWorkflowHandler, ResolutionContext context) {
    output.println(Level.DEBUG, "Running [" + processes.size() + "] to fetch MetaData for artifact [" + artifact + "]");

    boolean temporaryError = false;
    for (ProcessHandler process : processes) {
      output.println(Level.DEBUG, "Executing fetch process [" + process.getClass().getName() + "]" +
        " to resolve artifact MetaData");

      try {
        return process.fetchMetaData(artifact, publishWorkflowHandler);
      } catch (TemporaryIOException e) {
        output.println(Level.DEBUG, "Encountered temporary IO exception while fetching AMD for [" + artifact + "]", e);
        temporaryError = true;
      } catch (PermanentIOException e) {
        output.println(Level.DEBUG, "Encountered permanant IO exception while fetching AMD for [" + artifact + "]", e);
        throw new BuildException(e);
      } catch (DoesNotExistException e) {
        // This does nothing.
        output.println(Level.DEBUG, "Encountered DoesNotExistException while fetching AMD for [" + artifact + "]", e);
      } catch (NegativeCacheException e) {
        return null;
      }
    }

    if (!temporaryError) {
      context.addMissingItem(artifact, "AMD_FILE");
    }

    return null;
  }

  /**
   * Determines the version available for the given artifact. This version might be an integration if the artifact's
   * given version String is an integration string (i.e. <strong>1.0-{integration} </strong>) or it might be the latest
   * version if the artifact's version is <strong>{latest}</strong>.
   *
   * @param artifact The artifact to get the version for.
   * @return The version if one exists, otherwise null.
   */
  public String determineVersion(Artifact artifact) {
    Version best = null;
    String versionStr = null;
    for (ProcessHandler process : processes) {
      String currentVersionStr = process.determineVersion(artifact);
      if (currentVersionStr != null) {
        Version currentVersion = new Version(currentVersionStr);
        if (best == null || currentVersion.compareTo(best) > 0) {
          best = currentVersion;
          versionStr = currentVersionStr;
        }
      }
    }

    return versionStr;
  }
}
