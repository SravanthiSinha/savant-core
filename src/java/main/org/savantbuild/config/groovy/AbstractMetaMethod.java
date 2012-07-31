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

import java.lang.reflect.Modifier;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;

import groovy.lang.MetaMethod;

/**
 * This class is an abstract meta method for the Groovy DSL support.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractMetaMethod extends MetaMethod {
  private final Class<?> type;
  private final Class<?> returnType;
  private final String name;

  protected AbstractMetaMethod(Class<?> type, Class<?> returnType, String name) {
    super(new Class<?>[]{Object[].class});
    this.type = type;
    this.returnType = returnType;
    this.name = name;
  }

  @Override
  public int getModifiers() {
    return Modifier.PUBLIC;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Class getReturnType() {
    return returnType;
  }

  @Override
  public CachedClass getDeclaringClass() {
    return new CachedClass(type, ClassInfo.getClassInfo(type));
  }
}
