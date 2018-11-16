# Cannoli
Big Data Genomics ADAM Pipe API wrappers for bioinformatics tools.  Apache 2 licensed.

[![Build Status](https://img.shields.io/jenkins/s/https/amplab.cs.berkeley.edu/jenkins/view/Big%20Data%20Genomics/job/cannoli.svg)](https://amplab.cs.berkeley.edu/jenkins/view/Big%20Data%20Genomics/job/cannoli/)
[![Maven Central](https://img.shields.io/maven-central/v/org.bdgenomics.cannoli/cannoli-parent-spark2_2.11.svg?maxAge=600)](http://search.maven.org/#search%7Cga%7C1%7Corg.bdgenomics.cannoli)
[![API Documentation](http://javadoc.io/badge/org.bdgenomics.cannoli/cannoli-cli-spark2_2.11.svg?color=brightgreen&label=scaladoc)](http://javadoc.io/doc/org.bdgenomics.cannoli/cannoli-core-spark2_2.11)

![Cannoli project logo](https://github.com/heuermh/cannoli/raw/master/images/cannoli-shells.jpg)

### Hacking Cannoli

Install

 * JDK 1.8 or later, http://openjdk.java.net
 * Apache Maven 3.3.9 or later, http://maven.apache.org

To build

    $ mvn install


### Running Cannoli from the command line

To run the commands in this repository via the command line, use `cannoli-submit`.

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
        bcftoolsNorm : ADAM Pipe API wrapper for BCFtools norm.
   bedtoolsIntersect : ADAM Pipe API wrapper for Bedtools intersect.
              bowtie : ADAM Pipe API wrapper for Bowtie.
             bowtie2 : ADAM Pipe API wrapper for Bowtie 2.
                 bwa : ADAM Pipe API wrapper for BWA.
           freebayes : ADAM Pipe API wrapper for Freebayes.
            minimap2 : ADAM Pipe API wrapper for Minimap2.
     samtoolsMpileup : ADAM Pipe API wrapper for samtools mpileup.
              snpEff : ADAM Pipe API wrapper for SnpEff.
                 vep : ADAM Pipe API wrapper for Ensembl VEP.
         vtNormalize : ADAM Pipe API wrapper for vt normalize.

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
    -add_indices
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
    -add_indices
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
    -add_indices
```


### Using Cannoli interactively from the shell

To run the Cannoli interactive shell, based on the ADAM shell, which in turn extends the
Apache Spark shell, use `cannoli-shell`.

Wildcard import from `ADAMContext` to add implicit methods to SparkContext for loading reads,
alignments, variants, genotypes, and features, such as `sc.loadPairedFastqAsFragments` below.

Wildcard import from `Cannoli` to add implicit methods for calling external commands to the
genomic datasets loaded by ADAM, such as `reads.alignWithBwa` below.

```
$ ./bin/cannoli-shell

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
