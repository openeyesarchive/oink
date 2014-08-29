#!/usr/bin/env bash
#*******************************************************************************
# OINK - Copyright (c) 2014 OpenEyes Foundation
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#*******************************************************************************
#
#
# Restarts the OINK endpoints.
#
# Requires OINK_VERSION to be set.
# Run this script from "oink/oink-tests/oink-tests-multi-box/src".
# 

###################################
# Stop endpoints
###################################
pushd .
echo "Stopping endpoints"
cd test-workspace/vagrant
vagrant destroy --force

###################################
## Copy new build in
###################################
echo "Copying OINK distro into VFS"
cd ..
cp ../target/oink-platforms-karaf-distro-$OINK_VERSION.tar.gz vagrant/vfs

###################################
# Start endpoints
###################################
echo "Starting endpoints"
cd vagrant
vagrant up

popd
echo "Finished loading OINK VMs"

echo "COMPLETE"