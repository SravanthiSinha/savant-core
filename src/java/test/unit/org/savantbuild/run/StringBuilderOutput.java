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
package org.savantbuild.run;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.savantbuild.run.output.DefaultOutput;
import org.savantbuild.run.output.Level;

/**
 * <p>
 * This class is a testing helper that prints log messages to a StringBuilder.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class StringBuilderOutput extends DefaultOutput {
  public Level level = Level.INFO;
  public StringBuilder build = new StringBuilder();

  @Override
  public void debug(String message) {
    if (Level.DEBUG.ordinal() >= level.ordinal()) {
      build.append(message).append("\n");
    }
  }

  @Override
  public void info(String message) {
    if (Level.INFO.ordinal() >= level.ordinal()) {
      build.append(message).append("\n");
    }
  }

  @Override
  public void warning(String message) {
    if (Level.WARNING.ordinal() >= level.ordinal()) {
      build.append(message).append("\n");
    }
  }

  @Override
  public void failure(String message) {
    if (Level.FAILURE.ordinal() >= level.ordinal()) {
      build.append(message).append("\n");
    }
  }

  @Override
  public void print(Level level, String message) {
    if (level.ordinal() >= this.level.ordinal()) {
      build.append(message);
    }
  }

  @Override
  public void println(Level level, String message) {
    if (level.ordinal() >= this.level.ordinal()) {
      build.append(message).append("\n");
    }
  }

  @Override
  public void println(Level level, Throwable t) {
    if (level.ordinal() >= this.level.ordinal()) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      t.printStackTrace(new PrintWriter(baos));
      build.append(baos.toString()).append("\n");
    }
  }

  @Override
  public void println(Level level, String message, Throwable t) {
    if (level.ordinal() >= this.level.ordinal()) {
      build.append(message).append("\n");
      println(level, t);
    }
  }
}
