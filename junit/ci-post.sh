#/bin/bash

if [ ! -d "$WEBSERVER_DROPDIR" ]; then
  WEBSERVER_DROPDIR=~/webserver/drop
  if [ ! -d "$WEBSERVER_DROPDIR" ]; then
    echo WEBSERVER_DROPDIR: $WEBSERVER_DROPDIR
    echo Directory is not valid
    exit 1
  fi
fi

VERSION=`grep -m 1 "version" pom.xml | sed 's/^[^>]*>\(.*\?\)<.*$/\1/'`

# Build Javadoc archive and publish to web staging
tar cfz target/javadoc.tgz --transform="s,apidocs,helpers/junit/$VERSION/javadoc," -C target apidocs
cp target/javadoc.tgz $WEBSERVER_DROPDIR/helpers-junit.tgz
echo "rm -r helpers/junit/$VERSION/javadoc" > $WEBSERVER_DROPDIR/helpers-junit.tgz.action
