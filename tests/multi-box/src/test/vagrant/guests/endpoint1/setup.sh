#!/bin/bash

# Configure RabbitMQ User
sudo rabbitmqctl add_user oinkendpoint1 Test1571
sudo rabbitmqctl set_permissions -p / oinkendpoint1 ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags oinkendpoint1 management

# Setup RabbitMQ dynamic shovels
sudo rabbitmq-plugins enable rabbitmq_management
sudo rabbitmq-plugins enable rabbitmq_shovel
sudo rabbitmq-plugins enable rabbitmq_shovel_management
sudo service rabbitmq-server restart

sudo rabbitmqctl set_parameter shovel "oink_out_shovel" '{"src-uri": "amqp://oinkendpoint1:Test1571@10.0.115.2:5672/", "src-queue": "pas.hl7v2.in", "dest-uri": "amqp://oinkendpoint2:Test1571@10.0.115.3:5672/", "dest-exchange": "test"}'

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

# Enable facade
./bin/client "oink:enable oink-adapter-facade /vagrant/guests/endpoint1/facade.properties"

# Enable proxy
./bin/client "oink:enable oink-adapter-proxy /vagrant/guests/endpoint1/proxy.properties"

popd

popd

