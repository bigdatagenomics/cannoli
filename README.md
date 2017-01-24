# cannoli
Big Data Genomics ADAM Pipe API wrappers for bioinformatics tools.  Apache 2 licensed.

###Hacking cannoli

Install

 * JDK 1.8 or later, http://openjdk.java.net
 * Apache Maven 3.3.9 or later, http://maven.apache.org

To build

    $ mvn install


###Running cannoli using ```adam-submit```

To run the commands in this repository via the ADAM command line, specify ```ADAM_MAIN``` and add the cannoli jar
to the classpath with the Spark ```--jars``` argument.

Note the ```--``` argument separator between Spark arguments and ADAM arguments.

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
              bowtie : ADAM Pipe API wrapper for Bowtie.
             bowtie2 : ADAM Pipe API wrapper for Bowtie2.
             example : Example.
           freebayes : ADAM Pipe API wrapper for Freebayes.
              snpEff : ADAM Pipe API wrapper for SnpEff.
```
