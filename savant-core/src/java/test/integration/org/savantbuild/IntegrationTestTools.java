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
package org.savantbuild;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class contains testing methods.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class IntegrationTestTools {
  public static <T> Map<T, T> map(T... t) {
    Map<T, T> map = new HashMap<T, T>();
    for (int i = 0; i < t.length; i = i + 2) {
      map.put(t[i], t[i + 1]);
    }
    return map;
  }
}
