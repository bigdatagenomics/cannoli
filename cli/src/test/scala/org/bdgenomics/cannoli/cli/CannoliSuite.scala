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
package org.bdgenomics.cannoli.cli

import java.io.ByteArrayOutputStream
import com.google.inject.{ AbstractModule, Guice }
import net.codingwell.scalaguice.ScalaModule
import net.codingwell.scalaguice.InjectorExtensions._
import org.bdgenomics.adam.cli.CommandGroup
import org.bdgenomics.cannoli.cli.Cannoli.defaultCommandGroups
import org.scalatest.FunSuite

class CannoliSuite extends FunSuite {

  test("default command groups is non empty") {
    assert(defaultCommandGroups.nonEmpty)
  }

  test("module provides default command groups") {
    val injector = Guice.createInjector(new CannoliModule())
    val commandGroups = injector.instance[List[CommandGroup]]
    assert(commandGroups == defaultCommandGroups)
  }

  test("inject default command groups when called via main") {
    val stream = new ByteArrayOutputStream()
    Console.withOut(stream) {
      Cannoli.main(Array())
    }
    val out = stream.toString()
    // the help text has been displayed
    assert(out.contains("Usage"))
    // ...and freebayes (from default groups) is one of the commands listed
    assert(out.contains("freebayes"))
  }

  test("command groups is empty when called via apply") {
    val stream = new ByteArrayOutputStream()
    Console.withOut(stream) {
      new Cannoli(List.empty)(Array())
    }
    val out = stream.toString()
    assert(out.contains("Usage"))
    assert(!out.contains("freebayes"))
  }

  test("single command group") {
    val stream = new ByteArrayOutputStream()
    Console.withOut(stream) {
      new Cannoli(List(CommandGroup("SINGLE COMMAND GROUP", List(Freebayes)))).apply(Array())
    }
    val out = stream.toString()
    assert(out.contains("Usage"))
    assert(out.contains("SINGLE"))
    assert(out.contains("freebayes"))
  }

  test("add new command group to default command groups") {
    val stream = new ByteArrayOutputStream()
    Console.withOut(stream) {
      val commandGroups = defaultCommandGroups.union(List(CommandGroup("NEW COMMAND GROUP", List(Freebayes))))
      new Cannoli(commandGroups)(Array())
    }
    val out = stream.toString()
    assert(out.contains("Usage"))
    assert(out.contains("NEW"))
  }

  test("module restores default command groups when called via apply") {
    val stream = new ByteArrayOutputStream()
    Console.withOut(stream) {
      val injector = Guice.createInjector(new CannoliModule())
      val commandGroups = injector.instance[List[CommandGroup]]
      new Cannoli(commandGroups).apply(Array())
    }
    val out = stream.toString()
    assert(out.contains("Usage"))
    assert(out.contains("freebayes"))
  }

  test("custom module with single command group") {
    val stream = new ByteArrayOutputStream()
    Console.withOut(stream) {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          bind[List[CommandGroup]].toInstance(List(CommandGroup("SINGLE COMMAND GROUP", List(Freebayes))))
        }
      }
      val injector = Guice.createInjector(module)
      val commandGroups = injector.instance[List[CommandGroup]]
      new Cannoli(commandGroups).apply(Array())
    }
    val out = stream.toString()
    assert(out.contains("Usage"))
    assert(out.contains("SINGLE"))
    assert(out.contains("freebayes"))
  }

  test("custom module with new command group added to default command groups") {
    val stream = new ByteArrayOutputStream()
    Console.withOut(stream) {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          bind[List[CommandGroup]].toInstance(defaultCommandGroups.union(List(CommandGroup("NEW COMMAND GROUP", List(Freebayes)))))
        }
      }
      val injector = Guice.createInjector(module)
      val commandGroups = injector.instance[List[CommandGroup]]
      new Cannoli(commandGroups).apply(Array())
    }
    val out = stream.toString()
    assert(out.contains("Usage"))
    assert(out.contains("NEW"))
  }
}
