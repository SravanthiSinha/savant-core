/*
 * Copyright (c) 2001-2011, Inversoft, All Rights Reserved
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
package org.savantbuild.dep;

import java.io.File;
import java.util.Map;

import org.savantbuild.domain.Dependencies;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Publication;
import org.savantbuild.domain.PublishWorkflow;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines the process for publishing the project's artifacts.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultDependencyPublisher.class)
public interface DependencyPublisher {
  /**
   * <p>
   * Publishes the given publication (artifact) using the given workflow. If the integration flag is true, this will
   * publish an integration build which means that the version of the project will be modified to include integration
   * build numbering.
   * </p>
   * <p>
   * Otherwise, this publishes a full release of the publication.
   * </p>
   *
   * @param project     The project used to get the publications dependencies and the version of the publication.
   * @param publication The publication to publish.
   * @param workflow    The workflow to use for publishing the publication.
   * @param integration Whether or not the publication is an integration build.
   * @param listeners   Listeners.
   * @return The file that the publication was published to if it was published locally.
   */
  File publish(Project project, Publication publication, PublishWorkflow workflow, boolean integration, DependencyListener... listeners);

  /**
   * <p>
   * Publishes all of the given publications (artifacts) using the given workflow. If the integration flag is true, this
   * will publish an integration build which means that the version of the project will be modified to include integration
   * build numbering.
   * </p>
   * <p>
   * Otherwise, this publishes a full release of the publications.
   * </p>
   *
   * @param project      The project used to get the publications dependencies and the version of the publication.
   * @param publications The publications to publish.
   * @param workflow     The workflow to use for publishing the publications.
   * @param integration  Whether or not the publications are integration builds.
   * @param listeners    Listeners.
   * @return The files that the publications were published to if they were published locally.
   */
  Map<Publication, File> publish(Project project, Iterable<Publication> publications, PublishWorkflow workflow, boolean integration, DependencyListener... listeners);

  /**
   * Checks if the given dependencies has any artifacts using integration builds. This is useful for checking it the
   * project depends on integration builds prior to a full release. Project's should never use integration builds
   * during a full release because this causes major issues with transitive dependencies.
   *
   * @param dependencies The dependencies to check.
   * @return True if the dependencies contain integration builds, false otherwise.
   */
  boolean hasIntegrations(Dependencies dependencies);
}
