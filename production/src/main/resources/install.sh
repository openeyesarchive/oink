#!/bin/bash

# Usage:
#         install <TARGET_BASE_DIRECTORY> <SETTINGS_FILE>

set -e 

OINK_VERSION=0.4-SNAPSHOT
TARGET_BASE_DIRECTORY=$1
SETTINGS_FILE=$2

# Copy OINK distro to destination
sudo cp oink-$OINK_VERSION.tar.gz $TARGET_BASE_DIRECTORY
pushd .

	cd $TARGET_BASE_DIRECTORY
	
	sudo rm -f oink
	sudo mv oink-$OINK_VERSION oink-$OINK_VERSION-$(date -d "today" +"%Y%m%d%H%M")

	sudo tar -zxf oink-$OINK_VERSION.tar.gz
	sudo mv distro-$OINK_VERSION oink-$OINK_VERSION
	sudo ln -s oink-$OINK_VERSION oink
	sudo chown `whoami` oink
	sudo chown `whoami` oink-$OINK_VERSION
	
popd

pushd .

# Tear down anything from before
sudo rm -Rf $TARGET_BASE_DIRECTORY/oink-staging/

# Build staging directory
sudo mkdir -p $TARGET_BASE_DIRECTORY/oink-staging
sudo chown `whoami` $TARGET_BASE_DIRECTORY/oink-staging
cp -R bin $TARGET_BASE_DIRECTORY/oink-staging
cp -R templates $TARGET_BASE_DIRECTORY/oink-staging

	cd $TARGET_BASE_DIRECTORY/oink-staging/templates
	sudo chmod a+x ../bin/replace_values.sh
	../bin/replace_values.sh $SETTINGS_FILE $TARGET_BASE_DIRECTORY/oink/settings
	mv $TARGET_BASE_DIRECTORY/oink/settings/*.sh $TARGET_BASE_DIRECTORY/oink-staging/bin
	sudo chmod a+x $TARGET_BASE_DIRECTORY/oink-staging/bin/*.sh
	
popd

echo ""
echo ""
echo "-------------------------------------------------"
echo "Build scripts are located in: "
echo "-------------------------------------------------"
ls -d -1 /opt/oink-staging/bin/build*
echo "-------------------------------------------------"

echo ""
echo "DONE"
echo ""
