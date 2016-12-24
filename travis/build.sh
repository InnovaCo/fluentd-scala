#!/bin/bash
set -ex

if [[ "$TRAVIS_PULL_REQUEST" == "false" && "$TRAVIS_BRANCH" == "master" ]]; then
  echo "$oss_gpg_passphrase" | gpg --passphrase-fd 0 ./travis/gpg-public.asc.gpg
  echo "$oss_gpg_passphrase" | gpg --passphrase-fd 0 ./travis/gpg-private.asc.gpg

  sbt ';set version <<= (version)(_ + ".'${TRAVIS_BUILD_NUMBER:-0}'")' +test +publishSigned sonatypeReleaseAll
fi