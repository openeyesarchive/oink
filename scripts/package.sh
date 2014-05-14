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