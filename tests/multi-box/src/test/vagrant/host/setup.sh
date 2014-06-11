#!/bin/bash
#
#
# Starts three virtual machines needed for tests.
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
git checkout origin/develop

# Rename OpenEyes to workspace to spoof Vagrant setup for OE
cd ..
mv OpenEyes workspace

cd workspace
vagrant destroy --force

# Start OpenEyes and prepare box
vagrant up
./bin/prep.sh

# Allow admin user access to API
SQL_STATEMENT="insert into authassignment (itemname, userid) values ('API access', 1);"
vagrant ssh -c "/usr/bin/mysql -u openeyes -poe_test openeyes -e \"$SQL_STATEMENT\""
echo "Finished loading OpenEyes VM"
popd

####################################
## Set up and start the two Oink VMs
####################################
echo "Starting OINK VMs"
pushd .

mkdir -p vagrant
cp -R ../src/test/vagrant/** vagrant

mkdir -p vagrant/vfs ; cp ../target/distro-0.3-SNAPSHOT.tar.gz vagrant/vfs
cd vagrant

vagrant up

popd
echo "Finished loading OINK VMs"

popd

echo "COMPLETE"