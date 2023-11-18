#!/bin/sh

# Modify the path to saxon in the following:
SAXON_JAR=~/Downloads/SaxonHE11-4J/saxon-he-11.4.jar

if [ $# -lt 2 ]
then
  echo "Usage: $0 <output-file> <input-directory-tree>"
  exit 1
fi

java -classpath "$SAXON_JAR" net.sf.saxon.Transform -xsl:harvest_trainingdata.xsl -s:harvest_trainingdata.xsl -o:"$1" input-dir="$2"