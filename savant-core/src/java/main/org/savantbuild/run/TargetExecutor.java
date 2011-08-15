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
package org.savantbuild.run;

import java.util.List;
import java.util.Map;

import org.savantbuild.domain.Project;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines how targets are executed by Savant. This handles target dependencies,
 * arguments, etc.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultTargetExecutor.class)
public interface TargetExecutor {
  /**
   * Runs the target with the given name without any arguments.
   *
   * @param project The project to get the target from.
   * @param name    The name of the target.
   */
  void run(Project project, String name);

  /**
   * Runs the target with the given name. The arguments given are parsed using the configuration inside the target.
   *
   * @param project   The project to get the target from.
   * @param name      The name of the target.
   * @param arguments The arguments (from the command line) to the target.
   */
  void run(Project project, String name, List<String> arguments);

  /**
   * Runs the target with the given name and the given arguments.
   *
   * @param project   The project to get the target from.
   * @param name      The name of the target.
   * @param arguments The arguments (already parsed).
   */
  void run(Project project, String name, Map<String, String> arguments);
}
