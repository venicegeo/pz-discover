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
  -Dartifact=io.piazzageo:$APP:$SHORT:$EXT \
  -DgroupId=io.piazzageo \
  -DartifactId=$APP \
  -Dversion=$SHORT \
  -Dtype=$EXT \
  -DoverWrite=true \
  -DoutputDirectory=$root \
  -DdestFileName=app.$EXT
