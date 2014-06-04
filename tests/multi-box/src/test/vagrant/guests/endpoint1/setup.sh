#!/bin/bash

sudo rabbitmqctl add_user oinkendpoint1 Test1571
sudo rabbitmqctl set_permissions -p / oinkendpoint1 ".*" ".*" ".*"
sudo rabbitmqctl set_user_tags oinkendpoint1 management