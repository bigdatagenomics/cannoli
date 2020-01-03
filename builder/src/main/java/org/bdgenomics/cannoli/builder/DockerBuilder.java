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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Docker container builder.
 */
public final class DockerBuilder extends ContainerBuilder {

    /**
     * Create a new Docker command builder.
     */
    public DockerBuilder() {
        super();
    }

    /**
     * Create a new Docker command builder with the specified executable.
     *
     * @param executable executable, must not be null
     */
    public DockerBuilder(final String executable) {
        super(executable);
    }

    /**
     * Create a new Docker command builder with the specified executable and image.
     *
     * @param executable executable, must not be null
     * @param image image, must not be null
     */
    public DockerBuilder(final String executable, final String image) {
        super(executable, image);
    }


    @Override
    protected List<String> getContainerCommands() {
        return ImmutableList.of("docker", "run", "-i");
    }

    @Override
    protected List<String> getRemoveArgument() {
        return ImmutableList.of("--rm");
    }

    @Override
    protected List<String> formatImage(final String image) {
        return ImmutableList.of(image);
    }

    @Override
    protected List<String> formatEnvironmentVariable(final String variable, final String value) {
        return Collections.emptyList();
    }

    @Override
    protected List<String> formatEnvironmentArgument(final String variable, final String value) {
        return ImmutableList.of("--env", variable + "=" + value);
    }

    @Override
    protected List<String> formatMount(final String source, final String target) {
        return ImmutableList.of("-v", source + ":" + target);
    }
}
