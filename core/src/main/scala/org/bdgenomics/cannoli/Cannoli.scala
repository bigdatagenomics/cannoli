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

import htsjdk.samtools.ValidationStringency
import org.bdgenomics.adam.rdd.feature.FeatureRDD
import org.bdgenomics.adam.rdd.fragment.FragmentRDD
import org.bdgenomics.adam.rdd.read.AlignmentRecordRDD
import org.bdgenomics.adam.rdd.variant.VariantContextRDD

/**
 * Implicits on Cannoli function source data sets.
 */
object Cannoli {
  implicit class CannoliAlignmentRecordRDD(rdd: AlignmentRecordRDD) {

    /**
     * Call variants from the alignments in this AlignmentRecordRDD with freebayes via Cannoli.
     *
     * @param args Freebayes function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextRDD.
     */
    def callVariantsWithFreebayes(
      args: FreebayesArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextRDD = {
      new Freebayes(args, stringency, rdd.rdd.context).apply(rdd)
    }

    /**
     * Call variants from the alignments in this AlignmentRecordRDD with samtools mpileup via Cannoli.
     *
     * @param args Samtools mpileup function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextRDD.
     */
    def callVariantsWithSamtoolsMpileup(
      args: SamtoolsMpileupArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextRDD = {
      new SamtoolsMpileup(args, stringency, rdd.rdd.context).apply(rdd)
    }
  }

  implicit class CannoliFeatureRDD(rdd: FeatureRDD) {

    /**
     * Intersect the features in this FeatureRDD with bedtools via Cannoli.
     *
     * @param args Bedtools intersect function arguments.
     * @return FeatureRDD.
     */
    def intersectWithBedtools(args: BedtoolsIntersectArgs): FeatureRDD = {
      new BedtoolsIntersect(args, rdd.rdd.context).apply(rdd)
    }
  }

  implicit class CannoliFragmentRDD(rdd: FragmentRDD) {

    /**
     * Align the reads in this FragmentRDD with bowtie via Cannoli.
     *
     * @param args Bowtie function arguments.
     * @return AlignmentRecordRDD.
     */
    def alignWithBowtie(args: BowtieArgs): AlignmentRecordRDD = {
      new Bowtie(args, rdd.rdd.context).apply(rdd)
    }

    /**
     * Align the reads in this FragmentRDD with bowtie2 via Cannoli.
     *
     * @param args Bowtie2 function arguments.
     * @return AlignmentRecordRDD.
     */
    def alignWithBowtie2(args: Bowtie2Args): AlignmentRecordRDD = {
      new Bowtie2(args, rdd.rdd.context).apply(rdd)
    }

    /**
     * Align the reads in this FragmentRDD with bwa via Cannoli.
     *
     * @param args Bwa function arguments.
     * @return AlignmentRecordRDD.
     */
    def alignWithBwa(args: BwaArgs): AlignmentRecordRDD = {
      new Bwa(args, rdd.rdd.context).apply(rdd)
    }

    /**
     * Align the reads in this FragmentRDD with minimap2 via Cannoli.
     *
     * @param args Minimap2 function arguments.
     * @return AlignmentRecordRDD.
     */
    def alignWithMinimap2(args: Minimap2Args): AlignmentRecordRDD = {
      new Minimap2(args, rdd.rdd.context).apply(rdd)
    }
  }

  implicit class CannoliVariantContextRDD(rdd: VariantContextRDD) {

    /**
     * Annotate the variant contexts in this VariantContextRDD with SnpEff via Cannoli.
     *
     * @param args SnpEff function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextRDD.
     */
    def annotateWithSnpEff(
      args: SnpEffArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextRDD = {
      new SnpEff(args, stringency, rdd.rdd.context).apply(rdd)
    }

    /**
     * Annotate the variant contexts in this VariantContextRDD with Ensembl VEP via Cannoli.
     *
     * @param args Vep function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextRDD.
     */
    def annotateWithVep(
      args: VepArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextRDD = {
      new Vep(args, stringency, rdd.rdd.context).apply(rdd)
    }

    /**
     * Normalize the variant contexts in this VariantContextRDD with bcftools norm via Cannoli.
     *
     * @param args Bcftools norm function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextRDD.
     */
    def normalizeWithBcftools(
      args: BcftoolsNormArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextRDD = {
      new BcftoolsNorm(args, stringency, rdd.rdd.context).apply(rdd)
    }

    /**
     * Normalize the variant contexts in this VariantContextRDD with vt normalize via Cannoli.
     *
     * @param args Vt normalize function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextRDD.
     */
    def normalizeWithVt(
      args: VtNormalizeArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextRDD = {
      new VtNormalize(args, stringency, rdd.rdd.context).apply(rdd)
    }
  }
}
