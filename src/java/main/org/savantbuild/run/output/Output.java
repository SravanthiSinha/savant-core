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

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines the method that is used to print out messages from Savant.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultOutput.class)
public interface Output {
  /**
   * Prints out the given message at the debug level.
   *
   * @param message The message.
   */
  void debug(String message);

  /**
   * Prints out the given message at the info level.
   *
   * @param message The message.
   */
  void info(String message);

  /**
   * Prints out the given message at the warning level.
   *
   * @param message The message.
   */
  void warning(String message);

  /**
   * Prints out the given message at the failure level.
   *
   * @param message The message.
   */
  void failure(String message);

  /**
   * Prints on the given message without a newline.
   *
   * @param level   The level.
   * @param message The message.
   */
  void print(Level level, String message);

  /**
   * Prints out the given message with a newline.
   *
   * @param level   The level.
   * @param message The message.
   */
  void println(Level level, String message);

  /**
   * Prints out the given exception with a newline.
   *
   * @param level The level.
   * @param t     The exception.
   */
  void println(Level level, Throwable t);

  /**
   * Prints out the given message and exception with a newline.
   *
   * @param level   The level.
   * @param message The message.
   * @param t       The exception.
   */
  void println(Level level, String message, Throwable t);

  /**
   * @return The output level.
   */
  Level getLevel();

  /**
   * Sets the output level.
   *
   * @param level The level.
   */
  void setLevel(Level level);
}
