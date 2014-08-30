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

# Install RabbitMQ
sudo echo -e "\n\ndeb http://www.rabbitmq.com/debian/ testing main\n" | sudo tee --append /etc/apt/sources.list
wget http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
sudo apt-key add rabbitmq-signing-key-public.asc
rm rabbitmq-signing-key-public.asc
sudo apt-get -y update
sudo apt-get -y install rabbitmq-server

# Delete RabbitMQ guest
sudo rabbitmqctl delete_user guest

# Configure RabbitMQ oinkadmin
sudo rabbitmqctl add_user oinkadmin Test1571
sudo rabbitmqctl set_permissions -p / oinkadmin ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags oinkadmin administrator

# Configure RabbitMQ endpoint
sudo rabbitmqctl add_user oinkendpoint2 Test1571
sudo rabbitmqctl set_permissions -p / oinkendpoint2 ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags oinkendpoint2 management

# Setup RabbitMQ dynamic shovels
sudo rabbitmq-plugins enable rabbitmq_management
sudo rabbitmq-plugins enable rabbitmq_shovel
sudo rabbitmq-plugins enable rabbitmq_shovel_management
sudo service rabbitmq-server restart

sudo rabbitmqctl set_parameter shovel "oink_facade_response_shovel" '{"src-uri": "amqp://oinkendpoint2:Test1571@10.0.115.3", "src-exchange": "oink", "src-exchange-key": "openeyes.facade.response", "dest-uri": "amqp://oinkendpoint1:Test1571@10.0.115.2", "dest-exchange": "oink"}'

sudo rabbitmqctl set_parameter shovel "oink_proxy_in_shovel" '{"src-uri": "amqp://oinkendpoint2:Test1571@10.0.115.3", "src-exchange": "oink", "src-exchange-key": "openeyes.proxy.in", "dest-uri": "amqp://oinkendpoint1:Test1571@10.0.115.2", "dest-exchange": "oink"}'

wget http://hg.rabbitmq.com/rabbitmq-management/raw-file/rabbitmq_v3_3_5/bin/rabbitmqadmin
chmod a+x rabbitmqadmin
./rabbitmqadmin -u oinkadmin -p Test1571 declare queue name=hl7v2.adapter.deadLetters.out durable=true
./rabbitmqadmin -u oinkadmin -p Test1571 declare binding source=oink routing_key=hl7v2.adapter.deadLetters.out destination=hl7v2.adapter.deadLetters.out

# Move oink to correct location
sudo mkdir -p /opt/oink
sudo chown -R `whoami` /opt/oink

pushd .
cd /opt/oink
cp /vagrant/vfs/oink-platforms-karaf-distro-*.tar.gz .
tar -zxvf oink-platforms-karaf-distro-*.tar.gz

# Start Karaf
pushd .
cd oink-platforms-karaf-distro-*
touch bin/setenv
echo "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64" >> bin/setenv
echo "export JAVA_MIN_MEM=512M" >> bin/setenv
echo "export JAVA_MAX_MEM=1024M" >> bin/setenv
echo "export JAVA_PERM_MEM=512M" >> bin/setenv
echo "karaf.delay.console=true" >> etc/system.properties
sudo sed -i -e 's@^log4j\.appender\.out\.file.*@log4j\.appender\.out\.file=/vagrant/guests/endpoint2/karaf.log@g' etc/org.ops4j.pax.logging.cfg

sudo ./bin/start

# Wait for Bundles to load
echo "Waiting for 120secs to allow Karaf to start all startupBundles"
sleep 2m

# Wait for it to start
echo "Attempting to connect to karaf"
./bin/client -r 100 -d 6 ""

# Enable hl7v2
./bin/client "oink:enable oink-adapter-hl7v2 /vagrant/guests/endpoint2/hl7v2.properties"

# Wait for hl7v2
# Wait for proxy to start
ATTEMPTS=1
OUTPUT=1
while [ $OUTPUT -ne 0 ] && [ $ATTEMPTS -lt 6 ]; do
        sleep 10
        ((ATTEMPTS++))
        OUTPUT=$(./bin/client -r 100 -d 6 "oink:status oink-adapter-hl7v2" | tail -1)
        echo "Attempt $ATTEMPTS had status $OUTPUT"
done
if [ $OUTPUT -ne 0 ] 
then
        echo "oink-adapter-hl7v2 did not start. Check logs"
fi

popd

popd