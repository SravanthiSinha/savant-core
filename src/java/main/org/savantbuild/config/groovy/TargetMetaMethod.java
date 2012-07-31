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
package org.savantbuild.config.groovy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.savantbuild.BuildException;
import org.savantbuild.config.TargetProxy;
import org.savantbuild.domain.Plugin;
import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;
import org.savantbuild.domain.Targetable;
import org.savantbuild.run.TargetExecutor;

/**
 * This class is the Groovy MetaMethod that handles the creation of new targets within the Context as the build file is
 * executed (parsed).
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class TargetMetaMethod extends AbstractMetaMethod {
  private static final String MESSAGE = "Invalid target definition. The target must have a name and can optionally be " +
    "followed by a set of attributes (defined using Groovy's Map notation) like this:\n\n" +
    "    target(\"name\", dependsOn: \"otherTarget\") << {\n" +
    "      // body\n" +
    "    }\n\n" +
    "Also, you might have setup the target body incorrectly. You need to ensure that the << are directly after " +
    "the target definition before the body.";
  private final TargetExecutor targetExecutor;
  private final Project project;
  private final Targetable targetable;

  public TargetMetaMethod(TargetExecutor targetExecutor, Class theClass, Project project, Targetable targetable) {
    super(theClass, Target.class, "target");
    this.targetExecutor = targetExecutor;
    this.project = project;
    this.targetable = targetable;
  }

  @Override
  public Object invoke(Object o, Object[] arguments) {
    if (arguments.length != 1) {
      throw new BuildException("Invalid target definition.");
    }

    Object[] argArray = (Object[]) arguments[0];
    Map<String, Object> settings = new HashMap<String, Object>();
    String name;
    if (argArray.length == 1) {
      name = argArray[0].toString();
    } else if (argArray.length == 2) {
      name = argArray[1].toString();

      try {
        settings.putAll((Map<String, Object>) argArray[0]);
      } catch (ClassCastException e) {
        throw new BuildException(MESSAGE);
      }
    } else {
      throw new BuildException(MESSAGE);
    }

    Map<String, List<Object>> params = new HashMap<String, List<Object>>();
    try {
      Map<String, List<Object>> localParams = (Map<String, List<Object>>) settings.get("params");
      if (localParams != null) {
        for (List<Object> objects : localParams.values()) {
          if (objects.size() != 2 || !(objects.get(0) instanceof Boolean) || !(objects.get(1) instanceof String)) {
            throw new ClassCastException();
          }
        }

        params.putAll(localParams);
      }

      if (params.containsKey("help")) {
        throw new BuildException("The [help] parameter is a reserved parameter. You cannot define it for your target [" + name + "]");
      }

      params.put("help", Arrays.<Object>asList(false, "displays the help for this target only"));
    } catch (ClassCastException e) {
      throw new BuildException("Invalid target [" + name + "]. The params definition for the target was not defined " +
        "using a Map that contains Lists with a Boolean and a String. It should look like this:\n" +
        "\ttarget(\"" + name + "\", params: [param1: [true, \"description\"], param2: [false, \"descrption2\"]]) {...}");
    }

    Object description = settings.get("description");
    if (description == null) {
      description = "No description";
    }

    Object value = settings.get("dependsOn");
    List<String> dependencies = new ArrayList<String>();
    if (value != null) {
      if (value instanceof List) {
        dependencies.addAll((List) value);
      } else {
        dependencies.add(value.toString());
      }
    }

    int index = name.indexOf(":");
    Targetable local = targetable;
    String targetName = name;
    if (index > 0) {
      if (targetable instanceof Plugin) {
        throw new BuildException("The target [" + name + "] is referencing a plugin target. You can't reference " +
          "plugin targets from a plugin, only from a project's build script.");
      }

      Project project = (Project) targetable;
      String pluginName = name.substring(0, index);
      if (project.getPlugins().get(pluginName) == null) {
        throw new BuildException("The target [" + name + "] is referencing a plugin target and the plugin [" +
          pluginName + "] does not exist in the project.");
      }

      local = project.getPlugins().get(pluginName);
      targetName = name.substring(index + 1);
    }

    Target target = local.lookupTarget(targetName);
    if (target == null) {
      target = local.createTarget(targetName);
      target.setDescription(description.toString());
      target.getParams().putAll(params);
      target.getDependencies().addAll(dependencies);
    }

    return new TargetProxy(targetExecutor, project, target, name);
  }
}
