#!/bin/bash -ex

lein do clean, with-profile -user deps :tree
