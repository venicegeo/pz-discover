#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

export PATH=$PATH:$root/scripts

! type lein >/dev/null 2>&1 && source $root/scripts/lein.sh

lein do clean, test
