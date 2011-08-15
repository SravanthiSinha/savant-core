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

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.HttpRetryException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.savantbuild.util.StringTools;

/**
 * <p>
 * This class provides some common IO tools.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class IOTools {
  /**
   * Runs the callable in a protected state. If the IO operation failed because a resource doesn't
   * exist, null is returned. If a network failure occurred, then a TemporaryIOException is thrown.
   * Otherwise, a PermanentIOException is throw to indicate a permanant failure that will always occur
   * if this method is called multiple times with the same input (for example the URL is malformed).
   *
   * @param callable The callable to execute.
   * @return The result of the Callable.
   * @throws PermanentIOException  If the IO caused an exception and it was not recoverable. This means
   *                               that calling this method with the same Callable will always cause the error and it will
   *                               never succeed. This currently is only thrown for MalformedURLException and all other
   *                               unkown exceptions (like NPEs and such).
   * @throws TemporaryIOException  If the IO caused an exception and it was only temporary due to
   *                               environmental conditions or other causes that will eventually be remedied. Most of the
   *                               IOException sub-classes are temporary, such as SocketException and the like.
   * @throws DoesNotExistException If the IO is doing file IO and the file does not exist.
   */
  public static <T> T protectIO(Callable<T> callable)
    throws PermanentIOException, TemporaryIOException, DoesNotExistException {
    try {
      return callable.call();
    } catch (MalformedURLException mue) {
      // The URL is permanatly jacked
      throw new PermanentIOException(mue);
    } catch (ConnectException ce) {
      // A temporary state where the connection to the remote host failed. The server might be down
      throw new TemporaryIOException(ce);
    } catch (NoRouteToHostException nrthe) {
      // A temporary state where the connection to the remote host failed. The network might be down
      throw new TemporaryIOException(nrthe);
    } catch (PortUnreachableException pue) {
      // A temporary state where the connection to the remote host failed. The network might be down
      throw new TemporaryIOException(pue);
    } catch (SocketException se) {
      // A temporary state where the connection to the remote host failed or a read or write failed.
      // The network might be down or a router might have failed
      throw new TemporaryIOException(se);
    } catch (SocketTimeoutException ste) {
      // A temporary state where a connect, read or write timed out. The network might be down
      throw new TemporaryIOException(ste);
    } catch (UnknownHostException uhe) {
      // A temporary state where the DNS lookup failed. The DNS servers could be down or the network might be down
      throw new TemporaryIOException(uhe);
    } catch (FileNotFoundException fnfe) {
      // The URL is a file and it doesn't exist.
      throw new DoesNotExistException(fnfe);
    } catch (EOFException eofe) {
      // A temporary state a Streaming read or write failed.
      throw new TemporaryIOException(eofe);
    } catch (HttpRetryException hre) {
      // A temporary state an HTTP connection or operation required a retry with the server and the HTTP client
      // in the JDK could not accomplish it.
      throw new TemporaryIOException(hre);
    } catch (InterruptedIOException iioe) {
      // A temporary state an HTTP connection or operation failed in the middle.
      throw new TemporaryIOException(iioe);
    } catch (MD5Exception md5e) {
      // This is permanent because the MD5 was found and was invalid.
      throw new PermanentIOException(md5e);
    } catch (ProtocolException pe) {
      // A temporary state where the TCP layer failed.
      throw new TemporaryIOException(pe);
    } catch (UnknownServiceException use) {
      // A temporary state where the HTTP server returned a corrupt MIME type.
      throw new TemporaryIOException(use);
    } catch (StreamCorruptedException sce) {
      // A temporary state where the TCP stream was corrupt. Possibly an error in the TCP stack or checksum.
      throw new TemporaryIOException(sce);
    } catch (SSLHandshakeException she) {
      // A temporary state because the server is mis-configured for SSL.
      throw new TemporaryIOException(she);
    } catch (SSLKeyException ske) {
      // A temporary state because the server or client had a bad key cached or stored.
      throw new TemporaryIOException(ske);
    } catch (SSLPeerUnverifiedException spue) {
      // A temporary state because the server might have the SSL port turned off and will turn it back on in the
      // future.
      throw new TemporaryIOException(spue);
    } catch (SSLProtocolException spe) {
      // A temporary state because someone didn't implement the SSL specification correctly.
      throw new TemporaryIOException(spe);
    } catch (IOException ioe) {
      // A temporary state where the connection to the remote host failed. The network might be down
      throw new TemporaryIOException(ioe);
    } catch (PermanentIOException pioe) {
      // Rethrow.
      throw pioe;
    } catch (TemporaryIOException tioe) {
      // Rethrow.
      throw tioe;
    } catch (DoesNotExistException dnee) {
      // Rethrow.
      throw dnee;
    } catch (Exception e) {
      // This is really unlikely and therefore I'm assuming this is a bug in Savant or JDK or something. Therefore
      // it will probably not resolve it self anytime soon and I'm going to make it permanent.
      throw new PermanentIOException(e);
    }
  }

  /**
   * Reads from the given input stream and writes the contents out to the given OutputStream. During the write, the
   * MD5 sum from input stream is calculated and compared with the given MD5 sum. This does not close the InputStream
   * but <b>DOES</b> close the OutputStream so that the data gets flushed out correctly.
   *
   * @param is  The InputStream to read from. This InputStream is wrapped in a BufferedInputStream
   *            for performance.
   * @param os  The OutputStream to write to.
   * @param md5 (Optional) The MD5 sum to check against.
   * @return The MD5 checksum of the file that was written out. This helps in case the caller needs
   *         the sum and the parameter is not given.
   * @throws IOException  If the output operation fails.
   * @throws MD5Exception If the MD5 check failed.
   */
  public static MD5 write(InputStream is, OutputStream os, MD5 md5) throws IOException {
    MessageDigest digest;
    // Copy to the file can do the MD5 sum while copying
    try {
      digest = MessageDigest.getInstance("MD5");
      digest.reset();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to locate MD5 algorithm");
    }

    DigestInputStream inputStream = new DigestInputStream(new BufferedInputStream(is), digest);
    inputStream.on(true);

    // First ensure that directory is present
    BufferedOutputStream bof = new BufferedOutputStream(os);

    try {
      // Then output the file
      byte[] b = new byte[8192];
      int len;
      while ((len = inputStream.read(b)) != -1) {
        bof.write(b, 0, len);
      }
    } finally {
      bof.close();
    }

    if (md5 != null && md5.bytes != null) {
      byte[] localMD5 = digest.digest();
      if (localMD5 != null && !Arrays.equals(localMD5, md5.bytes)) {
        throw new MD5Exception("MD5 mismatch when writing from the InputStream to the OutputStream.");
      }
    }

    byte[] bytes = inputStream.getMessageDigest().digest();
    return new MD5(StringTools.toHex(bytes), bytes, null);
  }

  /**
   * Parses an MD5 from the given InputStream.
   *
   * @param file The file to parse.
   * @return The MD5.
   */
  public static MD5 parseMD5(final File file) {
    if (file == null || !file.isFile()) {
      return null;
    }

    return protectIO(new Callable<MD5>() {
      @Override
      public MD5 call() throws Exception {
        InputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");

        StringBuffer buf = new StringBuffer();
        char[] c = new char[1024];
        int count;
        while ((count = isr.read(c, 0, 1024)) != -1) {
          for (int i = 0; i < count; i++) {
            buf.append(c[i]);
          }
        }

        isr.close();
        is.close();

        String str = buf.toString().trim();
        String name = null;
        String sum = null;
        if (str.length() > 0) {
          // Validate format (should be either only the md5sum or the sum plus the file name)
          if (str.length() < 32) {
            throw new MD5Exception("Invalid md5sum [" + str + "]");
          }

          if (str.length() == 32) {
            sum = str;
          } else if (str.length() > 33) {
            int index = str.indexOf(" ");
            if (index == 32) {
              sum = str.substring(0, 32);

              // Find file name and verify
              while (str.charAt(index) == ' ') {
                index++;
              }

              if (index == str.length()) {
                throw new MD5Exception("Invalid md5sum [" + str + "]");
              }

              name = str.substring(index);
            } else {
              throw new MD5Exception("Invalid md5sum [" + str + "]");
            }
          } else {
            throw new MD5Exception("Invalid md5sum [" + str + "]");
          }
        }

        return new MD5(sum, StringTools.fromHex(sum), name);
      }
    });
  }
}
