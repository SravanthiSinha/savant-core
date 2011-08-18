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
package org.savantbuild.config.groovy;

import java.util.Map;

import org.savantbuild.BuildException;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Publication;

import static org.savantbuild.util.StringTools.*;

/**
 * <p>
 * This class is the Groovy MetaMethod that handles the publications.
 * </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class PublicationMetaMethod extends AbstractMetaMethod {
  private static final String MESSAGE = "Invalid publication definition. You must specify the name, type and file of the " +
    "publication like this:\n" +
    "  publication(name: \"foo\", type: \"jar\", file: \"target/jars/foo.jar\")";

  private final Project project;

  public PublicationMetaMethod(Class theClass, Project project) {
    super(theClass, Publication.class, "publication");
    this.project = project;
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

    Map values = (Map) argArray[0];
    if (!values.containsKey("name") || !values.containsKey("type") || !values.containsKey("file")) {
      throw new BuildException(MESSAGE);
    }

    Publication publication = new Publication(safe(values.get("name")), safe(values.get("type")), safe(values.get("file")),
      safe(values.get("compatibility")), safe(values.get("dependencies")));
    project.getPublications().add(publication);
    return publication;
  }
}
