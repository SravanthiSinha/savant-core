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

import org.savantbuild.domain.Project;
import org.savantbuild.domain.Target;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines how help is printed out to the user.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultHelpOutputer.class)
public interface HelpOutputer {
  /**
   * Prints the entire project's help message.
   *
   * @param project The project.
   */
  void print(Project project);

  /**
   * Prints the help message for just the given target.
   *
   * @param target The target.
   */
  void print(Target target);
}
