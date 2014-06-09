#!/bin/bash

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

sudo rabbitmqctl set_parameter shovel "oink_facade_response_shovel" '{"src-uri": "amqp://oinkendpoint2:Test1571@10.0.115.3", "src-queue": "openeyes.facade.response", "dest-uri": "amqp://oinkendpoint1:Test1571@10.0.115.2", "dest-exchange": "test"}'

sudo rabbitmqctl set_parameter shovel "oink_proxy_in_shovel" '{"src-uri": "amqp://oinkendpoint2:Test1571@10.0.115.3", "src-queue": "openeyes.proxy.in", "dest-uri": "amqp://oinkendpoint1:Test1571@10.0.115.2", "dest-exchange": "test"}'

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
touch bin/setenv
echo "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64" >> bin/setenv
echo "export JAVA_MIN_MEM=512M" >> bin/setenv
echo "export JAVA_MAX_MEM=1024M" >> bin/setenv
echo "export JAVA_PERM_MEM=512M" >> bin/setenv
sudo ./bin/start

# Wait for it to start
echo "Attempting to connect to karaf"
./bin/client -r 100 -d 6 "echo"

# Enable hl7v2
./bin/client "oink:enable oink-adapter-hl7v2 /vagrant/guests/endpoint2/hl7v2.properties"

popd

popd