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
package org.savantbuild.config;

import org.savantbuild.domain.Project;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines the method for constructing synthetic target handling of Savant.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultSyntheticTargetFactory.class)
public interface SyntheticTargetFactory {
  /**
   * Builds the synthetic target with the given name for the given project.
   *
   * @param name    The name of the target.
   * @param project The project.
   */
  void build(String name, Project project);
}
