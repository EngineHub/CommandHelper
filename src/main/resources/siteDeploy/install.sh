#!/bin/bash

# This script is meant to install all the components needed to install the methodscript.com site
# on a brand new server. There are a few assumptions that are made, if this script is to be run without
# problems.
# 1. This will be installed on a fresh server in the AWS cloud
# 2. This script will be run as root.
# 3. Nothing else is present on the server, and only ephemeral data will be stored on this server
# If any of these assumptions are incorrect, then the "install" option must not be specified in the deploy
# command

# Don't break stuff
set -e

# Update the server
apt-get update
apt-get upgrade

# Install apache
apt-get install apache2

