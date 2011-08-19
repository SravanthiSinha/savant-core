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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.savantbuild.util.StringTools;

/**
 * <p>
 * This class provides File utility methods.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class FileTools {
  /**
   * Copies to contents of one file to another file.
   *
   * @param from The from file.
   * @param to   The to file or directory.
   * @throws IOException If the copy files or the to file already exists and can't be deleted.
   */
  public static void copy(File from, File to) throws IOException {
    // If the to file is a directory, construct the destination regular file
    if (to.isDirectory()) {
      to = new File(to, from.getName());
    }

    // Delete the to file if it exists if the from file is newer
    if (to.exists() && !to.delete()) {
      throw new IOException("Unable overwrite file [" + to.getAbsolutePath() + "]");
    }

    FileInputStream in = new FileInputStream(from);
    FileOutputStream out = new FileOutputStream(to);
    int len;
    byte[] buf = new byte[8192];
    do {
      len = in.read(buf);
      if (len > 0) {
        out.write(buf, 0, len);
      }
    } while (len > 0);

    in.close();
    out.close();
  }

  /**
   * Calculates the MD5 sum for the given file.
   *
   * @param file The file to MD5.
   * @return The MD5 sum and never null.
   * @throws IOException If the file could not be MD5 summed.
   */
  public static MD5 md5(File file) throws IOException {
    if (!file.exists() || file.isDirectory()) {
      throw new IllegalArgumentException("Invalid file to MD5 [" + file.getAbsolutePath() + "]");
    }

    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("MD5");
      digest.reset();
    } catch (NoSuchAlgorithmException e) {
      System.err.println("Unable to locate MD5 algorithm");
      System.exit(1);
    }

    // Read in the file in blocks while doing the MD5 sum
    FileInputStream fis = new FileInputStream(file);
    BufferedInputStream bis = new BufferedInputStream(fis);
    DigestInputStream dis = new DigestInputStream(bis, digest);
    dis.on(true);

    byte[] ba = new byte[1024];
    while (dis.read(ba, 0, 1024) != -1) ;

    dis.close();
    bis.close();
    fis.close();

    byte[] md5 = digest.digest();
    return new MD5(StringTools.toHex(md5), md5, file.getName());
  }

  /**
   * Prunes the given file.
   *
   * @param f The file.
   */
  public static void prune(File f) {
    if (!f.exists()) {
      return;
    }

    File[] files = f.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        file.delete();
      } else {
        prune(file);
      }
    }

    f.delete();
  }

  public static void write(File file, String s) throws IOException {
    FileWriter fw = new FileWriter(file);
    fw.append(s);
    fw.flush();
    fw.close();
  }

  public static String read(File file) throws IOException {
    char[] ca = new char[1024];
    FileReader fr = new FileReader(file);
    int read;
    StringBuilder build = new StringBuilder();
    while ((read = fr.read(ca)) != -1) {
      build.append(ca, 0, read);
    }

    return build.toString();
  }

  public static File writeMD5(MD5 md5) throws IOException {
    File tempMD5 = File.createTempFile("savant-publish", "md5");
    tempMD5.deleteOnExit();
    FileTools.write(tempMD5, md5.sum);
    return tempMD5;
  }

  /**
   * Unzips the given JAR file.
   *
   * @param file The JAR file to unzip.
   * @param dir  The directory to unzip to.
   * @throws IOException If the unzip fails.
   */
  public static void unzip(File file, File dir) throws IOException {
    JarInputStream jis = new JarInputStream(new FileInputStream(file));
    JarEntry entry = jis.getNextJarEntry();
    byte[] buf = new byte[1024];
    while (entry != null) {
      if (!entry.isDirectory()) {
        File out = new File(dir, entry.getName());
        if (!out.getParentFile().exists() && !out.getParentFile().mkdirs()) {
          throw new IOException("Unable to create directory [" + out.getParentFile().getAbsolutePath() +
            "] while expanding the JAR file [" + file.getAbsolutePath() + "]");
        }

        FileOutputStream fos = new FileOutputStream(out);
        int len;
        while ((len = jis.read(buf)) > 0) {
          fos.write(buf, 0, len);
        }
        fos.flush();
        fos.close();
      }

      entry = jis.getNextJarEntry();
    }
  }

    /**
     * Finds the root file (the parent directory) of the given file.
     *
     * @param   file The file to find the parent of.
     * @return The parent.
     */
    public static File getRoot(File file) {
        File parent = file;
        while (true) {
            File tempParent = parent.getParentFile();
            if (tempParent == null) {
                break;
            } else {
                parent = tempParent;
            }
        }
        return parent;
    }
}
