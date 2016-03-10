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
  -DartifactItems.artifactItem.groupId=io.piazzageo \
  -DartifactItems.artifactItem.artifactId=$APP \
  -DartifactItems.artifactItem.version=$SHORT \
  -DartifactItems.artifactItem.type=$EXT \
  -DartifactItems.artifactItem.overWrite=true \
  -DartifactItems.artifactItem.outputDirectory=$root \
  -DartifactItems.artifactItem.destFileName=app.$EXT
