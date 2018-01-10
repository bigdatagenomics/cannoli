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
package org.bdgenomics.cannoli

import org.apache.hadoop.fs.{ FileSystem, Path }
import org.apache.spark.SparkContext
import org.bdgenomics.utils.cli.{ Args4jBase, BDGSparkCommand }
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Option => Args4jOption }

abstract class CannoliArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable_path", usage = "Path to the wrapped executable.")
  var executablePath: String

  @Args4jOption(required = false, name = "-docker_command", usage = "The docker command to run. Defaults to 'docker'.")
  var dockerCommand: String = "docker"

  @Args4jOption(required = false, name = "-docker_image", usage = "Docker image to use.")
  var dockerImage: String

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch the executable. If false, uses the executable path.")
  var useDocker: Boolean = false

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

abstract class CannoliCommand[A <: CannoliArgs] extends BDGSparkCommand[A] with Logging {
  // todo: various docker stuff
}

abstract class CannoliAlignerArgs extends CannoliArgs {
  @Args4jOption(required = true, name = "-index", usage = "Path to the index.")
  var indexPath: String

  @Args4jOption(required = false, name = "-add_indices", usage = "Adds index files via SparkFiles mechanism. Defaults to false.")
  var addIndices: Boolean = false
}

abstract class CannoliAlignerCommand[A <: CannoliAlignerArgs] extends CannoliCommand {

  protected def getIndexPaths(
    sc: SparkContext,
    indexPath: String,
    requiredExtensions: Seq[String],
    optionalExtensions: Seq[String]): Seq[String] = {

    // oddly, the hadoop fs apis don't seem to have a way to do this?
    def canonicalizePath(fs: FileSystem, path: Path): String = {
      val fsUri = fs.getUri()
      new Path(fsUri.getScheme, fsUri.getAuthority,
        Path.getPathWithoutSchemeAndAuthority(path).toString).toString
    }

    def optionalPath(ext: String): Option[String] = {
      val path = new Path(indexPath, ext)
      val fs = path.getFileSystem(sc.hadoopConfiguration)
      if (fs.exists(path)) {
        Some(canonicalizePath(fs, path))
      } else {
        None
      }
    }

    val pathsWithScheme = requiredExtensions.map(ext => {
      optionalPath(ext).getOrElse({
        throw new IllegalStateException(
          "Required index file %s%s does not exist.".format(indexPath, ext))
      })
    })

    val optionalPathsWithScheme = optionalExtensions.flatMap(optionalPath)

    pathsWithScheme ++ optionalPathsWithScheme
  }

  // todo: various index stuff
}
