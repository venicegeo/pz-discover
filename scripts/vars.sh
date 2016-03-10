#!/bin/bash

APP=pz-discover
EXT=jar
SHORT=$(git rev-parse --short HEAD)
ARTIFACT=$APP.$SHORT.$EXT
NEXUSURL=https://nexus.devops.geointservices.io/content/repositories/Piazza
