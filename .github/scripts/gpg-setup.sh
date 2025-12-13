#!/usr/bin/env bash
set -e

# Create .gnupg directory with proper permissions
mkdir -p ~/.gnupg
chmod 700 ~/.gnupg

# Configure GPG for non-interactive use
echo "use-agent" >> ~/.gnupg/gpg.conf
echo "pinentry-mode loopback" >> ~/.gnupg/gpg.conf
echo "allow-loopback-pinentry" >> ~/.gnupg/gpg-agent.conf

# Set proper permissions on GPG config files
chmod 600 ~/.gnupg/gpg.conf ~/.gnupg/gpg-agent.conf

# Reload the GPG agent
echo RELOADAGENT | gpg-connect-agent

# Import the PGP secret key
echo "$PGP_SECRET" | base64 --decode | gpg --import --no-tty --batch --yes
