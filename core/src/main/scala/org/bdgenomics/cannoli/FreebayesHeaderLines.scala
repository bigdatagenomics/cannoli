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

import org.bdgenomics.adam.converters.DefaultHeaderLines

import htsjdk.variant.vcf.{
  VCFFormatHeaderLine,
  VCFHeaderLine,
  VCFHeaderLineCount,
  VCFHeaderLineType,
  VCFInfoHeaderLine
}

/**
 * Freebayes VCF header lines.
 */
private[cannoli] object FreebayesHeaderLines {

  // ##INFO=<ID=NS,Number=1,Type=Integer,Description="Number of samples with data">
  lazy val ns = new VCFInfoHeaderLine("NS",
    1,
    VCFHeaderLineType.Integer,
    "Number of samples with data")

  // ##INFO=<ID=DP,Number=1,Type=Integer,Description="Total read depth at the locus">
  lazy val dp = new VCFInfoHeaderLine("DP",
    1,
    VCFHeaderLineType.Integer,
    "Total read depth at the locus")

  // ##INFO=<ID=DPB,Number=1,Type=Float,Description="Total read depth per bp at the locus; bases in reads overlapping / bases in haplotype">
  lazy val dpb = new VCFInfoHeaderLine("DPB",
    1,
    VCFHeaderLineType.Float,
    "Total read depth per bp at the locus; bases in reads overlapping / bases in haplotype")

  // ##INFO=<ID=AN,Number=1,Type=Integer,Description="Total number of alleles in called genotypes">
  lazy val an = new VCFInfoHeaderLine("AN",
    1,
    VCFHeaderLineType.Integer,
    "Total number of alleles in called genotypes")

  // ##INFO=<ID=RO,Number=1,Type=Integer,Description="Count of full observations of the reference haplotype.">
  lazy val ro = new VCFInfoHeaderLine("RO",
    1,
    VCFHeaderLineType.Integer,
    "Count of full observations of the reference haplotype.")

  // ##INFO=<ID=AO,Number=A,Type=Integer,Description="Count of full observations of this alternate haplotype.">
  lazy val ao = new VCFInfoHeaderLine("AO",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Integer,
    "Count of full observations of this alternate haplotype.")

  // ##INFO=<ID=PRO,Number=1,Type=Float,Description="Reference allele observation count, with partial observations recorded fractionally">
  lazy val pro = new VCFInfoHeaderLine("PRO",
    1,
    VCFHeaderLineType.Float,
    "Reference allele observation count, with partial observations recorded fractionally")

  // ##INFO=<ID=PAO,Number=A,Type=Float,Description="Alternate allele observations, with partial observations recorded fractionally">
  lazy val pao = new VCFInfoHeaderLine("PAO",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Alternate allele observations, with partial observations recorded fractionally")

  // ##INFO=<ID=SRF,Number=1,Type=Integer,Description="Number of reference observations on the forward strand">
  lazy val srf = new VCFInfoHeaderLine("SRF",
    1,
    VCFHeaderLineType.Integer,
    "Number of reference observations on the forward strand")

  // ##INFO=<ID=SRR,Number=1,Type=Integer,Description="Number of reference observations on the reverse strand">
  lazy val srr = new VCFInfoHeaderLine("SRR",
    1,
    VCFHeaderLineType.Integer,
    "Number of reference observations on the reverse strand")

  // ##INFO=<ID=SAF,Number=A,Type=Integer,Description="Number of alternate observations on the forward strand">
  lazy val saf = new VCFInfoHeaderLine("SAF",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Integer,
    "Number of alternate observations on the forward strand")

  // ##INFO=<ID=SAR,Number=A,Type=Integer,Description="Number of alternate observations on the reverse strand">
  lazy val sar = new VCFInfoHeaderLine("SAR",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Integer,
    "Number of alternate observations on the reverse strand")

  // ##INFO=<ID=SRP,Number=1,Type=Float,Description="Strand balance probability for the reference allele: Phred-scaled upper-bounds estimate of the probability of observing the deviation between SRF and SRR given E(SRF/SRR) ~ 0.5, derived using Hoeffding's inequality">
  lazy val srp = new VCFInfoHeaderLine("SRP",
    1,
    VCFHeaderLineType.Float,
    "Strand balance probability for the reference allele: Phred-scaled upper-bounds estimate of the probability of observing the deviation between SRF and SRR given E(SRF/SRR) ~ 0.5, derived using Hoeffding's inequality")

  // ##INFO=<ID=SAP,Number=A,Type=Float,Description="Strand balance probability for the alternate allele: Phred-scaled upper-bounds estimate of the probability of observing the deviation between SAF and SAR given E(SAF/SAR) ~ 0.5, derived using Hoeffding's inequality">
  lazy val sap = new VCFInfoHeaderLine("SAP",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Strand balance probability for the alternate allele: Phred-scaled upper-bounds estimate of the probability of observing the deviation between SAF and SAR given E(SAF/SAR) ~ 0.5, derived using Hoeffding's inequality")

  // ##INFO=<ID=AB,Number=A,Type=Float,Description="Allele balance at heterozygous sites: a number between 0 and 1 representing the ratio of reads showing the reference allele to all reads, considering only reads from individuals called as heterozygous">
  lazy val ab = new VCFInfoHeaderLine("AB",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Allele balance at heterozygous sites: a number between 0 and 1 representing the ratio of reads showing the reference allele to all reads, considering only reads from individuals called as heterozygous")

  // ##INFO=<ID=ABP,Number=A,Type=Float,Description="Allele balance probability at heterozygous sites: Phred-scaled upper-bounds estimate of the probability of observing the deviation between ABR and ABA given E(ABR/ABA) ~ 0.5, derived using Hoeffding's inequality">
  lazy val abp = new VCFInfoHeaderLine("ABP",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Allele balance probability at heterozygous sites: Phred-scaled upper-bounds estimate of the probability of observing the deviation between ABR and ABA given E(ABR/ABA) ~ 0.5, derived using Hoeffding's inequality")

  // ##INFO=<ID=RUN,Number=A,Type=Integer,Description="Run length: the number of consecutive repeats of the alternate allele in the reference genome">
  lazy val run = new VCFInfoHeaderLine("RUN",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Integer,
    "Run length: the number of consecutive repeats of the alternate allele in the reference genome")

  // ##INFO=<ID=RPP,Number=A,Type=Float,Description="Read Placement Probability: Phred-scaled upper-bounds estimate of the probability of observing the deviation between RPL and RPR given E(RPL/RPR) ~ 0.5, derived using Hoeffding's inequality">
  lazy val rpp = new VCFInfoHeaderLine("RPP",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Read Placement Probability: Phred-scaled upper-bounds estimate of the probability of observing the deviation between RPL and RPR given E(RPL/RPR) ~ 0.5, derived using Hoeffding's inequality")

  // ##INFO=<ID=RPPR,Number=1,Type=Float,Description="Read Placement Probability for reference observations: Phred-scaled upper-bounds estimate of the probability of observing the deviation between RPL and RPR given E(RPL/RPR) ~ 0.5, derived using Hoeffding's inequality">
  lazy val rppr = new VCFInfoHeaderLine("RPPR",
    1,
    VCFHeaderLineType.Float,
    "Read Placement Probability for reference observations: Phred-scaled upper-bounds estimate of the probability of observing the deviation between RPL and RPR given E(RPL/RPR) ~ 0.5, derived using Hoeffding's inequality")

  // ##INFO=<ID=RPL,Number=A,Type=Float,Description="Reads Placed Left: number of reads supporting the alternate balanced to the left (5') of the alternate allele">
  lazy val rpl = new VCFInfoHeaderLine("RPL",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Reads Placed Left: number of reads supporting the alternate balanced to the left (5') of the alternate allele")

  // ##INFO=<ID=RPR,Number=A,Type=Float,Description="Reads Placed Right: number of reads supporting the alternate balanced to the right (3') of the alternate allele">
  lazy val rpr = new VCFInfoHeaderLine("RPR",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Reads Placed Right: number of reads supporting the alternate balanced to the right (3') of the alternate allele")

  // ##INFO=<ID=EPP,Number=A,Type=Float,Description="End Placement Probability: Phred-scaled upper-bounds estimate of the probability of observing the deviation between EL and ER given E(EL/ER) ~ 0.5, derived using Hoeffding's inequality">
  lazy val epp = new VCFInfoHeaderLine("EPP",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "End Placement Probability: Phred-scaled upper-bounds estimate of the probability of observing the deviation between EL and ER given E(EL/ER) ~ 0.5, derived using Hoeffding's inequality")

  // ##INFO=<ID=EPPR,Number=1,Type=Float,Description="End Placement Probability for reference observations: Phred-scaled upper-bounds estimate of the probability of observing the deviation between EL and ER given E(EL/ER) ~ 0.5, derived using Hoeffding's inequality">
  lazy val eppr = new VCFInfoHeaderLine("EPPR",
    1,
    VCFHeaderLineType.Float,
    "End Placement Probability for reference observations: Phred-scaled upper-bounds estimate of the probability of observing the deviation between EL and ER given E(EL/ER) ~ 0.5, derived using Hoeffding's inequality")

  // ##INFO=<ID=DPRA,Number=A,Type=Float,Description="Alternate allele depth ratio.  Ratio between depth in samples with each called alternate allele and those without.">
  lazy val dpra = new VCFInfoHeaderLine("DPRA",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Alternate allele depth ratio.  Ratio between depth in samples with each called alternate allele and those without.")

  // ##INFO=<ID=ODDS,Number=1,Type=Float,Description="The log odds ratio of the best genotype combination to the second-best.">
  lazy val odds = new VCFInfoHeaderLine("ODDS",
    1,
    VCFHeaderLineType.Float,
    "The log odds ratio of the best genotype combination to the second-best.")

  // ##INFO=<ID=GTI,Number=1,Type=Integer,Description="Number of genotyping iterations required to reach convergence or bailout.">
  lazy val gti = new VCFInfoHeaderLine("GTI",
    1,
    VCFHeaderLineType.Integer,
    "Number of genotyping iterations required to reach convergence or bailout.")

  // ##INFO=<ID=TYPE,Number=A,Type=String,Description="The type of allele, either snp, mnp, ins, del, or complex.">
  lazy val tpe = new VCFInfoHeaderLine("TYPE",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.String,
    "The type of allele, either snp, mnp, ins, del, or complex.")

  // ##INFO=<ID=NUMALT,Number=1,Type=Integer,Description="Number of unique non-reference alleles in called genotypes at this position.">
  lazy val numalt = new VCFInfoHeaderLine("NUMALT",
    1,
    VCFHeaderLineType.Integer,
    "Number of unique non-reference alleles in called genotypes at this position.")

  // ##INFO=<ID=MEANALT,Number=A,Type=Float,Description="Mean number of unique non-reference allele observations per sample with the corresponding alternate alleles.">
  lazy val meanalt = new VCFInfoHeaderLine("MEANALT",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Mean number of unique non-reference allele observations per sample with the corresponding alternate alleles.")

  // ##INFO=<ID=LEN,Number=A,Type=Integer,Description="allele length">
  lazy val len = new VCFInfoHeaderLine("LEN",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Integer,
    "allele length")

  // ##INFO=<ID=MQM,Number=A,Type=Float,Description="Mean mapping quality of observed alternate alleles">
  lazy val mqm = new VCFInfoHeaderLine("MQM",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Mean mapping quality of observed alternate alleles")

  // ##INFO=<ID=MQMR,Number=1,Type=Float,Description="Mean mapping quality of observed reference alleles">
  lazy val mqmr = new VCFInfoHeaderLine("MQMR",
    1,
    VCFHeaderLineType.Float,
    "Mean mapping quality of observed reference alleles")

  // ##INFO=<ID=PAIRED,Number=A,Type=Float,Description="Proportion of observed alternate alleles which are supported by properly paired read fragments">
  lazy val paired = new VCFInfoHeaderLine("PAIRED",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Proportion of observed alternate alleles which are supported by properly paired read fragments")

  // ##INFO=<ID=PAIREDR,Number=1,Type=Float,Description="Proportion of observed reference alleles which are supported by properly paired read fragments">
  lazy val pairedr = new VCFInfoHeaderLine("PAIREDR",
    1,
    VCFHeaderLineType.Float,
    "Proportion of observed reference alleles which are supported by properly paired read fragments")

  // ##INFO=<ID=MIN_DP,Number=1,Type=Integer,Description="Minimum depth in gVCF output block.">
  lazy val minDp = new VCFInfoHeaderLine("MIN_DP",
    1,
    VCFHeaderLineType.Integer,
    "Minimum depth in gVCF output block.")

  // ##INFO=<ID=END,Number=1,Type=Integer,Description="Last position (inclusive) in gVCF output record.">
  lazy val end = new VCFInfoHeaderLine("END",
    1,
    VCFHeaderLineType.Integer,
    "Last position (inclusive) in gVCF output record.")

  // ##INFO=<ID=technology.ILLUMINA,Number=A,Type=Float,Description="Fraction of observations supporting the alternate observed in reads from ILLUMINA">
  lazy val technologyIllumina = new VCFInfoHeaderLine("technology.ILLUMINA",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Fraction of observations supporting the alternate observed in reads from ILLUMINA")

  /** Freebayes VCF info header lines. */
  lazy val infoHeaderLines: Seq[VCFInfoHeaderLine] = Seq(
    ns, dp, dpb, an, ro, ao, pro, pao, srf, srr, saf, sar,
    srp, sap, ab, abp, run, rpp, rppr, rpl, rpr, epp, eppr, dpra,
    odds, gti, tpe, numalt, meanalt, len, mqm, mqmr, paired,
    pairedr, minDp, end, technologyIllumina
  )

  // ##FORMAT=<ID=GL,Number=G,Type=Float,Description="Genotype Likelihood, log10-scaled likelihoods of the data given the called genotype for each possible genotype generated from the reference and alternate alleles given the sample ploidy">
  lazy val gl = new VCFFormatHeaderLine("GL",
    VCFHeaderLineCount.G,
    VCFHeaderLineType.Float,
    "Genotype Likelihood, log10-scaled likelihoods of the data given the called genotype for each possible genotype generated from the reference and alternate alleles given the sample ploidy")

  // ##FORMAT=<ID=RO,Number=1,Type=Integer,Description="Reference allele observation count">
  lazy val roFormat = new VCFFormatHeaderLine("RO",
    1,
    VCFHeaderLineType.Integer,
    "Reference allele observation count")

  // ##FORMAT=<ID=QR,Number=1,Type=Integer,Description="Sum of quality of the reference observations">
  lazy val qr = new VCFFormatHeaderLine("QR",
    1,
    VCFHeaderLineType.Integer,
    "Sum of quality of the reference observations")

  // ##FORMAT=<ID=AO,Number=A,Type=Integer,Description="Alternate allele observation count">
  lazy val aoFormat = new VCFFormatHeaderLine("AO",
    VCFHeaderLineCount.G,
    VCFHeaderLineType.Float,
    "Alternate allele observation count")

  // ##FORMAT=<ID=QA,Number=A,Type=Integer,Description="Sum of quality of the alternate observations">
  lazy val qa = new VCFFormatHeaderLine("QA",
    VCFHeaderLineCount.A,
    VCFHeaderLineType.Float,
    "Sum of quality of the alternate observations")

  // ##FORMAT=<ID=MIN_DP,Number=1,Type=Integer,Description="Minimum depth in gVCF output block.">
  lazy val minDpFormat = new VCFFormatHeaderLine("MIN_DP",
    1,
    VCFHeaderLineType.Integer,
    "Minimum depth in gVCF output block.")

  /** Freebayes VCF format header lines. */
  lazy val formatHeaderLines: Seq[VCFFormatHeaderLine] = Seq(
    gl, roFormat, qr, aoFormat, qa, minDpFormat
  )

  /** Freebayes VCF header lines. */
  lazy val allHeaderLines: Seq[VCFHeaderLine] =
    DefaultHeaderLines.infoHeaderLines ++ infoHeaderLines ++
      DefaultHeaderLines.formatHeaderLines ++ formatHeaderLines
}
