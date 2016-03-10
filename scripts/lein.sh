#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

if ! type lein >/dev/null 2>&1; then
  curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > $root/scripts/lein
  chmod 700 $root/scripts/lein
fi
