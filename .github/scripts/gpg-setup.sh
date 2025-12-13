#!/usr/bin/env bash
set -e

# Import the PGP secret key
echo "$PGP_SECRET" | base64 --decode | gpg --import --no-tty --batch --yes
