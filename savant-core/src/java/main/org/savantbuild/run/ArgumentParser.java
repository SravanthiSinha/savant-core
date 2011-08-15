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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines how the command-line arguments are parsed by Savant.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultArgumentParser.class)
public interface ArgumentParser {
  /**
   * Parses the command line.
   *
   * @param options The CLI options.
   * @param args    The arguments.
   * @return The CommandLine.
   * @throws ParseException If parsing fails.
   */
  CommandLine parse(Options options, String... args) throws ParseException;

  /**
   * Parses the target options.
   *
   * @param options The CLI options.
   * @param args    The arguments.
   * @return The CommandLine.
   * @throws ParseException If parsing fails.
   */
  CommandLine parseTarget(Options options, String... args) throws ParseException;
}
