/*
 * Copyright (c) 2001-2010, Inversoft, All Rights Reserved
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
package org.savantbuild.config.groovy;

import org.savantbuild.config.PluginConfigurationService;
import org.savantbuild.dep.DependencyManager;
import org.savantbuild.domain.Context;
import org.savantbuild.domain.Project;
import org.savantbuild.run.TargetExecutor;

import groovy.lang.MetaClassImpl;

/**
 * This is the meta class that supports the Savant Build File DSL.
 *
 * @author Brian Pontarelli
 */
public class BuildMetaClass extends MetaClassImpl {
  public BuildMetaClass(DependencyManager manager, PluginConfigurationService pluginService, TargetExecutor targetExecutor,
                        Class<?> theClass, Context context, Project project) {
    super(theClass);
    super.addMetaMethod(new ProjectMetaMethod(manager, theClass, context, project));
    super.addMetaMethod(new PluginMetaMethod(pluginService, theClass, context, project));
    super.addMetaMethod(new DependenciesMetaMethod(theClass, project));
    super.addMetaMethod(new ArtifactGroupMetaMethod(theClass));
    super.addMetaMethod(new ArtifactMetaMethod(theClass));
    super.addMetaMethod(new PublicationMetaMethod(theClass, project));
    super.addMetaMethod(new TargetMetaMethod(targetExecutor, theClass, project, project));
  }
}
