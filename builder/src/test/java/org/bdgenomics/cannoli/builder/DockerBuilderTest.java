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
 * Unit test for DockerBuilder.
 */
public final class DockerBuilderTest {
    private DockerBuilder builder;

    @Before
    public void setUp() {
        builder = new DockerBuilder();
    }

    @Test
    public void testCtr() {
        assertNotNull(builder);
    }

    @Test(expected=NullPointerException.class)
    public void setImageNull() {
        builder.setImage(null);
    }

    @Test(expected=NullPointerException.class)
    public void addMountNull() {
        builder.addMount(null);
    }

    @Test(expected=NullPointerException.class)
    public void addMountNullSource() {
        builder.addMount(null, "/target");
    }

    @Test(expected=NullPointerException.class)
    public void addMountNullTarget() {
        builder.addMount("/source", null);
    }

    @Test(expected=NullPointerException.class)
    public void addMountsNull() {
        builder.addMounts(null);
    }

    @Test
    public void testResetEmpty() {
        builder.reset();
        assertNull(builder.getExecutable());
        assertNull(builder.getTimeout());
        assertNull(builder.getFlankSize());
        assertTrue(builder.getEnvironment().isEmpty());
        assertTrue(builder.getFiles().isEmpty());
        assertTrue(builder.getArguments().isEmpty());
        assertFalse(builder.getSudo());
        assertNull(builder.getImage());
        assertTrue(builder.getMounts().isEmpty());
    }

    @Test
    public void testResetFull() {
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

        builder.reset();
        assertNull(builder.getExecutable());
        assertNull(builder.getTimeout());
        assertNull(builder.getFlankSize());
        assertTrue(builder.getEnvironment().isEmpty());
        assertTrue(builder.getFiles().isEmpty());
        assertTrue(builder.getArguments().isEmpty());
        assertFalse(builder.getSudo());
        assertNull(builder.getImage());
        assertTrue(builder.getMounts().isEmpty());
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildNullExecutable() {
        builder.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildNullImage() {
        builder.setExecutable("foo").build();
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
        assertEquals(12, command.size());
        assertEquals("sudo", command.get(0));
        assertEquals("docker", command.get(1));
        assertEquals("run", command.get(2));
        assertEquals("-i", command.get(3));
        assertEquals("--env", command.get(4));
        assertEquals("VARIABLE=value", command.get(5));
        assertEquals("-v", command.get(6));
        assertEquals("/source:/target", command.get(7));
        assertEquals("--rm", command.get(8));
        assertEquals("image", command.get(9));
        assertEquals("foo", command.get(10));
        assertEquals("--help", command.get(11));
    }

    @Test
    public void testBuildMount() {
        builder
            .setExecutable("foo")
            .setImage("image")
            .addMount("/mount");

        assertTrue(builder.build().contains("/mount:/mount"));
    }
}
