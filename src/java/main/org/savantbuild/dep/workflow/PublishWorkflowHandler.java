/*
 * Copyright (c) 2001-2006, Inversoft, All Rights Reserved
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.savantbuild.BuildException;
import org.savantbuild.dep.workflow.process.ProcessHandler;
import org.savantbuild.dep.workflow.process.ProcessHandlerFactory;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.Process;
import org.savantbuild.domain.PublishWorkflow;
import org.savantbuild.run.output.Output;

/**
 * <p>
 * This is the interface that defines how artifacts are published to
 * different locations during resolution. Publishing is the act of
 * storing the artifact for later use. In general the publishing
 * corresponds one-to-one with the local cache store locations that
 * are used as part of the {@link FetchWorkflowHandler}, but this is in no
 * way required.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class PublishWorkflowHandler {
  public static PublishWorkflowHandler build(PublishWorkflow workflow, Output output) {
    List<org.savantbuild.domain.Process> processes = workflow.getProcesses();
    PublishWorkflowHandler publish = new PublishWorkflowHandler();
    for (Process process : processes) {
      ProcessHandler publishProcess = ProcessHandlerFactory.buildProcess(output, process);
      publish.getProcesses().add(publishProcess);
    }

    return publish;
  }

  private List<ProcessHandler> processes = new ArrayList<ProcessHandler>();

  /**
   * @return The process list.
   */
  public List<ProcessHandler> getProcesses() {
    return processes;
  }

  /**
   * Publishes the item using the processes in this workflow.
   *
   * @param artifact The artifact if needed.
   * @param item     The name of the item being published.
   * @param file     The file that is the artifact contents.
   * @return A file that can be used to reference the artifact for paths and other constructs.
   * @throws BuildException If the artifact could not be published for any reason.
   */
  public File publish(Artifact artifact, String item, File file) {
    File result = null;
    for (ProcessHandler process : processes) {
      File temp = process.publish(artifact, item, file);
      if (result == null) {
        result = temp;
      }
    }

    return result;
  }

  /**
   * Publishes a negative file for the artifact item. This file is empty, but signals Savant not to
   * attempt to fetch that specific item again, since it doesn't exist.
   *
   * @param artifact The artifact information used to publish.
   * @param item     The item that the negative is being published for.
   */
  public void publishNegative(Artifact artifact, String item) {
    File itemFile;
    try {
      itemFile = File.createTempFile("item", "item");
      itemFile.deleteOnExit();
    } catch (IOException e) {
      // This is okay, because negatives are only for performance and if we can't create one, we'll just
      // head out and try and fetch it again next time.
      return;
    }

    for (ProcessHandler process : processes) {
      try {
        process.publish(artifact, item + ".neg", itemFile);
      } catch (BuildException e) {
        // Continue since this is okay.
      }
    }
  }

  /**
   * Publishes a negative file for the artifact MetaData. This file is empty, but signals Savant not to
   * attempt to fetch that specific AMD file again, since it doesn't exist.
   *
   * @param artifact The artifact information used to publish.
   */
  public void publishNegativeMetaData(Artifact artifact) {
    File amdFile;
    try {
      amdFile = File.createTempFile("amd", "amd");
      amdFile.deleteOnExit();
    } catch (IOException e) {
      // This is okay, because negatives are only for performance and if we can't create one, we'll just
      // head out and try and fetch it again next time.
      return;
    }

    for (ProcessHandler process : processes) {
      try {
        process.publish(artifact, artifact.getArtifactNegativeMetaDataFile(), amdFile);
      } catch (BuildException e) {
        // Continue since this is okay.
      }
    }
  }

  /**
   * Deletes the item by removing it from all of the published locations. This is handled by
   * calling each of the publish processes in turn to remove the item. This method relies
   * on the {@link PublishWorkflowHandler} to setup any dependencies that the artifact might have. This is
   * done because it is assumed that the publish workflow will have access to the .deps file and
   * can parse it out.
   *
   * @param artifact The artifact information used to publish.
   * @param item     The name of the item being deleted.
   * @return True if the artifact was deleted, false if not. This is only true if a single process
   *         deleted the artifact. Therefore, all others might have failed, but this will still
   *         return true.
   */
  public boolean delete(Artifact artifact, String item) {
    boolean deleted = false;
    for (ProcessHandler process : processes) {
      deleted |= process.delete(artifact, item);
    }

    return deleted;
  }

  /**
   * Deletes all of the files that contain integration build versions.
   *
   * @param artifact The artifact information used to publish.
   */
  public void deleteIntegrationBuilds(Artifact artifact) {
    for (ProcessHandler process : processes) {
      process.deleteIntegrationBuilds(artifact);
    }
  }
}
