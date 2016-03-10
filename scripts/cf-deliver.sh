#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

# gather some data about the repo
source $root/scripts/vars.sh

[ -f $root/app.$EXT ] && exit

mvn dependency:get \
  -DremoteRepositories=nexus::default::$NEXUSURL \
  -DrepositoryId=nexus \
  -DartifactId=$APP \
  -DgroupId=io.piazzageo \
  -Dpackaging=$EXT \
  -Dtransitive=false \
  -Dversion=$SHORT

mvn dependency:copy \
  -Dartifact.groupId=io.piazzageo \
  -Dartifact.artifactId=$APP \
  -Dartifact.version=$SHORT \
  -Dartifact.type=$EXT \
  -Dartifact.overWrite=true \
  -Dartifact.outputDirectory=$root \
  -Dartifact.destFileName=app.$EXT
