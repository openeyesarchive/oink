#!/bin/bash

# Delete RabbitMQ guest
sudo rabbitmqctl delete_user guest

# Configure RabbitMQ oinkadmin
sudo rabbitmqctl add_user oinkadmin Test1571
sudo rabbitmqctl set_permissions -p / oinkadmin ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags oinkadmin administrator

# Configure RabbitMQ endpoint
sudo rabbitmqctl add_user oinkendpoint1 Test1571
sudo rabbitmqctl set_permissions -p / oinkendpoint1 ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags oinkendpoint1 management

# Setup RabbitMQ dynamic shovels
sudo rabbitmq-plugins enable rabbitmq_management
sudo rabbitmq-plugins enable rabbitmq_shovel
sudo rabbitmq-plugins enable rabbitmq_shovel_management
sudo service rabbitmq-server restart

sudo rabbitmqctl set_parameter shovel "oink_out_shovel" '{"src-uri": "amqp://oinkendpoint1:Test1571@10.0.115.2", "src-exchange": "test", "src-exchange-key": "pas.hl7v2.in", "dest-uri": "amqp://oinkendpoint2:Test1571@10.0.115.3", "dest-exchange": "test"}'
sudo rabbitmqctl set_parameter shovel "oink_out_shovel" '{"src-uri": "amqp://oinkendpoint1:Test1571@10.0.115.2", "src-exchange": "test", "src-exchange-key": "pas.hl7v2.response", "dest-uri": "amqp://oinkendpoint2:Test1571@10.0.115.3", "dest-exchange": "test"}'

# Move oink to correct location
sudo mkdir -p /opt/oink
sudo chown -R `whoami` /opt/oink

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
echo "karaf.delay.console=true" >> etc/system.properties
sudo sed -i -e 's@^log4j\.appender\.out\.file.*@log4j\.appender\.out\.file=/vagrant/guests/endpoint1/karaf.log@g' etc/org.ops4j.pax.logging.cfg

sudo ./bin/start

# Wait for Bundles to load
echo "Waiting for 120secs to allow Karaf to start all startupBundles"
sleep 2m

#Wait for it to start
echo "Attempting to connect to karaf"
./bin/client -r 100 -d 6 "echo"

# Enable facade
sudo ./bin/client "oink:enable oink-adapter-facade /vagrant/guests/endpoint1/facade.properties"

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
./bin/client "oink:enable oink-adapter-proxy /vagrant/guests/endpoint1/proxy.properties"

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

popd

