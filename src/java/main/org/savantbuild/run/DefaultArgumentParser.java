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
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.inject.Inject;

/**
 * <p>
 * This is the default implementation of the argument parser. It uses the CommandLineParser that is
 * bound into Guice to perform the CLI parsing.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultArgumentParser implements ArgumentParser {
  private final CommandLineParser parser;

  @Inject
  public DefaultArgumentParser(CommandLineParser parser) {
    this.parser = parser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommandLine parse(Options options, String... args) throws ParseException {
    return parser.parse(options, args, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommandLine parseTarget(Options options, String... args) throws ParseException {
    return parser.parse(options, args, false);
  }
}
