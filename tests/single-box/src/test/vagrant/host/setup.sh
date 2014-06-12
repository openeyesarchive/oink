#!/usr/bin/env bash
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