#!/bin/bash -ex

pushd `dirname $0` > /dev/null
base=$(pwd -P)
popd > /dev/null

# this step builds our artifact
lein do clean, uberjar

# the path where the artifact is
jarfile=$base/../target/uberjar/$APP-0.1.0-standalone.$EXT

# gather some data about the repo
source $base/vars.sh

# do we have this artifact in s3? If not, upload it.
aws s3 ls $S3URL || aws s3 cp $jarfile $S3URL

# success if we have an artifact stored in s3.
aws s3 ls $S3URL
