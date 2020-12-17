# Cannoli Changelog #

### Version 0.11.1 ###

**Closed issues:**

 - Release artifacts for version 0.11.0 are incorrect [\#284](https://github.com/bigdatagenomics/cannoli/issues/284)

**Merged and closed pull requests:**

 - [CANNOLI-284] Fix release script, default is Spark 3 Scala 2.12 profile [\#285](https://github.com/bigdatagenomics/cannoli/pull/285) ([heuermh](https://github.com/heuermh))


### Version 0.11.0 ###

**Closed issues:**

 - Bump ADAM dependency version to 0.33.0 [\#278](https://github.com/bigdatagenomics/cannoli/issues/278)
 - Refactor variant callers to fn[AlignmentDataset, GenotypeDataset] [\#272](https://github.com/bigdatagenomics/cannoli/issues/272)
 - Add additional arguments for freebayes command line wrapper [\#271](https://github.com/bigdatagenomics/cannoli/issues/271)
 - conda install cannoli issue  [\#268](https://github.com/bigdatagenomics/cannoli/issues/268)
 - Update Spark dependency to version 3.0.1 [\#265](https://github.com/bigdatagenomics/cannoli/issues/265)
 - Issue while saving variants as vcf. [\#260](https://github.com/bigdatagenomics/cannoli/issues/260)
 - Exception encountered while loading parquet files generated using Cannoli CLI. [\#258](https://github.com/bigdatagenomics/cannoli/issues/258)
 - Refactor Bwa class names to BwaMem [\#257](https://github.com/bigdatagenomics/cannoli/issues/257)
 - Add bwa-mem2 for aligning fragments [\#255](https://github.com/bigdatagenomics/cannoli/issues/255)
 - Default build to Spark 3/Scala 2.12 [\#248](https://github.com/bigdatagenomics/cannoli/issues/248)
 - Can not run vep [\#244](https://github.com/bigdatagenomics/cannoli/issues/244)
 - force evaluating accumulators before assigning them [\#243](https://github.com/bigdatagenomics/cannoli/issues/243)
 - Trying to install/run on HPC using Singularity [\#238](https://github.com/bigdatagenomics/cannoli/issues/238)
 - java.lang.UnsupportedOperationException: empty.max while sorting and indexing. [\#237](https://github.com/bigdatagenomics/cannoli/issues/237)
 - IllegalArgumentException: Reference index for 'chr3' not found in sequence dictionary [\#236](https://github.com/bigdatagenomics/cannoli/issues/236)
 - Cannoli for BWA using Singularity [\#201](https://github.com/bigdatagenomics/cannoli/issues/201)

**Merged and closed pull requests:**

 - [CANNOLI-278] Bump ADAM dependency version to 0.33.0 [\#279](https://github.com/bigdatagenomics/cannoli/pull/279) ([heuermh](https://github.com/heuermh))
 - Update container image versions [\#277](https://github.com/bigdatagenomics/cannoli/pull/277) ([heuermh](https://github.com/heuermh))
 - Update bwa mem example in readme [\#275](https://github.com/bigdatagenomics/cannoli/pull/275) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-271] Add additional command line arguments, header lines for freebayes [\#273](https://github.com/bigdatagenomics/cannoli/pull/273) ([heuermh](https://github.com/heuermh))
 - Update Spark 2.x dependency version to 2.4.7 [\#270](https://github.com/bigdatagenomics/cannoli/pull/270) ([heuermh](https://github.com/heuermh))
 - Bump bowtie2 container image to 2.4.2--py38h1c8e9b9_0 [\#269](https://github.com/bigdatagenomics/cannoli/pull/269) ([heuermh](https://github.com/heuermh))
 - Bump junit from 4.12 to 4.13.1 [\#267](https://github.com/bigdatagenomics/cannoli/pull/267) ([dependabot[bot]](https://github.com/apps/dependabot))
 - [CANNOLI-265] Update Spark dependency version to 3.0.1 [\#266](https://github.com/bigdatagenomics/cannoli/pull/266) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-257] Refactor Bwa class names to BwaMem [\#264](https://github.com/bigdatagenomics/cannoli/pull/264) ([heuermh](https://github.com/heuermh))
 - Default doc links to Spark 3/Scala 2.12 [\#263](https://github.com/bigdatagenomics/cannoli/pull/263) ([heuermh](https://github.com/heuermh))
 - Bump star container image to 2.7.5c--0 [\#262](https://github.com/bigdatagenomics/cannoli/pull/262) ([heuermh](https://github.com/heuermh))
 - Bump star container image to 2.7.5b--0 [\#261](https://github.com/bigdatagenomics/cannoli/pull/261) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-255] Add bwa-mem2 for aligning fragments [\#256](https://github.com/bigdatagenomics/cannoli/pull/256) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-248] Default build to Spark 3/Scala 2.12 [\#254](https://github.com/bigdatagenomics/cannoli/pull/254) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-244] Update --terms and other options for Ensembl VEP [\#251](https://github.com/bigdatagenomics/cannoli/pull/251) ([heuermh](https://github.com/heuermh))


### Version 0.10.0 ###

**Closed issues:**

 - Update ADAM dependency version to 0.32.0 [\#252](https://github.com/bigdatagenomics/cannoli/issues/252)
 - Unable to find bowtie2 index from hdfs [\#246](https://github.com/bigdatagenomics/cannoli/issues/246)
 - Update Spark dependency version to 2.4.6 [\#245](https://github.com/bigdatagenomics/cannoli/issues/245)
 - Use biocontainers as default container image for vt [\#241](https://github.com/bigdatagenomics/cannoli/issues/241)
 - Freebayes CLI command writes to wrong path [\#239](https://github.com/bigdatagenomics/cannoli/issues/239)
 - Support Apache Spark 3.x in build [\#216](https://github.com/bigdatagenomics/cannoli/issues/216)
 - running tools with Conda [\#206](https://github.com/bigdatagenomics/cannoli/issues/206)

**Merged and closed pull requests:**

 - [CANNOLI-252] Update ADAM dependency version to 0.32.0 [\#253](https://github.com/bigdatagenomics/cannoli/pull/253) ([heuermh](https://github.com/heuermh))
 - Update container image versions [\#250](https://github.com/bigdatagenomics/cannoli/pull/250) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-216] Support Apache Spark 3.x in build [\#249](https://github.com/bigdatagenomics/cannoli/pull/249) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-245] Update Spark dependency version to 2.4.6 [\#247](https://github.com/bigdatagenomics/cannoli/pull/247) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-241] Use biocontainers for vt; update container image versions [\#242](https://github.com/bigdatagenomics/cannoli/pull/242) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-239] Write Freebayes CLI command VCF to output path [\#240](https://github.com/bigdatagenomics/cannoli/pull/240) ([heuermh](https://github.com/heuermh))


### Version 0.9.0 ###

**Closed issues:**

 - Update ADAM dependency version to 0.31.0 [\#233](https://github.com/bigdatagenomics/cannoli/issues/233)
 - Bowtie2 2.3.5 does not work with --interleaved option [\#228](https://github.com/bigdatagenomics/cannoli/issues/228)
 - Add -num_threads argument to BlastnArgs [\#226](https://github.com/bigdatagenomics/cannoli/issues/226)
 - blastn ouput FileAlreadyExistsException [\#225](https://github.com/bigdatagenomics/cannoli/issues/225)
 - Jenkins build is not deploying Scala 2.12 snapshots [\#221](https://github.com/bigdatagenomics/cannoli/issues/221)
 - Sam readgroup might need to be fully parametrized [\#219](https://github.com/bigdatagenomics/cannoli/issues/219)
 - Add Jenkins support for Scala 2.12 [\#218](https://github.com/bigdatagenomics/cannoli/issues/218)
 - Scala 2.12 / spark 2.4.4 release [\#215](https://github.com/bigdatagenomics/cannoli/issues/215)
 - Jenkins build status icon link is broken [\#204](https://github.com/bigdatagenomics/cannoli/issues/204)

**Merged and closed pull requests:**

 - [CANNOLI-233] Update ADAM dependency version to 0.31.0 [\#235](https://github.com/bigdatagenomics/cannoli/pull/235) ([heuermh](https://github.com/heuermh))
 - Update Spark dependency version to 2.4.5 [\#234](https://github.com/bigdatagenomics/cannoli/pull/234) ([heuermh](https://github.com/heuermh))
 - Bump freebayes container image to 1.3.3 [\#232](https://github.com/bigdatagenomics/cannoli/pull/232) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-65] Add snap-aligner for alignment [\#231](https://github.com/bigdatagenomics/cannoli/pull/231) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-228] Bump Bowtie2 container image to version 2.3.5.1 [\#230](https://github.com/bigdatagenomics/cannoli/pull/230) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-226] Add -num_threads argument to BlastnArgs [\#227](https://github.com/bigdatagenomics/cannoli/pull/227) ([heuermh](https://github.com/heuermh))
 - Update container image versions [\#223](https://github.com/bigdatagenomics/cannoli/pull/223) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-219] Allow to fully specify read groups for bwa [\#222](https://github.com/bigdatagenomics/cannoli/pull/222) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-218] Add Jenkins support for Scala 2.12 [\#220](https://github.com/bigdatagenomics/cannoli/pull/220) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-215] Add support for Scala 2.12 in build and release scripts [\#217](https://github.com/bigdatagenomics/cannoli/pull/217) ([heuermh](https://github.com/heuermh))
 - Fix broken source paths [\#214](https://github.com/bigdatagenomics/cannoli/pull/214) ([heuermh](https://github.com/heuermh))
 - Fix broken source paths [\#213](https://github.com/bigdatagenomics/cannoli/pull/213) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-194] Move java source from core to new builder module [\#212](https://github.com/bigdatagenomics/cannoli/pull/212) ([heuermh](https://github.com/heuermh))
 - Remove Jenkins build status badge [\#211](https://github.com/bigdatagenomics/cannoli/pull/211) ([heuermh](https://github.com/heuermh))


### Version 0.8.0 ###

**Closed issues:**

 - Update ADAM dependency version to 0.30.0 [\#208](https://github.com/bigdatagenomics/cannoli/issues/208)
 - Cannoli for BWA using Singularity [\#201](https://github.com/bigdatagenomics/cannoli/issues/201)

**Merged and closed pull requests:**

 - Bump github-changes-maven-plugin dependency version to 1.1 [\#210](https://github.com/bigdatagenomics/cannoli/pull/210) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-208] Update ADAM dependency version to 0.30.0 [\#209](https://github.com/bigdatagenomics/cannoli/pull/209) ([heuermh](https://github.com/heuermh))
 - Update docker image tags [\#207](https://github.com/bigdatagenomics/cannoli/pull/207) ([heuermh](https://github.com/heuermh))
 - Update container image versions [\#205](https://github.com/bigdatagenomics/cannoli/pull/205) ([heuermh](https://github.com/heuermh))
 - Adding single-end read STAR-Mapper functions. [\#202](https://github.com/bigdatagenomics/cannoli/pull/202) ([heuermh](https://github.com/heuermh))
 - Update STAR container image to version 2.7.2b. [\#200](https://github.com/bigdatagenomics/cannoli/pull/200) ([heuermh](https://github.com/heuermh))


### Version 0.7.0 ###

**Closed issues:**

 - Bump ADAM dependency version to 0.29.0 [\#198](https://github.com/bigdatagenomics/cannoli/issues/198)
 - Maven javadoc artifacts are missing scaladoc [\#194](https://github.com/bigdatagenomics/cannoli/issues/194)
 - Note dataset type in CLI command docs [\#193](https://github.com/bigdatagenomics/cannoli/issues/193)
 - Add blastn for aligning sequences [\#189](https://github.com/bigdatagenomics/cannoli/issues/189)
 - Support VariantContext as Parquet [\#183](https://github.com/bigdatagenomics/cannoli/issues/183)
 - Add bcftools call command [\#182](https://github.com/bigdatagenomics/cannoli/issues/182)
 - Cannoli fails on missing Bowtie2 index file unnecessarily [\#177](https://github.com/bigdatagenomics/cannoli/issues/177)
 - Read issue - Parquet/Avro schema mismatch [\#176](https://github.com/bigdatagenomics/cannoli/issues/176)
 - Cannoli Freebayes [\#162](https://github.com/bigdatagenomics/cannoli/issues/162)
 - How to use samtool commands  [\#157](https://github.com/bigdatagenomics/cannoli/issues/157)
 - BWA Fails with Output directory already exists for more than 1 executor [\#137](https://github.com/bigdatagenomics/cannoli/issues/137)
 - Add STAR aligner [\#135](https://github.com/bigdatagenomics/cannoli/issues/135)

**Merged and closed pull requests:**

 - [CANNOLI-198] Bump ADAM dependency version to 0.29.0 [\#199](https://github.com/bigdatagenomics/cannoli/pull/199) ([heuermh](https://github.com/heuermh))
 - Add note about tag search for BioContainers Docker install doc [\#197](https://github.com/bigdatagenomics/cannoli/pull/197) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-194] Add scaladoc to Maven javadoc artifacts [\#196](https://github.com/bigdatagenomics/cannoli/pull/196) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-193] Note dataset type in CLI command docs. [\#195](https://github.com/bigdatagenomics/cannoli/pull/195) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-191] Add Magic-BLAST for aligning RNA-seq reads [\#192](https://github.com/bigdatagenomics/cannoli/pull/192) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-189] Add blastn for aligning sequences [\#190](https://github.com/bigdatagenomics/cannoli/pull/190) ([heuermh](https://github.com/heuermh))
 - Bump Docker image for bcftools. [\#188](https://github.com/bigdatagenomics/cannoli/pull/188) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-186] Adding single-end read bowtie2 functions [\#187](https://github.com/bigdatagenomics/cannoli/pull/187) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-183] Support VariantContext as Parquet in cli package. [\#185](https://github.com/bigdatagenomics/cannoli/pull/185) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-182] Add bcftools call command. [\#184](https://github.com/bigdatagenomics/cannoli/pull/184) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-177] Fail fast if index basename not found [\#181](https://github.com/bigdatagenomics/cannoli/pull/181) ([heuermh](https://github.com/heuermh))
 - Remove usage of deprecated o.b.utils.misc.Logging [\#180](https://github.com/bigdatagenomics/cannoli/pull/180) ([heuermh](https://github.com/heuermh))
 - Bump Docker image for freebayes. [\#179](https://github.com/bigdatagenomics/cannoli/pull/179) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-135] Adding STAR for alignment. [\#178](https://github.com/bigdatagenomics/cannoli/pull/178) ([heuermh](https://github.com/heuermh))


### Version 0.6.0 ###

**Closed issues:**

 - Update ADAM dependency version to 0.28.0 [\#172](https://github.com/bigdatagenomics/cannoli/issues/172)
 - Support Gem mapper in Cannoli [\#170](https://github.com/bigdatagenomics/cannoli/issues/170)

**Merged and closed pull requests:**

 - [CANNOLI-172] Update ADAM dependency version to 0.28.0. [\#173](https://github.com/bigdatagenomics/cannoli/pull/173) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-170] Adding GEM-Mapper for alignment. [\#171](https://github.com/bigdatagenomics/cannoli/pull/171) ([heuermh](https://github.com/heuermh))


### Version 0.5.0 ###

**Closed issues:**

 - Update ADAM dependency version to 0.27.0 [\#167](https://github.com/bigdatagenomics/cannoli/issues/167)
 - Update default docker images to latest stable versions [\#164](https://github.com/bigdatagenomics/cannoli/issues/164)
 - Consistent use of indexPath vs referencePath [\#160](https://github.com/bigdatagenomics/cannoli/issues/160)
 - Samtools mpileup -u is deprecated [\#158](https://github.com/bigdatagenomics/cannoli/issues/158)
 - Add additional arguments to Minimap2 [\#154](https://github.com/bigdatagenomics/cannoli/issues/154)
 - Add STAR aligner [\#135](https://github.com/bigdatagenomics/cannoli/issues/135)
 - Add HISAT2 aligner [\#134](https://github.com/bigdatagenomics/cannoli/issues/134)
 - RNA alignment [\#105](https://github.com/bigdatagenomics/cannoli/issues/105)
 - Add SNAP aligner [\#65](https://github.com/bigdatagenomics/cannoli/issues/65)
 - Support alt aware alignment liftover for BWA [\#38](https://github.com/bigdatagenomics/cannoli/issues/38)
 - support for MACS2 or other peak calling [\#35](https://github.com/bigdatagenomics/cannoli/issues/35)

**Merged and closed pull requests:**

 - [CANNOLI-167] Update ADAM dependency version to 0.27.0 [\#168](https://github.com/bigdatagenomics/cannoli/pull/168) ([heuermh](https://github.com/heuermh))
 - Replace utils.Logger with grizzled.slf4j.Logger [\#166](https://github.com/bigdatagenomics/cannoli/pull/166) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-164] Update default docker images to latest stable versions [\#165](https://github.com/bigdatagenomics/cannoli/pull/165) ([heuermh](https://github.com/heuermh))
 - Update minimap2 default docker image to version 2.17 [\#163](https://github.com/bigdatagenomics/cannoli/pull/163) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-160] Rename -fasta_reference to -reference. [\#161](https://github.com/bigdatagenomics/cannoli/pull/161) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-158] Adding bcftools mpileup. [\#159](https://github.com/bigdatagenomics/cannoli/pull/159) ([heuermh](https://github.com/heuermh))
 - Update ADAM dependency to 0.27.0-SNAPSHOT [\#156](https://github.com/bigdatagenomics/cannoli/pull/156) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-154] Add additional arguments to Minimap2 [\#155](https://github.com/bigdatagenomics/cannoli/pull/155) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-65] Adding Snap for alignment. [\#120](https://github.com/bigdatagenomics/cannoli/pull/120) ([heuermh](https://github.com/heuermh))
 - Added MACS2 to cannoli [\#64](https://github.com/bigdatagenomics/cannoli/pull/64) ([gunjanbaid](https://github.com/gunjanbaid))


### Version 0.4.0 ###

**Closed issues:**

 - Bump ADAM dependency to version 0.26.0. [\#152](https://github.com/bigdatagenomics/cannoli/issues/152)
 - Unit tests fail due to surefire plugin issue with Debian/Ubuntu Java8 [\#149](https://github.com/bigdatagenomics/cannoli/issues/149)
 - Error concerning Read names while running Cannoli [\#148](https://github.com/bigdatagenomics/cannoli/issues/148)
 - Update homebrew formula to version 0.3.0 [\#146](https://github.com/bigdatagenomics/cannoli/issues/146)
 - Single_end_reads [\#125](https://github.com/bigdatagenomics/cannoli/issues/125)
 - Support GFF3 in addition to BED for Bedtools command [\#113](https://github.com/bigdatagenomics/cannoli/issues/113)

**Merged and closed pull requests:**

 - [CANNOLI-152] Bump ADAM dependency to version 0.26.0 [\#153](https://github.com/bigdatagenomics/cannoli/pull/153) ([heuermh](https://github.com/heuermh))
 - Use build-helper-maven-plugin for build timestamp [\#151](https://github.com/bigdatagenomics/cannoli/pull/151) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-149] Update maven-surefire-plugin version to 3.0.0-M1. [\#150](https://github.com/bigdatagenomics/cannoli/pull/150) ([heuermh](https://github.com/heuermh))
 - Update default docker images. [\#147](https://github.com/bigdatagenomics/cannoli/pull/147) ([heuermh](https://github.com/heuermh))
 - Add section on using Cannoli interactively from cannoli-shell. [\#144](https://github.com/bigdatagenomics/cannoli/pull/144) ([heuermh](https://github.com/heuermh))


### Version 0.3.0 ###

**Closed issues:**

 - Add implicit methods that attach to source RDD [\#131](https://github.com/bigdatagenomics/cannoli/issues/131)
 - Flip function and command line class names around [\#130](https://github.com/bigdatagenomics/cannoli/issues/130)
 - Add API documentation link and badge [\#128](https://github.com/bigdatagenomics/cannoli/issues/128)
 - Add homebrew formula at brewsci/homebrew-bio [\#124](https://github.com/bigdatagenomics/cannoli/issues/124)
 - Add bioconda recipe [\#123](https://github.com/bigdatagenomics/cannoli/issues/123)
 - Support validation stringency in out formatters [\#122](https://github.com/bigdatagenomics/cannoli/issues/122)
 - Add Ensembl Variant Effect Predictor (VEP) for variant annotation [\#112](https://github.com/bigdatagenomics/cannoli/issues/112)
 - Add Minimap2 for alignment [\#111](https://github.com/bigdatagenomics/cannoli/issues/111)

**Merged and closed pull requests:**

 - Update release script for changelog. [\#143](https://github.com/bigdatagenomics/cannoli/pull/143) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-141] Update ADAM dependency to 0.25.0. [\#142](https://github.com/bigdatagenomics/cannoli/pull/142) ([heuermh](https://github.com/heuermh))
 - Update default docker image for bowtie2. [\#140](https://github.com/bigdatagenomics/cannoli/pull/140) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-138] Update Cannoli per latest ADAM snapshot changes. [\#139](https://github.com/bigdatagenomics/cannoli/pull/139) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-131] Add implicits on Cannoli function source data sets. [\#133](https://github.com/bigdatagenomics/cannoli/pull/133) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-130] Extract function classes to core package. [\#132](https://github.com/bigdatagenomics/cannoli/pull/132) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-128] Adding API documentation link and badge. [\#129](https://github.com/bigdatagenomics/cannoli/pull/129) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-112]  Adding Ensembl Variant Effect Predictor (VEP) for variant annotation [\#127](https://github.com/bigdatagenomics/cannoli/pull/127) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-122] Support validation stringency in out formatters. [\#126](https://github.com/bigdatagenomics/cannoli/pull/126) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-111] Adding Minimap2 for alignment. [\#119](https://github.com/bigdatagenomics/cannoli/pull/119) ([heuermh](https://github.com/heuermh))



### Version 0.2.0 ###

**Closed issues:**

 - Update ADAM dependency version to 0.24.0. [\#118](https://github.com/bigdatagenomics/cannoli/issues/118)
 - Javadoc error and warnings [\#115](https://github.com/bigdatagenomics/cannoli/issues/115)
 - Update pipe method calls due to latest ADAM 0.24.0 snapshot [\#114](https://github.com/bigdatagenomics/cannoli/issues/114)
 - Split commands with subcommands into separate Cannoli CLI classes [\#110](https://github.com/bigdatagenomics/cannoli/issues/110)
 - Jenkins build failing due to upstream changes. [\#108](https://github.com/bigdatagenomics/cannoli/issues/108)
 - Provide functions for use in cannoli-shell or notebooks. [\#104](https://github.com/bigdatagenomics/cannoli/issues/104)
 - Error running BWA with Docker [\#103](https://github.com/bigdatagenomics/cannoli/issues/103)
 - Allow use of Singularity instead of Docker [\#98](https://github.com/bigdatagenomics/cannoli/issues/98)
 - Bump ADAM dependency version to 0.24.0-SNAPSHOT. [\#95](https://github.com/bigdatagenomics/cannoli/issues/95)
 - Drop support for Scala 2.10 and Spark 1.x. [\#94](https://github.com/bigdatagenomics/cannoli/issues/94)
 - Tidy up FreeBayes [\#67](https://github.com/bigdatagenomics/cannoli/issues/67)
 - Support loading reference files from HDFS/other file system [\#50](https://github.com/bigdatagenomics/cannoli/issues/50)
 - Attributes from freebayes header missing from variants and genotypes [\#43](https://github.com/bigdatagenomics/cannoli/issues/43)
 - Factor out docker/mapping code [\#34](https://github.com/bigdatagenomics/cannoli/issues/34)
 - Add wrappers for GMAP and GSNAP aligners [\#29](https://github.com/bigdatagenomics/cannoli/issues/29)
 - Jenkins failures due to missing publish_scaladoc.sh [\#21](https://github.com/bigdatagenomics/cannoli/issues/21)

**Merged and closed pull requests:**

 - [CANNOLI-118] Update ADAM dependency version to 0.24.0. [\#121](https://github.com/bigdatagenomics/cannoli/pull/121) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-110] Split commands with subcommands into separate Cannoli CLI classes. [\#117](https://github.com/bigdatagenomics/cannoli/pull/117) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-115] Fix javadoc error and warnings. [\#116](https://github.com/bigdatagenomics/cannoli/pull/116) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-108] Command argument to pipe is now Seq[String]. [\#109](https://github.com/bigdatagenomics/cannoli/pull/109) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-98] Adding container builder. [\#107](https://github.com/bigdatagenomics/cannoli/pull/107) ([heuermh](https://github.com/heuermh))
 - Allow Singularity to run containers [\#106](https://github.com/bigdatagenomics/cannoli/pull/106) ([jpdna](https://github.com/jpdna))
 - [CANNOLI-95] Bump ADAM dependency version to 0.24.0-SNAPSHOT [\#102](https://github.com/bigdatagenomics/cannoli/pull/102) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-94] Dropping support for Scala 2.10 and Spark 1.x. [\#101](https://github.com/bigdatagenomics/cannoli/pull/101) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-94][CANNOLI-95] Drop support for Scala 2.10 and Spark 1.x. [\#100](https://github.com/bigdatagenomics/cannoli/pull/100) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-43] Use accumulator for VCF header lines. [\#72](https://github.com/bigdatagenomics/cannoli/pull/72) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-104] Provide functions for use in cannoli-shell or notebooks. [\#69](https://github.com/bigdatagenomics/cannoli/pull/69) ([heuermh](https://github.com/heuermh))
 - Add CannoliCommand and CannoliAlignerCommand. [\#54](https://github.com/bigdatagenomics/cannoli/pull/54) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-29] Add minimal GMAP and GSNAP wrappers. [\#32](https://github.com/bigdatagenomics/cannoli/pull/32) ([heuermh](https://github.com/heuermh))


### Version 0.1.0 ###

**Closed issues:**

 - Default Docker images to those on BioContainers registry [\#92](https://github.com/bigdatagenomics/cannoli/issues/92)
 - Bump to ADAM 0.23.0 [\#89](https://github.com/bigdatagenomics/cannoli/issues/89)
 - Jenkins compile error due to upstream 0.23.0-SNAPSHOT changes [\#87](https://github.com/bigdatagenomics/cannoli/issues/87)
 - Error with snpEff [\#86](https://github.com/bigdatagenomics/cannoli/issues/86)
 - Add wrapper for RealTimeGenomics/rtg-tools vcfeval [\#84](https://github.com/bigdatagenomics/cannoli/issues/84)
 - Incorrect case for snpEff when using docker [\#83](https://github.com/bigdatagenomics/cannoli/issues/83)
 - Cannoli-BWA Command Failure // Re Open [\#78](https://github.com/bigdatagenomics/cannoli/issues/78)
 - Cannoli-BWA Command Failure [\#77](https://github.com/bigdatagenomics/cannoli/issues/77)
 - All commands invoked via docker should use --rm=true [\#76](https://github.com/bigdatagenomics/cannoli/issues/76)
 - FreeBayes Issue [\#75](https://github.com/bigdatagenomics/cannoli/issues/75)
 - Read in Fragment not found [\#74](https://github.com/bigdatagenomics/cannoli/issues/74)
 - Cannoli Command [\#73](https://github.com/bigdatagenomics/cannoli/issues/73)
 - FileNotFoundException while loading file from HDFS [\#66](https://github.com/bigdatagenomics/cannoli/issues/66)
 - Create script for releasing to Sonatype OSS repository [\#63](https://github.com/bigdatagenomics/cannoli/issues/63)
 - Compile errors with latest ADAM 0.23.0-SNAPSHOT version [\#55](https://github.com/bigdatagenomics/cannoli/issues/55)
 - Rename FastqInterleaver --> InterleaveFastq [\#51](https://github.com/bigdatagenomics/cannoli/issues/51)
 - Remove SequenceDictionaryReader per ADAMContext.loadSequenceDictionary [\#48](https://github.com/bigdatagenomics/cannoli/issues/48)
 - Move adam-cli to provided scope [\#46](https://github.com/bigdatagenomics/cannoli/issues/46)
 - NoSuchMethodError when running cannoli locally [\#45](https://github.com/bigdatagenomics/cannoli/issues/45)
 -  BCF not yet supported  [\#44](https://github.com/bigdatagenomics/cannoli/issues/44)
 - Interleaver should support uBAM [\#39](https://github.com/bigdatagenomics/cannoli/issues/39)
 - Use --interleaved for bowtie2 [\#36](https://github.com/bigdatagenomics/cannoli/issues/36)
 - Use ADAM tab5 formatter for bowtie [\#33](https://github.com/bigdatagenomics/cannoli/issues/33)
 - Add ability to specify docker command [\#28](https://github.com/bigdatagenomics/cannoli/issues/28)
 - Bump BWA docker version argument [\#27](https://github.com/bigdatagenomics/cannoli/issues/27)
 - BWA-Mount index files into docker container [\#25](https://github.com/bigdatagenomics/cannoli/issues/25)
 - Disable incremental mode in the Scala build [\#23](https://github.com/bigdatagenomics/cannoli/issues/23)
 - Update plugin dependency versions to match bigdatagenomics/adam [\#20](https://github.com/bigdatagenomics/cannoli/issues/20)
 - Bowtie error: reads file does not look like a FASTQ file [\#18](https://github.com/bigdatagenomics/cannoli/issues/18)
 - Update ADAM dependency version to 0.22.0 [\#16](https://github.com/bigdatagenomics/cannoli/issues/16)
 - Add ability to force load format for BWA [\#14](https://github.com/bigdatagenomics/cannoli/issues/14)
 - Add a FASTQ interleaver [\#12](https://github.com/bigdatagenomics/cannoli/issues/12)
 - Properly handle sequence and read group metadata from BWA [\#10](https://github.com/bigdatagenomics/cannoli/issues/10)
 - Set up CI [\#9](https://github.com/bigdatagenomics/cannoli/issues/9)
 - Need to double escape tabs in BWA native [\#7](https://github.com/bigdatagenomics/cannoli/issues/7)
 - Migrate repository to bigdatagenomics organization [\#6](https://github.com/bigdatagenomics/cannoli/issues/6)
 - Allow BWA to save Fragments instead of AlignmentRecords [\#4](https://github.com/bigdatagenomics/cannoli/issues/4)

**Merged and closed pull requests:**

 - [CANNOLI-92] Use BioContainers Docker images with version tags by default. [\#96](https://github.com/bigdatagenomics/cannoli/pull/96) ([heuermh](https://github.com/heuermh))
 - Add hadoopConfiguration to VCFOutFormatter ctr. [\#93](https://github.com/bigdatagenomics/cannoli/pull/93) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-89] Bump ADAM dependency version to 0.23.0. [\#90](https://github.com/bigdatagenomics/cannoli/pull/90) ([heuermh](https://github.com/heuermh))
 - Fix compile error due to upstream 0.23.0-SNAPSHOT changes. [\#88](https://github.com/bigdatagenomics/cannoli/pull/88) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-83] Fix case for snpEff when using docker. [\#85](https://github.com/bigdatagenomics/cannoli/pull/85) ([heuermh](https://github.com/heuermh))
 - Replace cannoli ascii logo. [\#82](https://github.com/bigdatagenomics/cannoli/pull/82) ([heuermh](https://github.com/heuermh))
 - Add docker support to bowtie and bowtie2. [\#81](https://github.com/bigdatagenomics/cannoli/pull/81) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-76] Add --rm flag to docker run. [\#80](https://github.com/bigdatagenomics/cannoli/pull/80) ([heuermh](https://github.com/heuermh))
 - Adding code of contact adapted from the Contributor Convenant, version 1.4. [\#79](https://github.com/bigdatagenomics/cannoli/pull/79) ([heuermh](https://github.com/heuermh))
 - Adding wrapper for BCFtools norm. [\#71](https://github.com/bigdatagenomics/cannoli/pull/71) ([heuermh](https://github.com/heuermh))
 - Adding wrapper for samtools mpileup. [\#70](https://github.com/bigdatagenomics/cannoli/pull/70) ([heuermh](https://github.com/heuermh))
 - Adding wrapper for vt normalize. [\#68](https://github.com/bigdatagenomics/cannoli/pull/68) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-45] Move to multimodule Maven build, add cannoli-submit script [\#62](https://github.com/bigdatagenomics/cannoli/pull/62) ([heuermh](https://github.com/heuermh))
 - Update move_to scripts for Spark version 2.1.0. [\#61](https://github.com/bigdatagenomics/cannoli/pull/61) ([heuermh](https://github.com/heuermh))
 - Fix compile error in FragmentRDD.apply [\#60](https://github.com/bigdatagenomics/cannoli/pull/60) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-33] Use ADAM tab5 formatter for bowtie. [\#59](https://github.com/bigdatagenomics/cannoli/pull/59) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-48] Remove SequenceDictionaryReader. [\#58](https://github.com/bigdatagenomics/cannoli/pull/58) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-55] Fix compile errors due to upstream commit 0afbda96. [\#57](https://github.com/bigdatagenomics/cannoli/pull/57) ([heuermh](https://github.com/heuermh))
 - Fix compile issues with most recent ADAM version 0.23.0-SNAPSHOT. [\#56](https://github.com/bigdatagenomics/cannoli/pull/56) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-51] Rename FastqInterleaver --> InterleaveFastq [\#53](https://github.com/bigdatagenomics/cannoli/pull/53) ([heuermh](https://github.com/heuermh))
 - Code style fixes. [\#52](https://github.com/bigdatagenomics/cannoli/pull/52) ([heuermh](https://github.com/heuermh))
 - Update adam dependency version to 0.23.0-SNAPSHOT. [\#49](https://github.com/bigdatagenomics/cannoli/pull/49) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-46] Move adam-cli to provided scope. [\#47](https://github.com/bigdatagenomics/cannoli/pull/47) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-33] Use ADAM tab5 formatter for bowtie [\#42](https://github.com/bigdatagenomics/cannoli/pull/42) ([heuermh](https://github.com/heuermh))
 - Adding sample reads tool. [\#41](https://github.com/bigdatagenomics/cannoli/pull/41) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-39] Add support for exporting interleaved FASTQ as unaligned BAM. [\#40](https://github.com/bigdatagenomics/cannoli/pull/40) ([fnothaft](https://github.com/fnothaft))
 - Use --interleaved for bowtie2 [\#37](https://github.com/bigdatagenomics/cannoli/pull/37) ([heuermh](https://github.com/heuermh))
 - Add placeholder publish_scaladoc.sh script [\#31](https://github.com/bigdatagenomics/cannoli/pull/31) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-28] Allow docker command to be specified. [\#30](https://github.com/bigdatagenomics/cannoli/pull/30) ([fnothaft](https://github.com/fnothaft))
 - [CANNOLI-25] Properly support loading BWA indices via SparkFiles API. [\#26](https://github.com/bigdatagenomics/cannoli/pull/26) ([fnothaft](https://github.com/fnothaft))
 - Disable incremental mode in the Scala build. [\#24](https://github.com/bigdatagenomics/cannoli/pull/24) ([heuermh](https://github.com/heuermh))
 - Update examples in readme [\#22](https://github.com/bigdatagenomics/cannoli/pull/22) ([heuermh](https://github.com/heuermh))
 - Prep for move to bigdatagenomics org [\#19](https://github.com/bigdatagenomics/cannoli/pull/19) ([fnothaft](https://github.com/fnothaft))
 - [CANNOLI-16] Update ADAM dependency version to 0.22.0 [\#17](https://github.com/bigdatagenomics/cannoli/pull/17) ([heuermh](https://github.com/heuermh))
 - [CANNOLI-14] Add switches to force loading from Interleaved FASTQ/Parquet. [\#15](https://github.com/bigdatagenomics/cannoli/pull/15) ([fnothaft](https://github.com/fnothaft))
 - [CANNOLI-12] Add a FASTQ interleaver. [\#13](https://github.com/bigdatagenomics/cannoli/pull/13) ([fnothaft](https://github.com/fnothaft))
 - [CANNOLI-10] Attach record groups and sequence dictionary to BWA output. [\#11](https://github.com/bigdatagenomics/cannoli/pull/11) ([fnothaft](https://github.com/fnothaft))
 - [CANNOLI-7] Escape tabs in @RG line when running BWA without docker. [\#8](https://github.com/bigdatagenomics/cannoli/pull/8) ([fnothaft](https://github.com/fnothaft))
 - [CANNOLI-4] Add ability to save Fragments from BWA. [\#5](https://github.com/bigdatagenomics/cannoli/pull/5) ([fnothaft](https://github.com/fnothaft))
 - Add Docker arguments to Freebayes and SnpEff. [\#3](https://github.com/bigdatagenomics/cannoli/pull/3) ([heuermh](https://github.com/heuermh))
 - Add BWA runner. [\#2](https://github.com/bigdatagenomics/cannoli/pull/2) ([fnothaft](https://github.com/fnothaft))
 - SupportedHeaderLines renamed to DefaultHeaderLines [\#1](https://github.com/bigdatagenomics/cannoli/pull/1) ([waltermblair](https://github.com/waltermblair))
