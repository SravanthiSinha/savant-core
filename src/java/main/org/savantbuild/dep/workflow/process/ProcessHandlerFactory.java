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
package org.savantbuild.dep.workflow.process;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.savantbuild.BuildException;
import org.savantbuild.domain.Process;
import org.savantbuild.run.output.Output;

/**
 * <p>
 * This class is a simple factory that creates the process handlers.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ProcessHandlerFactory {
  public static ProcessHandler buildProcess(Output output, Process process) {
    String type = process.getAttributes().get("type");
    if (type.equals("url")) {
      return new URLProcessHandler(output, process.getAttributes());
    } else if (type.equals("svn")) {
      return new SVNProcessHandler(output, process.getAttributes());
    } else if (type.equals("scp")) {
      return new SCPProcessHandler(output, process.getAttributes());
    } else if (type.equals("cache")) {
      return new CacheProcess(output, process.getAttributes());
    }

    try {
      Class<?> klass = Class.forName(type);
      Constructor<?> constructor = klass.getConstructor(Map.class);
      return (ProcessHandler) constructor.newInstance(output, process.getAttributes());
    } catch (ClassNotFoundException e) {
      throw new BuildException("Invalid workflow process type [" + type + "]. Savant provides the types url, " +
        "svn, scp, or cache out-of-the-box. You can also specify a fully-qualified class name that implements" +
        "ProcessHandler.");
    } catch (NoSuchMethodException e) {
      throw new BuildException("Invalid workflow process type [" + type + "]. That class doesn't have a constructor " +
        "that takes (Output, Map<String, String>).");
    } catch (Exception e) {
      throw new BuildException("Error while invoking the constructor for the workflow process type [" + type + "].", e);
    }
  }
}
