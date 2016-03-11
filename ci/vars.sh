#!/bin/bash

APP=pz-discover
EXT=jar
VERSION=$(git rev-parse --short HEAD)
ARTIFACT=$APP-$VERSION.$EXT
