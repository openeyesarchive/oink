#!/bin/bash

# Configure RabbitMQ User
sudo rabbitmqctl add_user oinkendpoint2 Test1571
sudo rabbitmqctl set_permissions -p / oinkendpoint2 ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags oinkendpoint2 management

# Move oink to correct location
sudo mkdir -p /opt/oink
sudo chown -R `whoami` /opt/oink

mkdir -p /opt/oink/settings
pushd .
cd /opt/oink
cp /vagrant/vfs/distro-*.tar.gz .
tar -zxvf distro-*.tar.gz

# Start Karaf
pushd .
cd distro-*
export JAVA_HOME=$JAVA_HOME
./bin/start

#Wait for it to start
echo "Attempting to connect to karaf"
./bin/client -r 20 -d 6 "echo"

#Enable hl7v2
./bin/client "oink:enable oink-adapter-hl7v2 /vagrant/guests/endpoint2/hl7v2.properties"

popd

popd