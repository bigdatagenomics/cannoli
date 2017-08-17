#!/bin/bash

set +x

# save full filepath that is piped in
filePath=`cat`
# save just the filename
fileName=`basename ${filePath}`
# save location where all MACS2 output will be saved
outputPath=$1

# run macs2 callpeak function
# -t is input file, -n is prefix for output filenames, --outdir is location where files saved
macs2 callpeak -t ${filePath} -n ${fileName} --outdir ${outputPath}/${fileName}_output

# pipe out BED file entries from MACS2 output file
echo ${outputPath}/${fileName}_output/${fileName}_summits.bed
