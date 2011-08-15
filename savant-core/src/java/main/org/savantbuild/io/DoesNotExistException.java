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
 * This exception denotes a resource doesn't exist.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DoesNotExistException extends BuildException {
  public DoesNotExistException() {
    super();
  }

  public DoesNotExistException(String message) {
    super(message);
  }

  public DoesNotExistException(String message, ErrorList errors) {
    super(message, errors);
  }

  public DoesNotExistException(String message, Throwable cause) {
    super(message, cause);
  }

  public DoesNotExistException(String message, Throwable cause, ErrorList errors) {
    super(message, cause, errors);
  }

  public DoesNotExistException(Throwable cause) {
    super(cause);
  }

  public DoesNotExistException(Throwable cause, ErrorList errors) {
    super(cause, errors);
  }
}
