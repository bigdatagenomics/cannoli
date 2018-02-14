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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for LocalBuilder.
 */
public final class LocalBuilderTest {
    private LocalBuilder builder;

    @Before
    public void setUp() {
        builder = new LocalBuilder();
    }

    @Test
    public void testCtr() {
        assertNotNull(builder);
    }

    @Test(expected=NullPointerException.class)
    public void testAddNullVarargArguments() {
        builder.add((String[]) null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddNullIterableArguments() {
        builder.add((Iterable<String>) null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddArgumentNull() {
        builder.addArgument(null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddArgumentsNullVarargArguments() {
        builder.addArguments((String[]) null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddArgumentsNullIterableArguments() {
        builder.addArguments((Iterable<String>) null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddEnvironmentNullVariable() {
        builder.addEnvironment(null, "value");
    }

    @Test(expected=NullPointerException.class)
    public void testAddEnvironmentNullValue() {
        builder.addEnvironment("VARIABLE", null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddEnvironmentNullEnvironment() {
        builder.addEnvironment((Map<String, String>) null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddFileNull() {
        builder.addFile(null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddFilesNullVarargFiles() {
        builder.addFiles((String[]) null);
    }

    @Test(expected=NullPointerException.class)
    public void testAddFilesNullIterableFiles() {
        builder.addFiles((Iterable<String>) null);
    }

    @Test(expected=NullPointerException.class)
    public void testSetExecutableNull() {
        builder.setExecutable(null);
    }

    @Test(expected=NullPointerException.class)
    public void testSetTimeoutNullTimeUnit() {
        builder.setTimeout(1000L, null);
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
    }

    @Test
    public void testResetFull() {
        builder
            .setExecutable("foo")
            .setTimeout(1000L)
            .setFlankSize(100)
            .addEnvironment("VARIABLE", "value")
            .addFile("file")
            .addArgument("--help");

        builder.reset();
        assertNull(builder.getExecutable());
        assertNull(builder.getTimeout());
        assertNull(builder.getFlankSize());
        assertTrue(builder.getEnvironment().isEmpty());
        assertTrue(builder.getFiles().isEmpty());
        assertTrue(builder.getArguments().isEmpty());
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildNullExecutable() {
        builder.build();
    }

    @Test
    public void testBuild() {
        builder
            .setExecutable("foo")
            .setTimeout(1000L)
            .setFlankSize(100)
            .addEnvironment("VARIABLE", "value")
            .addFile("file")
            .addArgument("--help");

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

        List<String> command = builder.build();
        assertEquals(2, command.size());
        assertEquals("foo", command.get(0));
        assertEquals("--help", command.get(1));
    }
}
