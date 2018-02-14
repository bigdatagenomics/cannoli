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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Abstract container builder.
 */
abstract class ContainerBuilder extends CommandBuilder {

    /**
     * Create a new container builder.
     */
    protected ContainerBuilder() {
        super();
    }

    /**
     * Create a new container builder with the specified executable.
     *
     * @param executable executable, must not be null
     */
    protected ContainerBuilder(final String executable) {
        super(executable);
    }

    /**
     * Create a new container builder with the specified executable and image.
     *
     * @param executable executable, must not be null
     * @param image image, must not be null
     */
    protected ContainerBuilder(final String executable, final String image) {
        super(executable);
        setImage(image);
    }


    /**
     * Return the container commands for this container builder.
     *
     * @return the container commands for this container builder
     */
    protected abstract List<String> getContainerCommands();

    /**
     * Return the remove argument for this container builder.
     *
     * @return the remove argument for this container builder
     */
    protected abstract List<String> getRemoveArgument();

    /**
     * Format the specified environment variable into a list of strings.
     *
     * @param variable variable
     * @param value value
     * @return the specified environment variable formatted into a list of strings.
     */
    protected abstract List<String> formatEnvironmentVariable(String variable, String value);

    /**
     * Format the specified environment variable into a list of string arguments.
     *
     * @param variable variable
     * @param value value
     * @return the specified environment variable formatted into a list of string arguments.
     */
    protected abstract List<String> formatEnvironmentArgument(String variable, String value);

    /**
     * Format the specified environment variable into a list of string arguments.
     *
     * @param image image
     * @return the specified environment variable formatted into a list of string arguments.
     */
    protected abstract List<String> formatImage(String image);

    /**
     * Format the specified mount point into a list of string arguments.
     *
     * @param source source
     * @param target target
     * @return the specified mount point formatted into a list of string arguments
     */
    protected abstract List<String> formatMount(String source, String target);

    @Override
    public final List<String> build() {
        if (getExecutable() == null) {
            throw new IllegalStateException("executable must not be null");
        }
        if (getImage() == null) {
            throw new IllegalStateException("image must not be null");
        }

        List<String> command = new ArrayList<String>();

        // add environment variables
        for (Map.Entry<String, String> e : getEnvironment().entrySet()) {
            command.addAll(formatEnvironmentVariable(e.getKey(), e.getValue()));
        }

        // add sudo if necessary
        if (isSudo()) {
            command.add("sudo");
        }

        // e.g. docker run, etc.
        command.addAll(getContainerCommands());

        // add environment arguments
        for (Map.Entry<String, String> e : getEnvironment().entrySet()) {
            command.addAll(formatEnvironmentArgument(e.getKey(), e.getValue()));
        }

        // add mount arguments
        for (Map.Entry<String, String> e : getMounts().entrySet()) {
            command.addAll(formatMount(e.getKey(), e.getValue()));
        }

        // e.g. --rm
        command.addAll(getRemoveArgument());

        // container image name
        command.addAll(formatImage(getImage()));

        // add command
        command.add(getExecutable());

        // add command arguments
        command.addAll(getArguments());

        return command;
    }
}
