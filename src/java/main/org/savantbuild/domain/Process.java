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
package org.savantbuild.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class stores the attributes associated with a single workflow Process.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class Process {
  private final Map<String, String> attributes = new HashMap<String, String>();

  public Process() {
  }

  public Process(Map<String, String> attribtues) {
    this.attributes.putAll(attribtues);
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }
}
