#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

if ! type lein >/dev/null 2>&1; then
  wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
  chmod 700 $root/lein
  alias lein=$root/lein
fi
