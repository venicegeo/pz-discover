#!/bin/bash

APP=pz-discover
EXT=jar
VERSION=$(git rev-parse --short HEAD)
ARTIFACT=$APP.$VERSION.$EXT
NEXUSURL=https://nexus.devops.geointservices.io/content/repositories/Piazza
