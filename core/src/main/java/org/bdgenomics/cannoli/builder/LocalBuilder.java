/**
 * Licensed to Big Data Genomics (BDG) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The BDG licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.cannoli.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Local command builder.
 */
public final class LocalBuilder extends CommandBuilder {

    /**
     * Create a new local command builder.
     */
    public LocalBuilder() {
        super();
    }

    /**
     * Create a new local command builder with the specified executable.
     *
     * @param executable executable, must not be null
     */
    public LocalBuilder(final String executable) {
        super(executable);
    }


    @Override
    public final List<String> build() {
        if (getExecutable() == null) {
            throw new IllegalStateException("executable must not be null");
        }
        List<String> command = new ArrayList<String>();

        // add command
        command.add(getExecutable());

        // add command arguments
        command.addAll(getArguments());

        return command;
    }
}
