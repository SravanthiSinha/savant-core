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
package org.savantbuild.config;

import org.savantbuild.domain.Context;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines how configuration is loaded from the Savant build and workflow
 * files.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultConfigurationService.class)
public interface ConfigurationService {
  /**
   * Loads all of the configuration for the given context including the local project (if any), the workflows for the
   * machine, plugins, etc.
   *
   * @param context The context to configure.
   */
  void configure(Context context);
}
