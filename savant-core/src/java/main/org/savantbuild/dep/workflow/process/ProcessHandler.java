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

import org.savantbuild.BuildException;
import org.savantbuild.dep.NegativeCacheException;
import org.savantbuild.dep.workflow.PublishWorkflowHandler;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactMetaData;
import org.savantbuild.io.DoesNotExistException;
import org.savantbuild.io.PermanentIOException;
import org.savantbuild.io.TemporaryIOException;

/**
 * <p>
 * This interface defines a workflow process that can be used for either publishing or for fetching.
 * </p>
 *
 * @author Brian Pontarelli
 */
public interface ProcessHandler {
  /**
   * <p>
   * Attempts to fetch the given item. The item is normally associated with the artifact, but might
   * be associated with a group or project. This method can use the artifact for logging or other
   * purposes, but should use the item String for fetching only.
   * </p>
   * <p/>
   * <p>
   * If the item is found, it should be published by calling the {@link PublishWorkflowHandler}.
   * </p>
   *
   * @param artifact               The artifact that the item is associated with.
   * @param item                   The name of the item being fetched. This item name should NOT include the path
   *                               information. This will be handled by the processes so that flattened namespacing and
   *                               other types of handling can be performed. This item should only be the name of the
   *                               item being fetched. For example, if the artifact MD5 file is being fetched this would
   *                               look like this: common-collections-2.1.jar.md5.
   * @param publishWorkflowHandler The PublishWorkflowHandler that is used to store the item if it can be found.
   * @return The file that references the item for use in paths and other constructs and never null.
   * @throws TemporaryIOException   If the Artifact item could not be fetched temporarily.
   * @throws PermanentIOException   If the Artifact item could not be fetched permanently.
   * @throws DoesNotExistException  If the Artifact item does not exist at all.
   * @throws NegativeCacheException If the Artifact item was previously negatively cached.
   */
  File fetch(Artifact artifact, String item, PublishWorkflowHandler publishWorkflowHandler)
    throws TemporaryIOException, PermanentIOException, DoesNotExistException, NegativeCacheException;

  /**
   * <p>
   * Attempts to fetch the meta data for the given Artifact. Since different processes can use
   * different file names or transitive dependency storage mechanisms, this method is separated
   * from the fetchItem method. However, the publish process should also be called, which will
   * convert the meta data information into a Savant specific .amd file.
   * </p>
   * <p/>
   * <p>
   * If the meta data are found, it should be published by calling the {@link PublishWorkflowHandler}
   * using the {@link PublishWorkflowHandler#publish(Artifact, String, File)} method
   * or whatever method is appropriate for processes. The publish workflow should always be called
   * when meta data are found.
   * </p>
   *
   * @param artifact               The artifact that the meta data are being fetched for.
   * @param publishWorkflowHandler The PublishWorkflowHandler that is used to store the meta data in a Savant
   *                               specific format.
   * @return The ArtifactMetaData objectand never null.
   * @throws TemporaryIOException   If the ArtifactMetaData could not be fetched temporarily.
   * @throws PermanentIOException   If the ArtifactMetaData could not be fetched permanantly.
   * @throws DoesNotExistException  If the ArtifactMetaData does not exist at all.
   * @throws NegativeCacheException If the Artifact item was previously negatively cached.
   */
  ArtifactMetaData fetchMetaData(Artifact artifact, PublishWorkflowHandler publishWorkflowHandler)
    throws TemporaryIOException, PermanentIOException, DoesNotExistException, NegativeCacheException;

  /**
   * Determines the version available for the given artifact.
   *
   * @param artifact The artifact to get the version for.
   * @return The version if one exists, otherwise null.
   */
  String determineVersion(Artifact artifact);

  /**
   * <p>
   * Attempts to publish the given item. The item is normally associated with the artifact, but might
   * be associated with a group or project. This method can use the artifact for logging or other
   * purposes, but should use the item String for publishing only.
   * </p>
   * <p/>
   * <p>
   * If the item is published in a manner that a file can be returned, that file should be returned
   * as it might be used to create paths or other constructs.
   * </p>
   *
   * @param artifact The artifact that the item might be associated with.
   * @param item     The name of the item to publish.
   * @param file     The file that is the item.
   * @return The file if the publish process stored the given file locally (local cache for example). Otherwise, this
   *         should return null.
   * @throws BuildException If there was any issue publishing.
   */
  File publish(Artifact artifact, String item, File file) throws BuildException;

  /**
   * <p/>
   * Attempts to delete the item. If the item was deleted successfully, then this method should return true.
   * <p/>
   *
   * @param artifact The artifact.
   * @param item     The item to deleted.
   * @return True if the item was deleted, false otherwise.
   * @throws BuildException If there was any issue deleting the item.
   */
  boolean delete(Artifact artifact, String item) throws BuildException;

  /**
   * Deletes the integration builds.
   *
   * @param artifact The artifact. This artifacts version is the next integration build version.
   */
  void deleteIntegrationBuilds(Artifact artifact);
}
