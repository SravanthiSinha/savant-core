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
package org.savantbuild.net;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

import org.savantbuild.BuildException;
import org.savantbuild.io.DoesNotExistException;
import org.savantbuild.io.IOTools;
import org.savantbuild.io.MD5;
import org.savantbuild.io.PermanentIOException;
import org.savantbuild.io.TemporaryIOException;

import sun.misc.BASE64Encoder;

/**
 * <p>
 * This class provides toolkit methods for helping work with URLs and URIs and other network classes.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class NetTools {
  /**
   * Builds a URI from the given parts. These are concatenated together with slashes, depending on the endings of each.
   *
   * @param parts The parts
   * @return The URI.
   */
  public static URI build(String... parts) {
    try {
      StringBuilder build = new StringBuilder(parts[0]);
      for (int i = 1; i < parts.length; i++) {
        boolean endSlash = build.charAt(build.length() - 1) == '/';
        boolean startSlash = parts[i].startsWith("/");
        if (!endSlash && !startSlash) {
          build.append("/");
        }

        String part = parts[i];
        if (endSlash && startSlash) {
          part = parts[i].substring(1);
        }

        boolean first = true;
        String[] splits = part.split("/");
        for (String split : splits) {
          if (!first) {
            build.append("/");
          } else {
            first = false;
          }

          build.append(URLEncoder.encode(split, "UTF-8"));
        }
      }

      return new URI(build.toString());
    } catch (URISyntaxException e) {
      throw new BuildException(e);
    } catch (UnsupportedEncodingException e) {
      throw new BuildException(e);
    }
  }

  /**
   * Downloads the resource given.
   *
   * @param uri      The resource.
   * @param username (Optional) The username that might be used to connect to the resource.
   * @param password (Optional) The password that might be used to connect to the resource.
   * @param md5      (Optional) The MD5 of the resource (to verify).
   * @return A temp file that stores the resource.
   * @throws DoesNotExistException If the URI doesn't exist.
   * @throws TemporaryIOException  If there is a network error that might correct itself.
   * @throws PermanentIOException  If there is a network error or other error that won't correct itself.
   */
  public static File downloadToFile(final URI uri, final String username, final String password, final MD5 md5)
    throws DoesNotExistException, TemporaryIOException, PermanentIOException {
    return IOTools.protectIO(new Callable<File>() {
      @Override
      public File call() throws Exception {
        File file = File.createTempFile("savant-net-tools", "download");
        file.deleteOnExit();
        FileOutputStream os = new FileOutputStream(file);
        download(uri, username, password, os, md5);
        return file;
      }
    });
  }

  /**
   * Reads the contents of the given URI in a completely safe manner. All IOExceptions and other Exceptions are
   * translated into the three well-known exceptions on the signature.
   *
   * @param uri      The URI to read.
   * @param username (Optional) The username to use if the URI is HTTP and uses HTTP-Basic-Auth.
   * @param password (Optional) The password to use if the URI is HTTP and uses HTTP-Basic-Auth.
   * @return The content from the URI.
   * @throws DoesNotExistException If the URI doesn't exist.
   * @throws TemporaryIOException  If there is a network error that might correct itself.
   * @throws PermanentIOException  If there is a network error or other error that won't correct itself.
   */
  public static String downloadToString(final URI uri, final String username, final String password)
    throws DoesNotExistException, TemporaryIOException, PermanentIOException {
    return IOTools.protectIO(new Callable<String>() {
      @Override
      public String call() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        download(uri, username, password, baos, null);
        return baos.toString("UTF-8");
      }
    });
  }

  public static void download(URI uri, String username, String password, OutputStream os, MD5 md5) throws IOException {
    URLConnection uc = uri.toURL().openConnection();
    if (uc instanceof HttpURLConnection && username != null) {
      String credentials = username + ":" + password;
      BASE64Encoder encoder = new BASE64Encoder();
      uc.setRequestProperty("Authorization", "Basic " + encoder.encode(credentials.getBytes()));
    }

    if (uc instanceof HttpsURLConnection) {
      HttpsURLConnection huc = (HttpsURLConnection) uc;
      huc.setHostnameVerifier(new HostnameVerifier() {
        public boolean verify(String s, SSLSession sslSession) {
          return true;
        }
      });
    }

    uc.connect();

    if (uc instanceof HttpURLConnection) {
      HttpURLConnection huc = (HttpURLConnection) uc;
      int result = huc.getResponseCode();
      if (result >= 100 && result <= 199) {
        throw new TemporaryIOException("HTTP server returned 1xx resposne");
      } else if (result == 302 || result == 307) {
        throw new TemporaryIOException("HTTP sent redirect and the current HTTP client cannot redirect");
      } else if (result >= 300 && result <= 399) {
        throw new PermanentIOException("HTTP sent redirect and the current HTTP client cannot redirect");
      } else if (result == 404 || result == 410) {
        throw new DoesNotExistException("HTTP resource doesn't exist");
      } else if (result == 401) {
        throw new DoesNotExistException("HTTP server requires authentication and nothing was set");
      } else if (result >= 400 && result <= 599) {
        throw new TemporaryIOException("HTTP sent [" + result + "] failure and not a 404 or 500");
      }
    }

    InputStream is = uc.getInputStream();
    IOTools.write(is, os, md5);

    os.close();
    is.close();
  }
}
