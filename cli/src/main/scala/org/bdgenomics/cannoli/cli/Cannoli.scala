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

import java.util.logging.Level._
import javax.inject.Inject
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import org.bdgenomics.utils.misc.Logging
import org.bdgenomics.adam.cli.CommandGroup
import org.bdgenomics.adam.util.ParquetLogger
import org.bdgenomics.utils.cli._

object Cannoli {

  val defaultCommandGroups = List(CommandGroup("CANNOLI", List(BcftoolsNorm,
    BedtoolsIntersect,
    Bowtie,
    Bowtie2,
    Bwa,
    Freebayes,
    Minimap2,
    SamtoolsMpileup,
    SnpEff,
    Vep,
    VtNormalize)),
    CommandGroup("CANNOLI TOOLS", List(InterleaveFastq,
      SampleReads)))

  def main(args: Array[String]) {
    new Cannoli(defaultCommandGroups)(args)
  }
}

private class InitArgs extends Args4jBase with ParquetArgs {}

/**
 * Cannoli main.
 *
 * @param commandGroups List of command groups, can be injected via Guice.
 */
class Cannoli @Inject() (commandGroups: List[CommandGroup]) extends Logging {

  private def printLogo() {
    print("\n")
    println("""                              _ _ 
      *                             | (_)
      *   ___ __ _ _ __  _ __   ___ | |_ 
      *  / __/ _` | '_ \| '_ \ / _ \| | |
      * | (_| (_| | | | | | | | (_) | | |
      *  \___\__,_|_| |_|_| |_|\___/|_|_|""".stripMargin('*'))
  }

  private def printVersion() {
    printLogo()
    val about = new About()
    println("\nCannoli version: %s".format(about.version))
    if (about.isSnapshot) {
      println("Commit: %s Build: %s".format(about.commit, about.buildTimestamp))
    }
    println("Built for: ADAM %s, Apache Spark %s, and Scala %s"
      .format(about.adamVersion, about.sparkVersion, about.scalaVersion))
  }

  private def printCommands() {
    printLogo()
    println("\nUsage: cannoli-submit [<spark-args> --] <cannoli-args>")
    println("\nChoose one of the following commands:")
    commandGroups.foreach { grp =>
      println("\n%s".format(grp.name))
      grp.commands.foreach(cmd =>
        println("%20s : %s".format(cmd.commandName, cmd.commandDescription)))
    }
    println("\n")
  }

  def apply(args: Array[String]) {
    log.info("Cannoli invoked with args: %s".format(argsToString(args)))
    if (args.length < 1) {
      printCommands()
    } else if (args.contains("--version") || args.contains("-version")) {
      printVersion()
    } else {

      val commands =
        for {
          grp <- commandGroups
          cmd <- grp.commands
        } yield cmd

      commands.find(_.commandName == args(0)) match {
        case None => printCommands()
        case Some(cmd) =>
          init(Args4j[InitArgs](args drop 1, ignoreCmdLineExceptions = true))
          cmd.apply(args drop 1).run()
      }
    }
  }

  // Attempts to format the `args` array into a string in a way
  // suitable for copying and pasting back into the shell.
  private def argsToString(args: Array[String]): String = {
    def escapeArg(s: String) = "\"" + s.replaceAll("\\\"", "\\\\\"") + "\""
    args.map(escapeArg).mkString(" ")
  }

  private def init(args: InitArgs) {
    // Set parquet logging (default: severe)
    ParquetLogger.hadoopLoggerLevel(parse(args.logLevel))
  }
}

/**
 * Cannoli module, binds the default list of command groups.
 */
class CannoliModule extends AbstractModule with ScalaModule {
  override def configure() {
    bind[List[CommandGroup]].toInstance(Cannoli.defaultCommandGroups)
  }
}
