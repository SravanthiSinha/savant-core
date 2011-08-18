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
package org.savantbuild.run.output;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>
 * This class adapts the output to a writer.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class OutputWriter extends Writer {
  private final Output output;
  private final Level level;

  public OutputWriter(Output output, Level level) {
    this.output = output;
    this.level = level;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    output.print(level, new String(cbuf, off, len));
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
  }
}
