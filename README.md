# cannoli
Big Data Genomics ADAM Pipe API wrappers for bioinformatics tools.  Apache 2 licensed.

![cannoli project logo](https://github.com/heuermh/cannoli/raw/master/images/cannoli-shells.jpg)

### Hacking cannoli

Install

 * JDK 1.8 or later, http://openjdk.java.net
 * Apache Maven 3.3.9 or later, http://maven.apache.org

To build

    $ mvn install


### Running cannoli using ```adam-submit```

To run the commands in this repository via the ADAM command line, specify ```ADAM_MAIN``` and add the cannoli jar
to the classpath with the Spark ```--jars``` argument.

Note the ```--``` argument separator between Spark arguments and Cannoli command arguments.

Cannoli commands are now listed in the usage text.

```
$ ADAM_MAIN=org.bdgenomics.cannoli.Cannoli \
    adam-submit \
    --jars cannoli_2.10-0.1-SNAPSHOT.jar \
    --

Using ADAM_MAIN=org.bdgenomics.cannoli.Cannoli
Using SPARK_SUBMIT=/usr/local/bin/spark-submit

       e         888~-_          e             e    e
      d8b        888   \        d8b           d8b  d8b
     /Y88b       888    |      /Y88b         d888bdY88b
    /  Y88b      888    |     /  Y88b       / Y88Y Y888b
   /____Y88b     888   /     /____Y88b     /   YY   Y888b
  /      Y88b    888_-~     /      Y88b   /          Y888b

Usage: adam-submit [<spark-args> --] <adam-args>

Choose one of the following commands:

...

CANNOLI
            bedtools : ADAM Pipe API wrapper for Bedtools intersect.
              bowtie : ADAM Pipe API wrapper for Bowtie.
             bowtie2 : ADAM Pipe API wrapper for Bowtie2.
                 bwa : ADAM Pipe API wrapper for BWA.
           freebayes : ADAM Pipe API wrapper for Freebayes.
              snpEff : ADAM Pipe API wrapper for SnpEff.
```

Command arguments follow the ```--``` separator and command name.

```
$ ADAM_MAIN=org.bdgenomics.cannoli.Cannoli \
    adam-submit \
    --jars cannoli_2.10-0.1-SNAPSHOT.jar \
    -- \
    bwa --help

Using ADAM_MAIN=org.bdgenomics.cannoli.Cannoli
Using SPARK_SUBMIT=/usr/local/bin/spark-submit

 INPUT                        : Location to pipe from, in interleaved FASTQ format.
 OUTPUT                       : Location to pipe to.
 SAMPLE                       : Sample ID.
 -bwa_path VAL                : Path to the BWA executable. Defaults to bwa.
 -defer_merging               : Defers merging single file output.
 -docker_image VAL            : Docker image to use. Defaults to quay.io/ucsc_cgl/bwa:0.7.12--256539928ea162949d8a65ca5c79a72ef557ce7c.
 -h (-help, --help, -?)       : Print help
 -index VAL                   : Path to the bwa index to be searched, e.g. <ebwt> in bwa [options]* <ebwt> ...
 -parquet_block_size N        : Parquet block size (default = 128mb)
 -parquet_compression_codec   : Parquet compression codec
 -parquet_disable_dictionary  : Disable dictionary encoding
 -parquet_logging_level VAL   : Parquet logging level (default = severe)
 -parquet_page_size N         : Parquet page size (default = 1mb)
 -print_metrics               : Print metrics to the log on completion
 -single                      : Saves OUTPUT as single file.
 -use_docker                  : If true, uses Docker to launch BWA. If false, uses the BWA executable path.
```
