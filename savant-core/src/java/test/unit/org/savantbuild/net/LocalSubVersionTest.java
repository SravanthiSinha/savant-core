/*
 * Copyright (c) 2001-2011, Inversoft, All Rights Reserved
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
package org.savantbuild.net;

import java.io.File;
import java.io.IOException;

import org.savantbuild.io.FileTools;
import org.savantbuild.net.LocalSubVersion.StatusHandler;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p>
 * This class does unit tests for the SubVersion class.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class LocalSubVersionTest {
  @Test(enabled = true)
  public void url() {
    String url = LocalSubVersion.getProjectURL(new File("."));
    assertEquals(url, "https://savant-build.googlecode.com/svn/2.0/savant-core/trunk");
  }

  @Test(enabled = true)
  public void base() {
    String url = LocalSubVersion.getProjectBaseURL(new File("."));
    assertEquals(url, "https://savant-build.googlecode.com/svn/2.0/savant-core");
  }

  @Test(enabled = true)
  public void location() {
    assertEquals(LocalSubVersion.determineLocation("https://savant-build.googlecode.com/svn/2.0/savant-core/trunk"), "trunk");
    assertEquals(LocalSubVersion.determineLocation("https://savant-build.googlecode.com/svn/2.0/savant-core/branches/1.0"), "branch");
    assertEquals(LocalSubVersion.determineLocation("https://savant-build.googlecode.com/svn/2.0/savant-core/tags/1.0.1"), "tag");

    try {
      assertEquals(LocalSubVersion.determineLocation("https://savant-build.googlecode.com/svn/2.0/savant-core"), "tag");
      fail("Should have failed");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test(enabled = true)
  public void branch() {
    assertEquals(LocalSubVersion.determineBranch("https://savant-build.googlecode.com/svn/2.0/savant-core/branches/1.0"), "1.0");
    assertNull(LocalSubVersion.determineBranch("https://savant-build.googlecode.com/svn/2.0/savant-core/tags/1.0.1"));
  }

  @Test(enabled = true)
  public void root() {
    assertEquals(LocalSubVersion.determineRoot("https://savant-build.googlecode.com/svn/2.0/savant-core/branches/1.0"), "https://savant-build.googlecode.com/svn/2.0/savant-core");
    assertEquals(LocalSubVersion.determineRoot("https://savant-build.googlecode.com/svn/2.0/savant-core/tags/1.0.1"), "https://savant-build.googlecode.com/svn/2.0/savant-core");
  }

  @Test
  public void status() throws IOException {
    FileTools.write(new File("src/java/test/unit/org/savantbuild/net/svn-changed.txt"), "" + System.currentTimeMillis());

    final ThreadLocal<File> holder = new ThreadLocal<File>();
    LocalSubVersion.doStatus(new File("."), new StatusHandler() {
      @Override
      public void handle(File file) {
        if (file.getName().equals("svn-changed.txt")) {
          holder.set(file);
        }
      }
    });

    assertNotNull(holder.get());
  }
}
