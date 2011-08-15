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
package org.savantbuild.config.groovy;

import org.savantbuild.BuildException;
import org.savantbuild.domain.Project;

import groovy.lang.Closure;

/**
 * <p>
 * This class is the Groovy MetaMethod that handles the plugin init call.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class InitMetaMethod extends AbstractMetaMethod {
  private static final String MESSAGE = "Invalid plugin init definition. You must supply a body like this:\n" +
    "  init {\n" +
    "    // body\n" +
    "  }";

  public InitMetaMethod(Class theClass) {
    super(theClass, Project.class, "init");
  }

  @Override
  public Object invoke(Object o, Object[] arguments) {
    if (arguments.length != 1 || ((Object[]) arguments[0]).length != 1) {
      throw new BuildException(MESSAGE);
    }

    Object arg = ((Object[]) arguments[0])[0];
    if (!(arg instanceof Closure)) {
      throw new BuildException(MESSAGE);
    }

    Closure closure = (Closure) arg;
    return closure.call();
  }
}
