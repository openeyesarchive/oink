#!/bin/bash
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

# Delete RabbitMQ guest
sudo rabbitmqctl delete_user guest

# Configure RabbitMQ oinkadmin
sudo rabbitmqctl add_user #OINK_RABBIT_ADMIN_USERNAME# #OINK_RABBIT_ADMIN_PASSWORD#
sudo rabbitmqctl set_permissions -p / #OINK_RABBIT_ADMIN_USERNAME# ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags #OINK_RABBIT_ADMIN_USERNAME# administrator

# Configure RabbitMQ endpoint
sudo rabbitmqctl add_user #OINK_RABBIT_USERNAME# #OINK_RABBIT_PASSWORD#
sudo rabbitmqctl set_permissions -p / #OINK_RABBIT_USERNAME# ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags #OINK_RABBIT_USERNAME# management

# Setup RabbitMQ dynamic shovels
sudo rabbitmq-plugins enable rabbitmq_management
sudo rabbitmq-plugins enable rabbitmq_shovel
sudo rabbitmq-plugins enable rabbitmq_shovel_management
sudo service rabbitmq-server restart

sudo rabbitmqctl set_parameter shovel "oink_pas_hl7v2_in_shovel" '{"src-uri": "amqp://#OINK_RABBIT_USERNAME#:#OINK_RABBIT_PASSWORD#@#OINK_RABBIT_HOST#:#OINK_RABBIT_PORT#", "src-exchange": "#OINK_RABBIT_DEFAULT_EXCHANGE#", "src-exchange-key": "pas.hl7v2.in", "dest-uri": "amqp://#OINK_RABBIT_USERNAME_DEST#:#OINK_RABBIT_PASSWORD_DEST#@#OINK_RABBIT_HOST_DEST#:#OINK_RABBIT_PORT_DEST#", "dest-exchange": "#OINK_RABBIT_DEFAULT_EXCHANGE_DEST#"}'

sudo rabbitmqctl set_parameter shovel "oink_hl7v2_response_out_shovel" '{"src-uri": "amqp://#OINK_RABBIT_USERNAME#:#OINK_RABBIT_PASSWORD#@#OINK_RABBIT_HOST#:#OINK_RABBIT_PORT#", "src-exchange": "#OINK_RABBIT_DEFAULT_EXCHANGE#", "src-exchange-key": "pas.hl7v2.response", "dest-uri": "amqp://#OINK_RABBIT_USERNAME_DEST#:#OINK_RABBIT_PASSWORD_DEST#@#OINK_RABBIT_HOST_DEST#:#OINK_RABBIT_PORT_DEST#", "dest-exchange": "#OINK_RABBIT_DEFAULT_EXCHANGE_DEST#"}'

pushd
cd /opt/oink

sudo ./bin/start

# Wait for Bundles to load
echo "Waiting for 120secs to allow Karaf to start all startupBundles"
sleep 2m

#Wait for it to start
echo "Attempting to connect to karaf"
./bin/client -r 100 -d 6 "echo"

# Enable facade
sudo ./bin/client "oink:enable oink-adapter-facade /opt/oink/settings/facade.properties"

# Wait for facade to start
ATTEMPTS=1
OUTPUT=1
while [ $OUTPUT -ne 0 ] && [ $ATTEMPTS -lt 6 ]; do
        sleep 10
        ((ATTEMPTS++))
        OUTPUT=$(./bin/client -r 100 -d 6 "oink:status oink-adapter-facade" | tail -1)
        echo "Attempt $ATTEMPTS had status $OUTPUT"
done
if [ $OUTPUT -ne 0 ] 
then
        echo "oink-adapter-facade did not start. Check logs"
fi

# Enable proxy
./bin/client "oink:enable oink-adapter-proxy /opt/oink/settings/proxy.properties"

# Wait for proxy to start
ATTEMPTS=1
OUTPUT=1
while [ $OUTPUT -ne 0 ] && [ $ATTEMPTS -lt 6 ]; do
        sleep 10
        ((ATTEMPTS++))
        OUTPUT=$(./bin/client -r 100 -d 6 "oink:status oink-adapter-proxy" | tail -1)
        echo "Attempt $ATTEMPTS had status $OUTPUT"
done
if [ $OUTPUT -ne 0 ] 
then
        echo "oink-adapter-proxy did not start. Check logs"
fi

popd
