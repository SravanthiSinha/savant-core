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
package org.savantbuild.io;

import java.io.IOException;

/**
 * <p>
 * This is a runtime exception that can be thrown for MD5 failures.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class MD5Exception extends IOException {
  public MD5Exception() {
    super();
  }

  public MD5Exception(String message) {
    super(message);
  }

  public MD5Exception(String message, Throwable cause) {
    super(message, cause);
  }

  public MD5Exception(Throwable cause) {
    super(cause);
  }
}
