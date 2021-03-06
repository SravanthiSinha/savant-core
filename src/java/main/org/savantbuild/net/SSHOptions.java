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

/**
 * <p>
 * This class stores all the options.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class SSHOptions {
  public String server;
  public int port = 22;
  public String knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
  public boolean trustUnknownHosts = false;
  public String username = System.getProperty("user.name");
  public String password;
  public String identity = System.getProperty("user.home") + "/.ssh/id_dsa";
  public String passphrase = "";
  public String cipher;
}
