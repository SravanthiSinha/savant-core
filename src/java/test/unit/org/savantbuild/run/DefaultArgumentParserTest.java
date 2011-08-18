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
import org.apache.commons.cli.PosixParser;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p>
 * This class tests the argument parser.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultArgumentParserTest {
  @Test
  public void version() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine line = parser.parse(Main.options, "-v");
    assertTrue(line.hasOption("version"));
    assertTrue(line.hasOption('v'));

    line = parser.parse(Main.options, "--version");
    assertTrue(line.hasOption("version"));
    assertTrue(line.hasOption('v'));
  }

  @Test
  public void help() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine line = parser.parse(Main.options, "-h");
    assertTrue(line.hasOption("help"));
    assertTrue(line.hasOption('h'));

    line = parser.parse(Main.options, "--help");
    assertTrue(line.hasOption("help"));
    assertTrue(line.hasOption('h'));
  }

  @Test
  public void file() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine line = parser.parse(Main.options, "-f", "build.xml", "test");
    assertTrue(line.hasOption("file"));
    assertTrue(line.hasOption('f'));
    assertEquals(line.getOptionValue("file"), "build.xml");
    assertEquals(line.getOptionValue('f'), "build.xml");

    line = parser.parse(Main.options, "--file", "build.xml", "test");
    assertTrue(line.hasOption("file"));
    assertTrue(line.hasOption('f'));
    assertEquals(line.getOptionValue("file"), "build.xml");
    assertEquals(line.getOptionValue('f'), "build.xml");

    try {
      parser.parse(Main.options, "--file", "-h", "test");
      fail("Should have failed");
    } catch (ParseException e) {
      // Expected
    }
  }

  @Test
  public void args() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    CommandLine line = parser.parse(Main.options, "foo", "bar", "--test=foo");
    assertFalse(line.hasOption("help"));
    assertFalse(line.hasOption("version"));
    assertEquals(line.getArgs()[0], "foo");
    assertEquals(line.getArgs()[1], "bar");
    assertEquals(line.getArgs()[2], "--test=foo");
  }

  @Test
  public void target() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    Options options = new Options();
    options.addOption("test", true, "Testing 1");
    options.addOption("test2", false, "Testing 2");
    CommandLine line = parser.parseTarget(options, "--test=foo", "--test2");
    assertTrue(line.hasOption("test"));
    assertEquals(line.getOptionValue("test"), "foo");
    assertTrue(line.hasOption("test2"));
    assertNull(line.getOptionValue("test2"));
  }

  @Test
  public void missingValueTargetFailure() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    Options options = new Options();
    options.addOption("test", true, "Testing 1");
    options.addOption("test2", false, "Testing 2");
    try {
      parser.parseTarget(options, "--test");
      fail("Should have failed");
    } catch (ParseException e) {
      // Expected
    }
  }

  @Test
  public void hasUnexpectedValueTarget() throws ParseException {
    DefaultArgumentParser parser = new DefaultArgumentParser(new PosixParser());
    Options options = new Options();
    options.addOption("test", true, "Testing 1");
    options.addOption("test2", false, "Testing 2");

    CommandLine line = parser.parseTarget(options, "--test2=foo", "--test=foo");
    assertTrue(line.hasOption("test"));
    assertEquals(line.getOptionValue("test"), "foo");
    assertTrue(line.hasOption("test2"));
    assertNull(line.getOptionValue("test2"));
  }
}
