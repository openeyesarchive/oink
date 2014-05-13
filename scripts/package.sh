#!/bin/bash 

# This unix script checks out OINK, builds it, and produces a tar.gz containing three files for deployment; the application, the config and a README
# Requires GIT and Maven 3

rm -rf build
mkdir build

# Checkout source from git
git clone -b develop git@github.com:openeyes/oink.git build/source

# Build and install
mvn -f build/source/pom.xml clean install -DskipTests=true

# Get project version
VERSION=`mvn -f build/source/pom.xml help:evaluate -Dexpression=project.version 2>/dev/null| grep  "^[0-9]" | perl -ne 'chomp and print'`

mkdir build/archive

# Build a readme with instructions
echo "README HERE" > build/archive/README

# Build a dummy cfg for facade example
cp build/source/adapters/oink-adapter-facade/src/main/resources/facade.properties build/archive/uk.org.openeyes.oink.facade.cfg

# Copy KAR
cp build/source/platforms/karaf/kar/target/kar-$VERSION.kar build/archive/kar-$VERSION.kar

# Build tar
cd build/archive && tar cvzf ../../archive.tar.gz * && cd ../..