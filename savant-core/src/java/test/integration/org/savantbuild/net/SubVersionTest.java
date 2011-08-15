/*
 * Copyright (c) 2001-2006, Inversoft, All Rights Reserved
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
package org.savantbuild.net;

import java.io.File;
import java.io.IOException;

import org.savantbuild.io.FileTools;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p>
 * This class tests the SubVersion wrapper.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class SubVersionTest {
  @Test(enabled = true)
  public void badRepository() {
    SubVersion svn = new SubVersion("https://savant-build.googlecode.com/svn/svn-test/bad");
    assertFalse(svn.isExists());
  }

  @Test(enabled = true)
  public void importFile() throws IOException {
    SubVersion svn = new SubVersion("https://savant-build.googlecode.com/svn/svn-test/good");
    assertTrue(svn.isExists());
    assertTrue(svn.isDirectory());
    assertFalse(svn.isFile());

    // Make a temp file
    File temp = File.createTempFile("foo", "bar");
    temp.deleteOnExit();
    FileTools.write(temp, "Hello world");

    // Import it
    long now = System.currentTimeMillis();
    svn.doImport("/svn-test/import" + now + "/file" + now, temp);
  }

  @Test(enabled = true)
  public void export() throws IOException {
    SubVersion svn = new SubVersion("http://savant-build.googlecode.com/svn/svn-test/good");
    assertTrue(svn.isExists());
    assertTrue(svn.isDirectory());
    assertFalse(svn.isFile());

    // Make a temp file
    File temp = File.createTempFile("foo", "bar");
    temp.deleteOnExit();

    // Export it
    svn.doExport("file", temp);
    assertTrue(temp.exists());
    String content = FileTools.read(temp);
    assertEquals("Testing export", content.trim());
  }

  @Test(enabled = true)
  public void exportNested() throws IOException {
    SubVersion svn = new SubVersion("http://savant-build.googlecode.com/svn/svn-test/good");
    assertTrue(svn.isExists());
    assertTrue(svn.isDirectory());
    assertFalse(svn.isFile());

    // Make a temp file
    File temp = File.createTempFile("foo", "bar");
    temp.deleteOnExit();

    // Export it
    svn.doExport("nested/file", temp);
    assertTrue(temp.exists());
    String content = FileTools.read(temp);
    assertEquals("Testing nested export", content.trim());
  }

  @Test(enabled = true)
  public void exportDir() throws IOException {
    SubVersion svn = new SubVersion("http://savant-build.googlecode.com/svn/svn-test");
    assertTrue(svn.isExists());
    assertTrue(svn.isDirectory());
    assertFalse(svn.isFile());

    // Export it
    File dir = new File("target/test/svn/export");
    FileTools.prune(dir);
    svn.doExport("good", dir);
    assertTrue(dir.isDirectory());
    assertTrue(new File(dir, "nested/file").isFile());
  }

  @Test(enabled = true)
  public void checkoutDir() throws IOException {
    SubVersion svn = new SubVersion("http://savant-build.googlecode.com/svn/svn-test");
    assertTrue(svn.isExists());
    assertTrue(svn.isDirectory());
    assertFalse(svn.isFile());

    // Export it
    File dir = new File("target/test/svn/checkout");
    FileTools.prune(dir);
    svn.doCheckout("good", dir);
    assertTrue(dir.isDirectory());
    assertTrue(new File(dir, "nested/file").isFile());
    assertTrue(new File(dir, ".svn").isDirectory());
    FileTools.prune(dir);
  }
}
