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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for SingularityBuilder.
 */
public final class SingularityBuilderTest {
    private SingularityBuilder builder;

    @Before
    public void setUp() {
        builder = new SingularityBuilder();
    }

    @Test
    public void testCtr() {
        assertNotNull(builder);
    }

    @Test
    public void testBuild() {
        builder
            .setExecutable("foo")
            .setTimeout(1000L)
            .setFlankSize(100)
            .addEnvironment("VARIABLE", "value")
            .addFile("file")
            .addArgument("--help")
            .setSudo(true)
            .setImage("image")
            .addMount("/source", "/target");

        assertEquals("foo", builder.getExecutable());
        assertEquals(Long.valueOf(1000L), builder.getTimeout());
        assertEquals(Long.valueOf(1000L), builder.getOptTimeout().get());
        assertEquals(Integer.valueOf(100), builder.getFlankSize());
        assertEquals(Integer.valueOf(100), builder.getOptFlankSize().get());
        assertEquals(1, builder.getEnvironment().size());
        assertEquals("value", builder.getEnvironment().get("VARIABLE"));
        assertEquals(1, builder.getFiles().size());
        assertEquals("file", builder.getFiles().get(0));
        assertEquals(1, builder.getArguments().size());
        assertEquals("--help", builder.getArguments().get(0));
        assertTrue(builder.getSudo());
        assertEquals("image", builder.getImage());
        assertEquals(1, builder.getMounts().size());
        assertEquals("/target", builder.getMounts().get("/source"));

        List<String> command = builder.build();

        assertEquals(9, command.size());
        assertEquals("SINGULARITYENV_VARIABLE=value", command.get(0));
        assertEquals("sudo", command.get(1));
        assertEquals("singularity", command.get(2));
        assertEquals("exec", command.get(3));
        assertEquals("--bind", command.get(4));
        assertEquals("/source:/target", command.get(5));
        assertEquals("docker://image", command.get(6));
        assertEquals("foo", command.get(7));
        assertEquals("--help", command.get(8));
    }

    @Test
    public void testImage() {
        assertTrue(new SingularityBuilder("foo", "/image").build().contains("/image"));
    }

    @Test
    public void testShubImage() {
        assertTrue(new SingularityBuilder("foo", "shub://image").build().contains("shub://image"));
    }

    @Test
    public void testDockerImage() {
        assertTrue(new SingularityBuilder("foo", "image").build().contains("docker://image"));
    }

    @Test
    public void testBuildMount() {
        builder
            .setExecutable("foo")
            .setImage("image")
            .addMount("/mount");

        assertTrue(builder.build().contains("/mount"));
    }
}
