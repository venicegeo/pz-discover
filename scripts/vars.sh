#!/bin/bash

APP=pz-discover
EXT=jar
SHA=$(git rev-parse HEAD)
SHORT=$(git rev-parse --short HEAD)

ARTIFACT="$SHA.$EXT"
REPOSITORY="Piazza"
NEXUSURL=https://nexus.devops.geointservices.io/content/repositories/$REPOSITORY

DOMAIN="cf.piazzageo.io"
