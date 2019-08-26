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
import org.bdgenomics.adam.rdd.feature.FeatureDataset
import org.bdgenomics.adam.rdd.fragment.FragmentDataset
import org.bdgenomics.adam.rdd.read.AlignmentRecordDataset
import org.bdgenomics.adam.rdd.variant.VariantContextDataset

/**
 * Implicits on Cannoli function source data sets.
 */
object Cannoli {
  implicit class CannoliAlignmentRecordDataset(alignments: AlignmentRecordDataset) {

    /**
     * Call variants from the alignments in this AlignmentRecordDataset with freebayes via Cannoli.
     *
     * @param args Freebayes function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextDataset.
     */
    def callVariantsWithFreebayes(
      args: FreebayesArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextDataset = {
      new Freebayes(args, stringency, alignments.rdd.context).apply(alignments)
    }

    /**
     * Call variants from the alignments in this AlignmentRecordDataset with bcftools mpileup via Cannoli.
     *
     * @param args Bcftools mpileup function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextDataset.
     */
    def callVariantsWithBcftoolsMpileup(
      args: BcftoolsMpileupArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextDataset = {
      new BcftoolsMpileup(args, stringency, alignments.rdd.context).apply(alignments)
    }

    /**
     * Call variants from the alignments in this AlignmentRecordDataset with samtools mpileup via Cannoli.
     *
     * @param args Samtools mpileup function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextDataset.
     */
    def callVariantsWithSamtoolsMpileup(
      args: SamtoolsMpileupArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextDataset = {
      new SamtoolsMpileup(args, stringency, alignments.rdd.context).apply(alignments)
    }
  }

  implicit class CannoliFeatureDataset(features: FeatureDataset) {

    /**
     * Intersect the features in this FeatureDataset with bedtools via Cannoli.
     *
     * @param args Bedtools intersect function arguments.
     * @return FeatureDataset.
     */
    def intersectWithBedtools(args: BedtoolsIntersectArgs): FeatureDataset = {
      new BedtoolsIntersect(args, features.rdd.context).apply(features)
    }
  }

  implicit class CannoliFragmentDataset(fragments: FragmentDataset) {

    /**
     * Align the reads in this FragmentDataset with bowtie via Cannoli.
     *
     * @param args Bowtie function arguments.
     * @return AlignmentRecordDataset.
     */
    def alignWithBowtie(args: BowtieArgs): AlignmentRecordDataset = {
      new Bowtie(args, fragments.rdd.context).apply(fragments)
    }

    /**
     * Align the reads in this FragmentDataset with bowtie2 via Cannoli.
     *
     * @param args Bowtie2 function arguments.
     * @return AlignmentRecordDataset.
     */
    def alignWithBowtie2(args: Bowtie2Args): AlignmentRecordDataset = {
      new Bowtie2(args, fragments.rdd.context).apply(fragments)
    }

    /**
     * Align the reads in this FragmentDataset with bwa via Cannoli.
     *
     * @param args Bwa function arguments.
     * @return AlignmentRecordDataset.
     */
    def alignWithBwa(args: BwaArgs): AlignmentRecordDataset = {
      new Bwa(args, fragments.rdd.context).apply(fragments)
    }

    /**
     * Align the reads in this FragmentDataset with GEM-Mapper via Cannoli.
     *
     * @param args GEM-Mapper function arguments.
     * @return AlignmentRecordDataset.
     */
    def alignWithGem(args: GemArgs): AlignmentRecordDataset = {
      new Gem(args, fragments.rdd.context).apply(fragments)
    }

    /**
     * Align the reads in this FragmentDataset with minimap2 via Cannoli.
     *
     * @param args Minimap2 function arguments.
     * @return AlignmentRecordDataset.
     */
    def alignWithMinimap2(args: Minimap2Args): AlignmentRecordDataset = {
      new Minimap2(args, fragments.rdd.context).apply(fragments)
    }

    /**
     * Align the reads in this FragmentDataset with STAR via Cannoli.
     *
     * @param args STAR function arguments.
     * @return AlignmentRecordDataset.
     */
    def alignWithStar(args: StarArgs): AlignmentRecordDataset = {
      new Star(args, fragments.rdd.context).apply(fragments)
    }
  }

  implicit class CannoliVariantContextDataset(vcs: VariantContextDataset) {

    /**
     * Annotate the variant contexts in this VariantContextDataset with SnpEff via Cannoli.
     *
     * @param args SnpEff function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextDataset.
     */
    def annotateWithSnpEff(
      args: SnpEffArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextDataset = {
      new SnpEff(args, stringency, vcs.rdd.context).apply(vcs)
    }

    /**
     * Annotate the variant contexts in this VariantContextDataset with Ensembl VEP via Cannoli.
     *
     * @param args Vep function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextDataset.
     */
    def annotateWithVep(
      args: VepArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextDataset = {
      new Vep(args, stringency, vcs.rdd.context).apply(vcs)
    }

    /**
     * Call variant contexts in this VariantContextDataset with bcftools call via Cannoli.
     *
     * @param args Bcftools call function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextDataset.
     */
    def callWithBcftools(
      args: BcftoolsCallArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextDataset = {
      new BcftoolsCall(args, stringency, vcs.rdd.context).apply(vcs)
    }

    /**
     * Normalize the variant contexts in this VariantContextDataset with bcftools norm via Cannoli.
     *
     * @param args Bcftools norm function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextDataset.
     */
    def normalizeWithBcftools(
      args: BcftoolsNormArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextDataset = {
      new BcftoolsNorm(args, stringency, vcs.rdd.context).apply(vcs)
    }

    /**
     * Normalize the variant contexts in this VariantContextDataset with vt normalize via Cannoli.
     *
     * @param args Vt normalize function arguments.
     * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
     * @return VariantContextDataset.
     */
    def normalizeWithVt(
      args: VtNormalizeArgs,
      stringency: ValidationStringency = ValidationStringency.LENIENT): VariantContextDataset = {
      new VtNormalize(args, stringency, vcs.rdd.context).apply(vcs)
    }
  }
}
