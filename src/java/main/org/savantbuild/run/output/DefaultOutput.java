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
package org.savantbuild.run.output;

import com.google.inject.Singleton;

/**
 * <p>
 * This is the default output.
 * </p>
 *
 * @author Brian Pontarelli
 */
@Singleton
public class DefaultOutput implements Output {
  private Level level = Level.INFO;

  @Override
  public void debug(String message) {
    if (Level.DEBUG.ordinal() >= this.level.ordinal()) {
      System.out.println(message);
    }
  }

  @Override
  public void info(String message) {
    if (Level.INFO.ordinal() >= this.level.ordinal()) {
      System.out.println(message);
    }
  }

  @Override
  public void warning(String message) {
    if (Level.WARNING.ordinal() >= this.level.ordinal()) {
      System.out.println(message);
    }
  }

  @Override
  public void failure(String message) {
    if (Level.FAILURE.ordinal() >= this.level.ordinal()) {
      System.out.println(message);
    }
  }

  @Override
  public void print(Level level, String message) {
    if (level.ordinal() >= this.level.ordinal()) {
      System.out.print(message);
    }
  }

  @Override
  public void println(Level level, String message) {
    if (level.ordinal() >= this.level.ordinal()) {
      System.out.println(message);
    }
  }

  @Override
  public void println(Level level, Throwable t) {
    if (level.ordinal() >= this.level.ordinal()) {
      t.printStackTrace(System.out);
    }
  }

  @Override
  public void println(Level level, String message, Throwable t) {
    if (level.ordinal() >= this.level.ordinal()) {
      System.out.println(message);
      t.printStackTrace(System.out);
    }
  }

  @Override
  public Level getLevel() {
    return level;
  }

  @Override
  public void setLevel(Level level) {
    this.level = level;
  }
}
