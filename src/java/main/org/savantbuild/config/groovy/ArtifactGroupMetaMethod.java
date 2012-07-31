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

import org.savantbuild.BuildException;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.Dependencies;

import groovy.lang.Closure;

/**
 * This class is the Groovy MetaMethod that handles the dependency information.
 *
 * @author Brian Pontarelli
 */
public class ArtifactGroupMetaMethod extends AbstractMetaMethod {
  // Package protected reference to the current artifactGroup.
  static ArtifactGroup current;

  private static final String MESSAGE = "Invalid artifactGroup definition. You must specify the type of the artifactGroup  " +
    "and must specify a block that defines the artifacts like this:\n" +
    "  artifactGroup(\"compile\") {\n" +
    "    artifact(group: \"org.apache.commons\", name: \"commons-io\", version: \"1.0\")\n" +
    "  }";

  public ArtifactGroupMetaMethod(Class theClass) {
    super(theClass, ArtifactGroup.class, "artifactGroup");
  }

  @Override
  public Object invoke(Object o, Object[] arguments) {
    if (arguments.length != 1) {
      throw new BuildException(MESSAGE);
    }

    Object[] argArray = (Object[]) arguments[0];
    if (argArray == null || argArray.length != 2) {
      throw new BuildException(MESSAGE);
    }

    Dependencies dependencies = DependenciesMetaMethod.current;
    if (dependencies == null) {
      throw new BuildException("You cannot define an artifactGroup outside of a dependencies definition.");
    }

    ArtifactGroup artifactGroup = new ArtifactGroup(argArray[0].toString());
    if (!(argArray[1] instanceof Closure)) {
      throw new BuildException(MESSAGE);
    }

    current = artifactGroup;
    Closure closure = (Closure) argArray[1];
    closure.call();
    current = null;

    if (dependencies.getArtifactGroups().containsKey(artifactGroup.getType())) {
      throw new BuildException("An artifactGroup named [" + artifactGroup.getType() + "] is already defined.");
    }

    dependencies.getArtifactGroups().put(artifactGroup.getType(), artifactGroup);
    return artifactGroup;
  }
}
