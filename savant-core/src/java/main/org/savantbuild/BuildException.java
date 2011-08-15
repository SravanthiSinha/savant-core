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
package org.savantbuild;

import org.savantbuild.util.ErrorList;

/**
 * <p>
 * This is the main build exception.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class BuildException extends RuntimeException {
  private final ErrorList errors;
  private String fileName;

  public BuildException() {
    this(null, null, null);
  }

  public BuildException(ErrorList errors) {
    this.errors = errors;
  }

  public BuildException(String message) {
    this(message, (ErrorList) null);
  }

  public BuildException(String message, Throwable cause) {
    this(message, cause, null);
  }

  public BuildException(Throwable cause) {
    this(cause, null);
  }

  public BuildException(String message, ErrorList errors) {
    super(message);
    this.errors = errors;
  }

  public BuildException(String message, Throwable cause, ErrorList errors) {
    super(message, cause);
    this.errors = errors;
  }

  public BuildException(Throwable cause, ErrorList errors) {
    super(cause);
    this.errors = errors;
  }

  public ErrorList getErrors() {
    return errors;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String toString() {
    return super.toString() + (errors != null ? "\n" + errors.toString() : "");
  }
}
