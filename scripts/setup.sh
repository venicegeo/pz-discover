#!/bin/bash -ex

pushd `dirname $0/..` > /dev/null
root=$(pwd -P)
popd > /dev/null

wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
chmod 700 $root/lein

$root/lein do clean, with-profile -user deps :tree
