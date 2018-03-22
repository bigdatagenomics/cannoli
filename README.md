# cannoli
Big Data Genomics ADAM Pipe API wrappers for bioinformatics tools.  Apache 2 licensed.

![cannoli project logo](https://github.com/heuermh/cannoli/raw/master/images/cannoli-shells.jpg)

### Hacking cannoli

Install

 * JDK 1.8 or later, http://openjdk.java.net
 * Apache Maven 3.3.9 or later, http://maven.apache.org

To build

    $ mvn install


### Running cannoli

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
     samtoolsMpileup : ADAM Pipe API wrapper for samtools mpileup.
              snpEff : ADAM Pipe API wrapper for SnpEff.
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
