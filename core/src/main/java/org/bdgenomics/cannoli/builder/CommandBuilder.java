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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Abstract command builder.
 */
public abstract class CommandBuilder implements Serializable {
    /** Command to run. */
    private String executable;

    /** Name of container image. */
    private String image;

    /** Number of bases to flank each command invocation by. */
    private Integer flankSize;

    /** True to run via sudo. */
    private boolean sudo = false;

    /** How long to let a single partition run for, in seconds. */
    private Long timeout;

    /** List of command arguments. */
    private final List<String> arguments = new ArrayList<String>();

    /** Map of environment variables. */
    private final Map<String, String> environment = new HashMap<String, String>();

    /** List of files to make available locally. */
    private final List<String> files = new ArrayList<String>();

    /** Map of mount points. */
    private Map<String, String> mounts = new HashMap<String, String>();


    /**
     * Create a new command builder.
     */
    protected CommandBuilder() {
        // empty
    }

    /**
     * Create a new command builder with the specified executable.
     *
     * @param executable executable, must not be null
     */
    protected CommandBuilder(final String executable) {
        this();
        setExecutable(executable);
    }


    /**
     * Set the executable for this command builder.
     *
     * @param executable executable, must not be null
     * @return this command builder
     */
    public final CommandBuilder setExecutable(final String executable) {
        checkNotNull(executable);
        this.executable = executable;
        return this;
    }

    /**
     * Set the number of bases to flank each command invocation by for this builder.
     *
     * @param flankSize number of bases to flank each command invocation by
     * @return this command builder
     */
    public final CommandBuilder setFlankSize(@Nullable final Integer flankSize) {
        this.flankSize = flankSize;
        return this;
    }

    /**
     * Set the image for this command builder.
     *
     * @param image image, must not be null
     * @return this command builder
     */
    public final CommandBuilder setImage(final String image) {
        checkNotNull(image);
        this.image = image;
        return this;
    }

    /**
     * Set to true to run via sudo for this command builder.
     *
     * @param sudo true to run via sudo
     * @return this command builder
     */
    public final CommandBuilder setSudo(final boolean sudo) {
        this.sudo = sudo;
        return this;
    }

