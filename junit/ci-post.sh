#/bin/bash

DROP_DIR=~/webserver/drop
VERSION=`grep -m 1 "version" pom.xml | sed 's/^[^>]*>\(.*\?\)<.*$/\1/'`

# Build Javadoc archive and publish to web staging
tar cfz target/javadoc.tgz --transform="s,apidocs,helpers/junit/$VERSION/javadoc," -C target apidocs
cp target/javadoc.tgz $DROP_DIR/helpers-junit.tgz
echo "rm -r helpers/junit/$VERSION/javadoc" > $DROP_DIR/helpers-junit.tgz.action
