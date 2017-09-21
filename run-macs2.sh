

#!/bin/bash

set +x

tee datafile > /dev/null
rm -r -f NA_summits.bed
/home/eecs/gunjan/envs/gunjan/bin/macs2 callpeak -t datafile --verbose 0
#fileName=HELLO
#awk -v file="$fileName" 'BEGIN{OFS="\t"}{$4=file}1' NA_summits.bed
cat NA_summits.bed

#tee hi.txt > /dev/null
#cp /home/eecs/gunjan/all-adam/cannoli/test.bed gunjan.bed
#cat gunjan.bed
#/home/eecs/gunjan/envs/gunjan/bin/macs2 callpeak -t test.bed -n HI

#cat /home/eecs/gunjan/all-adam/cannoli/test.bed
#cat HI_summits.bed
