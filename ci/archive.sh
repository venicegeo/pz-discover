#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

export PATH=$PATH:$root/ci

! type lein >/dev/null 2>&1 && source $root/ci/lein.sh

# gather some data about the repo
source $root/ci/vars.sh

# the path where the artifact is
src=$root/target/$APP-*-standalone.$EXT

# this step builds our artifact
[ -f $src ] || lein do clean, uberjar

mv $src $ARTIFACT
