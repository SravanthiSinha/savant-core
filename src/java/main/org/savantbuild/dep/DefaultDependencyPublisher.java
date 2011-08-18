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
package org.savantbuild.dep;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.savantbuild.BuildException;
import org.savantbuild.dep.version.ArtifactVersionTools;
import org.savantbuild.dep.workflow.PublishWorkflowHandler;
import org.savantbuild.dep.xml.ArtifactTools;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactMetaData;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Publication;
import org.savantbuild.domain.PublishWorkflow;
import org.savantbuild.io.FileTools;
import org.savantbuild.io.MD5;
import org.savantbuild.run.output.Output;

import com.google.inject.Inject;
import static java.util.Arrays.*;

/**
 * <p>
 * This is the default implementation of the publisher. This uses the domain objects given to publish the artifacts.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultDependencyPublisher implements DependencyPublisher {
  private final Output output;

  @Inject
  public DefaultDependencyPublisher(Output output) {
    this.output = output;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public File publish(Project project, Publication publication, PublishWorkflow workflow, boolean integration, DependencyListener... listeners) {
    Map<Publication, File> results = publish(project, asList(publication), workflow, integration, listeners);
    return results.get(publication);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<Publication, File> publish(Project project, Iterable<Publication> publications, PublishWorkflow workflow, boolean integration, DependencyListener... listeners) {
    if (workflow == null) {
      throw new BuildException("No PublishWorkflow ws given in order to publish the projects artifacts (publications). " +
        "Ensure that you have properly configured a publish workflow in the ~/.savant/workflows.savant file or that " +
        "you are using the default publish workflows.");
    }

    if (publications == null || !publications.iterator().hasNext()) {
      throw new BuildException("No artifacts (publications) were given to publish. This could be because you haven't " +
        "defined any or that you have called the Publisher incorrect.");
    }

    output.info("Publishing the project's artifacts (publications)");

    String integrationVersion = null;
    if (integration) {
      integrationVersion = project.getVersion() + "-IB" + dateTimeString();
    }

    Map<Publication, File> results = new HashMap<Publication, File>();
    PublishWorkflowHandler handler = PublishWorkflowHandler.build(workflow, output);
    for (Publication publication : publications) {
      String depName = publication.getDependencies();
      Dependencies deps = project.getDependencies().get(depName);
      if (depName != null && deps == null) {
        throw new BuildException("Invalid dependencies [" + depName + "] for release publication that is defined in the project.xml file.");
      }

      // Create an artifact that will be published.
      Artifact forPath;
      Artifact forName;
      if (integrationVersion != null) {
        forPath = new Artifact(project.getGroup(), project.getName(), publication.getName(), project.getVersion() + "-{integration}", publication.getType());
        forName = new Artifact(project.getGroup(), project.getName(), publication.getName(), integrationVersion, publication.getType());
      } else {
        forPath = new Artifact(project.getGroup(), project.getName(), publication.getName(), project.getVersion(), publication.getType());
        forName = forPath;
      }

      // Clean old integration builds files. This will include the source JARs, AMD files, artifacts, MD5 files
      // and any old neg files. This will not include any extra items.
      if (!integration) {
        handler.deleteIntegrationBuilds(forPath);
      }

      // Publish the artifact itself
      File file = new File(publication.getFile());
      if (!file.exists() || file.isDirectory()) {
        throw new BuildException("Invalid file defined in a publication element [" + publication.getFile() + "]");
      }

      // Publish the artifact
      File publishedFile = handler.publish(forPath, forName.getArtifactFile(), file);
      results.put(publication, publishedFile);
      md5(handler, forPath, forName.getArtifactFile(), file);

      // Publish the MetaData
      ArtifactMetaData amd = new ArtifactMetaData(deps, publication.getCompatibility());
      File amdFile = ArtifactTools.generateXML(amd);
      handler.publish(forPath, forName.getArtifactMetaDataFile(), amdFile);
      md5(handler, forPath, forName.getArtifactMetaDataFile(), amdFile);

      // Publish the source JAR if it exists next to the artifact file
      String name = file.getName();
      if (name.endsWith(publication.getType())) {
        name = name.substring(0, name.length() - publication.getType().length() - 1) + "-src." + publication.getType();
        File sourceJAR = new File(file.getParentFile(), name);
        if (sourceJAR.isFile()) {
          handler.publish(forPath, forName.getArtifactSourceFile(), sourceJAR);
          md5(handler, forPath, forName.getArtifactSourceFile(), sourceJAR);
        }
      }

      for (DependencyListener listener : listeners) {
        listener.artifactPublished(forName);
      }
    }

    return results;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasIntegrations(Dependencies dependencies) {
    Set<Artifact> artifacts = dependencies.getAllArtifacts();
    for (Artifact a : artifacts) {
      if (a.getVersion().endsWith(ArtifactVersionTools.INTEGRATION)) {
        return true;
//                throw new BuildException("Unable to publish project artifact because the release " +
//                    "has a dependency on the artifact [" + a + "] that is using integration build versions.");
      }
    }

    return false;
  }

  /**
   * @return The current UTC date time string for integration builds.
   */
  private String dateTimeString() {
    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    return df.format(new Date());
  }

  private void md5(PublishWorkflowHandler handler, Artifact artifact, String item, File file) {
    try {
      MD5 md5 = FileTools.md5(file);
      File md5File = FileTools.writeMD5(md5);
      handler.publish(artifact, item + ".md5", md5File);
      md5File.delete();
    } catch (IOException e) {
      throw new BuildException("Unable to generate MD5 checksum for the publication [" + artifact + "]", e);
    }
  }
}
