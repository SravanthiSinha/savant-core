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

import org.savantbuild.BuildException;
import org.savantbuild.util.ErrorList;

/**
 * <p>
 * This exception denotes a temporary failure that will cease to occur
 * when performing the same IO operation multiple times.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class TemporaryIOException extends BuildException {
  public TemporaryIOException() {
    super();
  }

  public TemporaryIOException(String message) {
    super(message);
  }

  public TemporaryIOException(String message, ErrorList errors) {
    super(message, errors);
  }

  public TemporaryIOException(String message, Throwable cause) {
    super(message, cause);
  }

  public TemporaryIOException(String message, Throwable cause, ErrorList errors) {
    super(message, cause, errors);
  }

  public TemporaryIOException(Throwable cause) {
    super(cause);
  }

  public TemporaryIOException(Throwable cause, ErrorList errors) {
    super(cause, errors);
  }
}
