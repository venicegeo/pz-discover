#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

export PATH=$PATH:$root/scripts

! type lein >/dev/null 2>&1 && source $root/scripts/lein.sh

# gather some data about the repo
source $root/scripts/vars.sh

# the path where the artifact is
srcfile=$root/target/$APP-*-standalone.$EXT
file=$APP.$SHA.$EXT

# this step builds our artifact
[ -f $jarfile ] || lein do clean, uberjar

mv $jarfile $file

# pom?
[ -f $root/pom.xml ] && genpom=false || genpom=false

# push artifact to nexus
mvn deploy:deploy-file \
  -Durl=$NEXUSURL \
  -DrepositoryId=$REPOSITORY \
  -Dfile=$file \
  -DgeneratePom=$genpom
  -DgroupId=core \
  -DartifactId=$APP \
  -Dversion=$SHA \
  -Dpackaging=$EXT
