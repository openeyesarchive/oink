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
# Starts OpenEyes vm and a vm for the rabbit broker
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

rm -Rf OpenEyes
rm -Rf workspace
git clone git@github.com:openeyes/OpenEyes.git
cd OpenEyes
git checkout origin/develop

# Rename OpenEyes to workspace to spoof Vagrant setup for OE
cd ..
mv OpenEyes workspace

cd workspace
vagrant destroy --force

# Start OpenEyes and prepare box

export OE_VAGRANT_MODE='bdd'
export OE_VAGRANT_IP=10.0.115.3
vagrant up
sed -i -e '1s@.*@#!/usr/bin/env bash@g' bin/prep.sh
./bin/prep.sh

# Allow admin user access to API
SQL_STATEMENT="insert into authassignment (itemname, userid) values ('API access', 1);"
vagrant ssh -c "/usr/bin/mysql -u openeyes -poe_test openeyes -e \"$SQL_STATEMENT\""
echo "Finished loading OpenEyes VM"
popd

####################################
## Set up and start the RabbitMQ
####################################
echo "Starting RabbitMQ VMs"
pushd .

mkdir -p rabbitvm
cp -R ../src/test/vagrant/** rabbitvm

cd rabbitvm

vagrant up

popd
echo "Finished loading RabbitMQ VMs"

popd

echo "COMPLETE"