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
# Starts three virtual machines needed for tests.
# Requires OINK_VERSION to be set.
# 

###################################
# Set up test working directory
###################################
pushd .
echo "Starting OpenEyes VM"
mkdir -p test-workspace
cd test-workspace

###################################
## Set up and start the OpenEyes VM
###################################
pushd .

OE_VAGRANT_MODE='bdd'
export OE_VAGRANT_MODE

rm -Rf OpenEyes
rm -Rf workspace
git clone git@github.com:openeyes/OpenEyes.git
cd OpenEyes
#git checkout origin/develop
git checkout feature/salisbury

# Rename OpenEyes to workspace to spoof Vagrant setup for OE
cd ..
mv OpenEyes workspace

# Copy OE config settings
mkdir openeyes-config
cp -R ../src/test/openeyes-config/** openeyes-config

cd workspace
vagrant destroy --force

# Start OpenEyes and prepare box
vagrant up
sed -i '1s@.*@#!/usr/bin/env bash@g' bin/prep.sh
./bin/prep.sh

# Allow admin user access to API
SQL_STATEMENT="insert into authassignment (itemname, userid) values ('API access', 1);"
vagrant ssh -c "/usr/bin/mysql -u openeyes -poe_test openeyes -e \"$SQL_STATEMENT\""

# Set FHIR settings
cd ../openeyes-config
sed -e "/'specialty_codes' => array(130, 'SUP')/ r fhirsettings.php"  ../workspace/protected/config/local/common.php
cd ..

echo "Finished loading OpenEyes VM"
popd


####################################
## Set up and start the two Oink VMs
####################################
echo "Starting OINK VMs"
pushd .

mkdir -p vagrant
cp -R ../src/test/vagrant/** vagrant

mkdir -p vagrant/vfs ; cp ../target/oink-platforms-karaf-distro-$OINK_VERSION.tar.gz vagrant/vfs
cd vagrant

vagrant up

popd
echo "Finished loading OINK VMs"

popd

echo "COMPLETE"