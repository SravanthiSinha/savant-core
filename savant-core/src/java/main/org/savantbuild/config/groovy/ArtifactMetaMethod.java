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

import java.util.Map;

import org.savantbuild.BuildException;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;

import static org.savantbuild.util.StringTools.*;

/**
 * <p>
 * This class is the Groovy MetaMethod that handles the dependency information.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ArtifactMetaMethod extends AbstractMetaMethod {
  private static final String MESSAGE = "Invalid artifact definition. You must specify the group, name, and version of the " +
    "artifact like this:\n" +
    "  artifact(group: \"org.apache.commons\", name: \"commons-io\", version: \"1.0\")";

  public ArtifactMetaMethod(Class theClass) {
    super(theClass, Artifact.class, "artifact");
  }

  @Override
  public Object invoke(Object o, Object[] arguments) {
    if (arguments.length != 1) {
      throw new BuildException(MESSAGE);
    }

    Object[] argArray = (Object[]) arguments[0];
    if (argArray == null || argArray.length != 1 || !(argArray[0] instanceof Map)) {
      throw new BuildException(MESSAGE);
    }

    ArtifactGroup artifactGroup = ArtifactGroupMetaMethod.current;
    if (artifactGroup == null) {
      throw new BuildException("You cannot define an artifact outside of an artifactGroup definition.");
    }

    Map values = (Map) argArray[0];
    if (!values.containsKey("group") || !values.containsKey("name") || !values.containsKey("version")) {
      throw new BuildException(MESSAGE);
    }

    String project = safe(values.get("project"));
    String name = safe(values.get("name"));
    String type = safe(values.get("type"));
    if (project == null) {
      project = name;
    }

    if (type == null) {
      type = "jar";
    }

    Artifact artifact = new Artifact(safe(values.get("group")), project, name, safe(values.get("version")), type);
    artifactGroup.getArtifacts().add(artifact);
    return artifact;
  }
}
