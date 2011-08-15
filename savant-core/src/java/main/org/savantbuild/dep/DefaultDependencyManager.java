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
package org.savantbuild.dep;

import org.savantbuild.domain.Project;
import org.savantbuild.domain.PublishWorkflow;
import org.savantbuild.domain.Workflow;
import org.savantbuild.domain.Workflows;

import com.google.inject.Inject;

/**
 * <p>
 * This class is the default implementation of the dependency service.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultDependencyManager implements DependencyManager {
  private final DependencyResolver resolver;
  private final DependencyPublisher publisher;
  private final DependencyDeleter deleter;

  @Inject
  public DefaultDependencyManager(DependencyResolver resolver, DependencyPublisher publisher, DependencyDeleter deleter) {
    this.resolver = resolver;
    this.publisher = publisher;
    this.deleter = deleter;
  }

  /**
   * <p>
   * Determines the best workflow to use for the project. This is done by first looking for a workflow whose name is
   * the same os the project's group plus the project's name. If this is null, it begins to trim off the left part
   * of this string. Here's an example:
   * </p>
   * <pre>
   * org.example.group.projectName
   * org.example.group
   * org.example
   * org
   * </pre>
   * <p>
   * If this gets to the end and nothing is found, it tries to get the default workflow (using a key of null).
   * </p>
   *
   * @param workflows The workflows.
   * @param project   The project.
   * @return The workflow or null.
   */
  @Override
  public Workflow determineProjectWorkflow(Workflows workflows, Project project) {
    String key = project.getGroup() + "." + project.getName();
    Workflow workflow;
    while ((workflow = workflows.getWorkflows().get(key)) == null) {
      int index = key.lastIndexOf('.');
      if (index > 0) {
        key = key.substring(0, index);
      } else {
        break;
      }
    }

    if (workflow == null) {
      workflow = workflows.getWorkflows().get(null);
    }

    return workflow;
  }

  /**
   * <p>
   * Determines the best publish workflow to use for the project. This is done by first looking for a workflow whose
   * name is the same os the project's group plus the project's name. If this is null, it begins to trim off the left
   * part of this string. Here's an example:
   * </p>
   * <pre>
   * org.example.group:projectName
   * org.example.group
   * org.example
   * org
   * </pre>
   * <p>
   * If this gets to the end and nothing is found, it tries to get the default publish workflow (using a key of null).
   * </p>
   *
   * @param workflows        The workflows.
   * @param project          The project.
   * @param integrationBuild Controls if the workflow is a release or local publish workflow.
   * @return The publish workflow or null.
   */
  @Override
  public PublishWorkflow determineProjectPublishWorkflow(Workflows workflows, Project project, boolean integrationBuild) {
    String key = project.getGroup() + ":" + project.getName();
    PublishWorkflow workflow = null;
    while (workflow == null) {
      if (integrationBuild) {
        workflow = workflows.getIntegrationWorkflows().get(key);
      } else {
        workflow = workflows.getReleaseWorkflows().get(key);
      }

      if (workflow == null && key != null) {
        int index = key.lastIndexOf('.');
        if (index > 0) {
          key = key.substring(0, index);
        } else {
          key = null;
        }
      } else if (key == null) {
        break;
      }
    }

    return workflow;
  }

  /**
   * {@inheritDoc}
   */
  public DependencyResolver getResolver() {
    return resolver;
  }

  /**
   * {@inheritDoc}
   */
  public DependencyPublisher getPublisher() {
    return publisher;
  }

  /**
   * {@inheritDoc}
   */
  public DependencyDeleter getDeleter() {
    return deleter;
  }
}
