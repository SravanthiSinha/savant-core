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

import org.savantbuild.domain.Project;
import org.savantbuild.domain.PublishWorkflow;
import org.savantbuild.domain.Workflow;
import org.savantbuild.domain.Workflows;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface is the main manager for handling the project's dependencies.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultDependencyManager.class)
public interface DependencyManager {
  /**
   * Determines the workflow that the project should use.
   *
   * @param workflows The workflows.
   * @param project   The project.
   * @return The workflow or null.
   */
  Workflow determineProjectWorkflow(Workflows workflows, Project project);

  /**
   * Determines the publish workflow handler that the project should use. If the integrationBuild flag is set, this
   * will be the local publish workflow. Otherwise, it will be the release publish workflow.
   *
   * @param workflows        The workflows configured for this machine.
   * @param project          The project.
   * @param integrationBuild Determines if the build is an integration build or release build.
   * @return The publish workflow or null.
   */
  PublishWorkflow determineProjectPublishWorkflow(Workflows workflows, Project project, boolean integrationBuild);

  /**
   * @return The dependency resolver.
   */
  DependencyResolver getResolver();

  /**
   * @return The dependency publisher.
   */
  DependencyPublisher getPublisher();

  /**
   * @return The dependency deleter.
   */
  DependencyDeleter getDeleter();
}
