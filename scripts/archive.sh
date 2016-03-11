#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

export PATH=$PATH:$root/scripts

! type lein >/dev/null 2>&1 && source $root/scripts/lein.sh

# gather some data about the repo
source $root/scripts/vars.sh

# the path where the artifact is
src=$root/target/$APP-*-standalone.$EXT

# this step builds our artifact
[ -f $src ] || lein do clean, uberjar

mv $src $ARTIFACT

# pom?
[ -f $root/pom.xml ] && genpom=false || genpom=false

# push artifact to nexus
mvn deploy:deploy-file \
  -Durl=$NEXUSURL \
  -DrepositoryId=nexus \
  -Dfile=$ARTIFACT \
  -DgeneratePom=$genpom \
  -DgroupId=io.piazzageo \
  -DartifactId=$APP \
  -Dversion=$VERSION \
  -Dpackaging=$EXT