    /**
     * Set how long to let a single partition run for, in seconds, for this builder.
     *
     * @param timeout how long to let a single partition run for, in seconds
     * @return this command builder
     */
    public final CommandBuilder setTimeout(@Nullable final Long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Set how long to let a single partition run for, in the specified time unit, for this builder.
     *
     * @param duration duration
     * @param timeUnit time unit, must not be null
     * @return this command builder
     */
    public final CommandBuilder setTimeout(final long duration, final TimeUnit timeUnit) {
        checkNotNull(timeUnit);
        this.timeout = timeUnit.toSeconds(duration);
        return this;
    }


    /**
     * Add one or more arguments to the list of command arguments for this command builder.
     *
     * @param arguments variable number of arguments to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder add(final String... arguments) {
        return addArguments(arguments);
    }

    /**
     * Add one or more arguments to the list of command arguments for this command builder.
     *
     * @param arguments arguments to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder add(final Iterable<String> arguments) {
        return addArguments(arguments);
    }

    /**
     * Add an argument to the list of command arguments for this command builder.
     *
     * @param argument argument to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addArgument(final String argument) {
        checkNotNull(argument);
        arguments.add(argument);
        return this;
    }

    /**
     * Add one or more arguments to the list of command arguments for this command builder.
     *
     * @param arguments variable number of arguments to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addArguments(final String... arguments) {
        checkNotNull(arguments);
        for (String argument : arguments) {
            this.arguments.add(argument);
        }
        return this;
    }

    /**
     * Add one or more arguments to the list of command arguments for this command builder.
     *
     * @param arguments arguments to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addArguments(final Iterable<String> arguments) {
        checkNotNull(arguments);
        for (String argument : arguments) {
            this.arguments.add(argument);
        }
        return this;
    }

    /**
     * Add an environment variable to the map of environment variables for this command builder.
     *
     * @param variable environment variable to add, must not be null
     * @param value environment variable value to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addEnvironment(final String variable, final String value) {
        checkNotNull(variable);
        checkNotNull(value);
        environment.put(variable, value);
        return this;
    }

    /**
     * Add environment variables to the map of environment variables for this command builder.
     *
     * @param environment environment variables to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addEnvironment(final Map<String, String> environment) {
        checkNotNull(environment);
        this.environment.putAll(environment);
        return this;
    }

    /**
     * Add a file to the list of files to make available locally for this command builder.
     *
     * @param file file to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addFile(final String file) {
        checkNotNull(file);
        files.add(file);
        return this;
    }

    /**
     * Add zero or more files to the list of files to make available locally for this command builder.
     *
     * @param files variable number of files to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addFiles(final String... files) {
        checkNotNull(files);
        for (String file : files) {
            this.files.add(file);
        }
        return this;
    }

    /**
     * Add files to the list of files to make available locally for this command builder.
     *
     * @param files files to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addFiles(final Iterable<String> files) {
        checkNotNull(files);
        for (String file : files) {
            this.files.add(file);
        }
        return this;
    }

    /**
     * Add the specified mount point to the map of mount points for this command builder.
     *
     * @param mount mount point source and target, must not be null
     * @return this command builder
     */
    public final CommandBuilder addMount(final String mount) {
        return addMount(mount, mount);
    }

    /**
     * Add the specified mount point to the map of mount points for this command builder.
     *
     * @param source mount point source, must not be null
     * @param target mount point target, must not be null
     * @return this command builder
     */
    public final CommandBuilder addMount(final String source, final String target) {
        checkNotNull(source);
        checkNotNull(target);
        mounts.put(source, target);
        return this;
    }

    /**
     * Add the specified mount points to the map of mount points for this command builder.
     *
     * @param mounts mount points to add, must not be null
     * @return this command builder
     */
    public final CommandBuilder addMounts(final Map<String, String> mounts) {
        checkNotNull(mounts);
        this.mounts.putAll(mounts);
        return this;
    }


    /**
     * Return the executable for this command builder.
     *
     * @return the executable for this command builder
     */
    public final String getExecutable() {
        return executable;
    }

    /**
     * Return the number of bases to flank each command invocation by for this builder.  May be null.
     *
     * @return the number of bases to flank each command invocation by for this builder
     */
    public final Integer getFlankSize() {
        return flankSize;
    }

    /**
     * Return the number of bases to flank each command invocation by for this builder, as an optional.
     *
     * @return the number of bases to flank each command invocation by for this builder, as an optional
     */
    public final Optional<Integer> getOptFlankSize() {
        return Optional.ofNullable(flankSize);
    }

    /**
     * Return the image for this command builder.
     *
     * @return the image for this command builder.
     */
    public final String getImage() {
        return image;
    }

    /**
     * Return how long to let a single partition run for, in seconds, for this builder.  May be null.
     *
     * @return how long to let a single partition run for, in seconds, for this builder
     */
    public final Long getTimeout() {
        return timeout;
    }

    /**
     * Return how long to let a single partition run for, in seconds, for this builder, as an optional.
     *
     * @return how long to let a single partition run for, in seconds, for this builder, as an optional
     */
    public final Optional<Long> getOptTimeout() {
        return Optional.ofNullable(timeout);
    }

    /**
     * Return true to run via sudo for this command builder.
     *
     * @return true to run via sudo for this command builder
     */
    public final boolean getSudo() {
        return isSudo();
    }

    /**
     * Return true to run via sudo for this command builder.
     *
     * @return true to run via sudo for this command builder
     */
    public final boolean isSudo() {
        return sudo;
    }

    /**
     * Return an immutable list of command arguments for this command builder.
     *
     * @return an immutable list of command arguments for this command builder
     */
    public final List<String> getArguments() {
        return ImmutableList.copyOf(arguments);
    }

    /**
     * Return an immutable map of environment variables for this command builder.
     *
     * @return an immutable map of environment variables for this command builder
     */
    public final Map<String, String> getEnvironment() {
        return ImmutableMap.copyOf(environment);
    }

    /**
     * Return an immutable list of files to make available locally for this command builder.
     *
     * @return an immutable list of files to make available locally for this command builder
     */
    public final List<String> getFiles() {
        return ImmutableList.copyOf(files);
    }

    /**
     * Return the map of mount points for this command builder.
     *
     * @return the map of mount points for this command builder
     */
    public final Map<String, String> getMounts() {
        return ImmutableMap.copyOf(mounts);
    }

    /**
     * Reset this command builder.
     *
     * @return this command builder
     */
    public final CommandBuilder reset() {
        executable = null;
        flankSize = null;
        image = null;
        sudo = false;
        timeout = null;

        arguments.clear();
        environment.clear();
        files.clear();
        mounts.clear();

        return this;
    }

    /**
     * Build and return the command for this command builder as a list of strings.
     *
     * @return the command for this command builder as a list of strings.
     * @throws IllegalStateException if this builder is in an illegal state, e.g.
     *    if required values are not set
     */
    public abstract List<String> build();
}
