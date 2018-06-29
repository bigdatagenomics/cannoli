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

import java.io.FileNotFoundException
import org.apache.hadoop.fs.{ Path, PathFilter }
import org.apache.spark.SparkContext

/**
 * Cannoli function.
 *
 * @param sc Spark context.
 * @tparam X Cannoli function argument type parameter.
 * @tparam Y Cannoli function result type parameter.
 */
abstract class CannoliFn[X, Y](val sc: SparkContext) extends Function1[X, Y] {

  def absolute(pathName: String): String = {
    val path = new Path(pathName)

    // get the underlying fs for the file
    val fs = Option(path.getFileSystem(sc.hadoopConfiguration)).getOrElse(
      throw new FileNotFoundException(
        s"Couldn't find filesystem for ${path.toUri} with Hadoop configuration ${sc.hadoopConfiguration}"
      ))

    Path.getPathWithoutSchemeAndAuthority(fs.resolvePath(path)).toString
  }

  def root(pathName: String): String = {
    val path = new Path(pathName)

    // get the underlying fs for the file
    val fs = Option(path.getFileSystem(sc.hadoopConfiguration)).getOrElse(
      throw new FileNotFoundException(
        s"Couldn't find filesystem for ${path.toUri} with Hadoop configuration ${sc.hadoopConfiguration}"
      ))

    Path.getPathWithoutSchemeAndAuthority(fs.resolvePath(path).getParent()).toString
  }

  def files(pathName: String): Seq[String] = {
    files(pathName, new PathFilter() {
      def accept(path: Path): Boolean = {
        return true
      }
    })
  }

  def files(pathName: String, filter: PathFilter): Seq[String] = {
    val path = new Path(pathName)

    // get the underlying fs for the file
    val fs = Option(path.getFileSystem(sc.hadoopConfiguration)).getOrElse(
      throw new FileNotFoundException(
        s"Couldn't find filesystem for ${path.toUri} with Hadoop configuration ${sc.hadoopConfiguration}"
      ))

    // elaborate out the path; this returns FileStatuses
    val paths = if (fs.isDirectory(path)) {
      val paths = fs.listStatus(path)
      if (paths.isEmpty) {
        throw new FileNotFoundException(
          s"Couldn't find any files matching ${path.toUri}, directory is empty"
        )
      }
      fs.listStatus(path, filter)
    } else {
      val paths = fs.globStatus(path)
      if (paths == null || paths.isEmpty) {
        throw new FileNotFoundException(
          s"Couldn't find any files matching ${path.toUri}"
        )
      }
      fs.globStatus(path, filter)
    }

    // the path must match PathFilter
    if (paths == null || paths.isEmpty) {
      throw new FileNotFoundException(
        s"Couldn't find any files matching ${path.toUri} for the requested PathFilter"
      )
    }

    // map the paths returned to their paths
    paths.map(_.getPath.toString)
  }
}
