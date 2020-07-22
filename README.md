# Cannoli
Distributed execution of bioinformatics tools on Apache Spark. Apache 2 licensed.

[![Maven Central](https://img.shields.io/maven-central/v/org.bdgenomics.cannoli/cannoli-parent-spark2_2.11.svg?maxAge=600)](http://search.maven.org/#search%7Cga%7C1%7Corg.bdgenomics.cannoli)
[![API Documentation](http://javadoc.io/badge/org.bdgenomics.cannoli/cannoli-cli-spark2_2.11.svg?color=brightgreen&label=scaladoc)](http://javadoc.io/doc/org.bdgenomics.cannoli/cannoli-core-spark2_2.11)

![Cannoli project logo](https://github.com/heuermh/cannoli/raw/master/images/cannoli-shells.jpg)


## Hacking Cannoli

Install

 * JDK 1.8 or later, https://openjdk.java.net
 * Apache Maven 3.3.9 or later, https://maven.apache.org

To build

```bash
$ mvn install
```

## Installing Cannoli

Cannoli is available in Conda via Bioconda, https://bioconda.github.io/

```bash
$ conda install cannoli
```

Cannoli is available in Homebrew via Brewsci/bio, https://github.com/brewsci/homebrew-bio

```bash
$ brew install brewsci/bio/cannoli
```

Cannoli is available in Docker via BioContainers, https://biocontainers.pro

```bash
$ docker pull quay.io/biocontainers/cannoli:{tag}
```

Find `{tag}` on the tag search page, https://quay.io/repository/biocontainers/cannoli?tab=tags


## Using Cannoli interactively from the shell

To run the Cannoli interactive shell, based on the ADAM shell, which in turn extends the
Apache Spark shell, use `cannoli-shell`.

Wildcard import from `ADAMContext` to add implicit methods to SparkContext for loading
alignments, features, fragments, genotypes, reads, sequences, slices, variant contexts,
or variants, such as `sc.loadPairedFastqAsFragments` below.

Wildcard import from `Cannoli` to add implicit methods for calling external commands to the
genomic datasets loaded by ADAM, such as `reads.alignWithBwa` below.

```
$ ./bin/cannoli-shell \
    <spark-args>

scala> import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMContext._

scala> import org.bdgenomics.cannoli.Cannoli._
import org.bdgenomics.cannoli.Cannoli._

scala> import org.bdgenomics.cannoli.BwaArgs
import org.bdgenomics.cannoli.BwaArgs

scala> val args = new BwaArgs()
args: org.bdgenomics.cannoli.BwaArgs = org.bdgenomics.cannoli.BwaArgs@54234569

scala> args.indexPath = "hg38.fa"
args.indexPath: String = hg38.fa

scala> args.sample = "sample"
args.sample: String = sample

scala> val reads = sc.loadPairedFastqAsFragments("sample1.fq", "sample2.fq")
reads: org.bdgenomics.adam.rdd.fragment.FragmentRDD = RDDBoundFragmentRDD with 0 reference
sequences, 0 read groups, and 0 processing steps

scala> val alignments = reads.alignWithBwa(args)
alignments: org.bdgenomics.adam.rdd.read.AlignmentRecordRDD = RDDBoundAlignmentRecordRDD with
0 reference sequences, 0 read groups, and 0 processing steps

scala> alignments.saveAsParquet("sample.alignments.adam")
```


## Running Cannoli from the command line

To run Cannoli commands from the command line, use `cannoli-submit`.

Note the ```--``` argument separator between Spark arguments and Cannoli command arguments.

```
$ ./bin/cannoli-submit --help

                              _ _ 
                             | (_)
   ___ __ _ _ __  _ __   ___ | |_ 
  / __/ _` | '_ \| '_ \ / _ \| | |
 | (_| (_| | | | | | | | (_) | | |
  \___\__,_|_| |_|_| |_|\___/|_|_|

Usage: cannoli-submit [<spark-args> --] <cannoli-args>

Choose one of the following commands:

CANNOLI
        bcftoolsCall : Call variant contexts with bcftools call.
     bcftoolsMpileup : Call variants from an alignment dataset with bcftools mpileup.
        bcftoolsNorm : Normalize variant contexts with bcftools norm.
   bedtoolsIntersect : Intersect the features in a feature dataset with Bedtools intersect.
              blastn : Align DNA sequences in a sequence dataset with blastn.
              bowtie : Align paired-end reads in a fragment dataset with Bowtie.
             bowtie2 : Align paired-end reads in a fragment dataset with Bowtie 2.
    singleEndBowtie2 : Align unaligned single-end reads in an alignment dataset with Bowtie 2.
                 bwa : Align paired-end reads in a fragment dataset with BWA.
             bwaMem2 : Align paired-end reads in a fragment dataset with Bwa-mem2.
           freebayes : Call variants from an alignment dataset with Freebayes.
                 gem : Align paired-end reads in a fragment dataset with GEM-Mapper.
          magicBlast : Align paired-end reads in a fragment dataset with Magic-BLAST.
            minimap2 : Align paired-end reads in a fragment dataset with Minimap2.
     samtoolsMpileup : Call variants from an alignment dataset with samtools mpileup.
                snap : Align paired-end reads in a fragment dataset with SNAP.
              snpEff : Annotate variant contexts with SnpEff.
                star : Align paired-end reads in a fragment dataset with STAR-Mapper.
       singleEndStar : Align unaligned single-end reads in an alignment dataset with STAR-Mapper.
                 vep : Annotate variant contexts with Ensembl VEP.
         vtNormalize : Normalize variant contexts with vt normalize.

CANNOLI TOOLS
     interleaveFastq : Interleaves two FASTQ files.
         sampleReads : Sample reads from interleaved FASTQ format.
```


External commands wrapped by Cannoli should be installed to each executor node in the cluster

```
$ ./bin/cannoli-submit \
    <spark-args>
    -- \
    bwa \
    sample.unaligned.fragments.adam \
    sample.bwa.hg38.alignments.adam \
    sample \
    -index hg38.fa \
    -sequence_dictionary hg38.dict \
    -fragments \
    -add_files
```

or can be run using Docker

```
$ ./bin/cannoli-submit \
    <spark-args>
    -- \
    bwa \
    sample.unaligned.fragments.adam \
    sample.bwa.hg38.alignments.adam \
    sample \
    -index hg38.fa \
    -sequence_dictionary hg38.dict \
    -fragments \
    -use_docker \
    -image quay.io/ucsc_cgl/bwa:0.7.12--256539928ea162949d8a65ca5c79a72ef557ce7c \
    -add_files
```

or can be run using Singularity

```
$ ./bin/cannoli-submit \
    <spark-args>
    -- \
    bwa \
    sample.unaligned.fragments.adam \
    sample.bwa.hg38.alignments.adam \
    sample \
    -index hg38.fa \
    -sequence_dictionary hg38.dict \
    -fragments \
    -use_singularity \
    -image quay.io/ucsc_cgl/bwa:0.7.12--256539928ea162949d8a65ca5c79a72ef557ce7c \
    -add_files
```
