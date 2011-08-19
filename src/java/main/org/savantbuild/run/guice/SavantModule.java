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
package org.savantbuild.run.guice;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.savantbuild.dep.ant.ArtifactCopyTask;
import org.savantbuild.dep.ant.DependencyPathTask;
import org.savantbuild.dep.version.ArtifactVersionTools;
import org.savantbuild.domain.Context;
import org.savantbuild.util.GroovyTools;

import com.google.inject.AbstractModule;

/**
 * <p>
 * This is the main Guice module for savant.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class SavantModule extends AbstractModule {
  private final Context context;

  public SavantModule(Context context) {
    this.context = context;
  }

  @Override
  protected void configure() {
    bind(CommandLineParser.class).to(PosixParser.class);

    // This binding is only for the ant integration and other places that Savant doesn't invoke directly
    bind(Context.class).toInstance(context);

    requestStaticInjection(ArtifactVersionTools.class);
    requestStaticInjection(DependencyPathTask.class);
    requestStaticInjection(ArtifactCopyTask.class);
    requestStaticInjection(GroovyTools.class);
  }
}
