#!/bin/bash
#
#
# Starts three virtual machines needed for tests.
# 

set -e

###################################
# Set up test working directory
###################################

mkdir -p test-workspace
pushd test-workspace

###################################
## Set up and start the OpenEyes VM
###################################
pushd

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
vagrant up
cd bin
./prep.sh

# Allow admin user access to API

SQL_STATEMENT = "insert into authassignment (itemname, userid) values ('API access', 1);"
vagrant ssh -c "/usr/bin/mysql mysql -u openeyes -poe_test openeyes -e \"$SQL_STATEMENT\""

popd

####################################
## Set up and start the two Oink VMs
####################################
mkdir -p vagrant
cp -R ../src/test/vagrant/** vagrant

mkdir -p vagrant/vfs ; cp ../target/distro-0.3-SNAPSHOT.tar.gz vagrant/vfs
pushd vagrant

vagrant up endpoint1

vagrant up endpoint2

popd

popd