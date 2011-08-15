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

import org.savantbuild.dep.NegativeCacheException;
import org.savantbuild.dep.workflow.PublishWorkflowHandler;
import org.savantbuild.dep.xml.ArtifactTools;
import org.savantbuild.dep.xml.MavenTools;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactMetaData;
import org.savantbuild.io.DoesNotExistException;
import org.savantbuild.io.PermanentIOException;
import org.savantbuild.io.TemporaryIOException;

/**
 * <p>
 * This class is an abstract process handler that provides some of the methods that are common
 * between all process handlers.
 * </p>
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractProcessHandler implements ProcessHandler {
  /**
   * Calls the fetch method passing in the artifact MetaData file as the item. This is the standard
   * method and can be overridden for different behavior.
   *
   * @param artifact               The artifact whose MetaData to fetch.
   * @param publishWorkflowHandler The publish workflow called to store the MetaData.
   * @return The MetaData or null if there are none.
   */
  @Override
  public ArtifactMetaData fetchMetaData(Artifact artifact, PublishWorkflowHandler publishWorkflowHandler)
    throws TemporaryIOException, PermanentIOException, DoesNotExistException, NegativeCacheException {
    try {
      File amdFile = fetch(artifact, artifact.getArtifactMetaDataFile(), publishWorkflowHandler);
      return ArtifactTools.parseArtifactMetaData(amdFile);
    } catch (DoesNotExistException e) {
      try {
        File file = fetch(artifact, MavenTools.pomName(artifact), publishWorkflowHandler);
        ArtifactMetaData amd = MavenTools.parsePOM(file);
        File amdFile = ArtifactTools.generateXML(amd);
        publishWorkflowHandler.publish(artifact, artifact.getArtifactMetaDataFile(), amdFile);
        return amd;
      } catch (DoesNotExistException e1) {
        throw new DoesNotExistException("ArtifactMetaData file for artifact [" + artifact + "] does not exist.");
      }
    }
  }
}
