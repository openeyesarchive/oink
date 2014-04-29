#!/bin/bash 

# This unix script checks out OINK, builds it, and produces a tar.gz containing three files for deployment; the application, the config and a README
# Requires GIT and Maven 3


function usage {
    echo "OINK Easy Deployment Script v0.1"
    echo "Usage: ./easydeploy.sh facadeParamFile"          
    exit
}

set -e

if [ "$#" -ne 1 ]; then
    usage
fi

rm -rf build
mkdir build

# Checkout source from git
git clone -b develop git@github.com:openeyes/oink.git build/source

# Build and install
mvn -f build/source/pom.xml clean install -DskipTests=true

# Get project version
VERSION=`mvn -f build/source/pom.xml help:evaluate -Dexpression=project.version 2>/dev/null| grep  "^[0-9]" | perl -ne 'chomp and print'`

FACADE_CONFIG_PATH=$1

mkdir -p build/target

tar -xf build/source/platforms/karaf/distro/target/distro-$VERSION.tar.gz -C build/target

# Build a dummy cfg for facade example
cp $1 build/target/distro-$VERSION/etc/uk.org.openeyes.oink.facade.cfg

# Copy KAR
cp build/source/platforms/karaf/kar/target/kar-$VERSION.kar build/target/distro-$VERSION/deploy/kar-$VERSION.kar

# Build tar
cd build/target/distro-$VERSION && tar cvzf ../target.tar.gz * && cd ../../..

rm -rf build/target/distro-$VERSION

echo "-----------------------------------------------------------------------"
echo "Script Finished --- target.tar.gz can be found in build/target folder"
echo "-----------------------------------------------------------------------"