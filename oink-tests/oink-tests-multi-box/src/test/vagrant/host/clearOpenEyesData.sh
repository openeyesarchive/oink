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
# Drops and re-initialises the OpenEyes database
#
# Run this script from "oink/oink-tests/oink-tests-multi-box/".
# 

pushd .
cd test-workspace/workspace

echo "Clearing database..."
vagrant ssh -c "/vagrant/bin/load-testdata.sh"

echo "Enabling API access..."
SQL_STATEMENT="insert into authassignment (itemname, userid) values ('API access', 1);"
vagrant ssh -c "/usr/bin/mysql -u openeyes -poe_test openeyes -e \"$SQL_STATEMENT\""

popd
echo "COMPLETE"