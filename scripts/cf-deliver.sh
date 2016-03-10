#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

# gather some data about the repo
source $root/scripts/vars.sh

[ -f $root/app.$EXT ] && exit

mvn dependency:get \
  -Dartifact=core:$APP:$SHORT:$EXT \
  -DremoteRepositories=$NEXUSURL

mvn dependency:copy \
    -DgroupId=core \
    -DartifactId=$APP \
    -Dversion=$SHORT \
    -Dtype=$EXT \
    -DoverWrite=true \
    -DoutputDirectory=$root \
    -DdestFileName=app.$EXT
