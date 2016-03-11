#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

# gather some data about the repo
source $root/ci/vars.sh

[ -f $root/$APP.$EXT ] && exit

mvn dependency:get \
  -DremoteRepositories=nexus::default::$NEXUSURL \
  -DrepositoryId=nexus \
  -DartifactId=$APP \
  -DgroupId=io.piazzageo \
  -Dpackaging=$EXT \
  -Dtransitive=false \
  -Dversion=$VERSION

mvn dependency:copy \
  -Dartifact=io.piazzageo:$APP:$VERSION:$EXT \
  -DstripVersion=true \
  -DoverWriteIfNewer=true \
  -DoutputDirectory=$root

mv $root/$ARTIFACT $root/$APP.$EXT
