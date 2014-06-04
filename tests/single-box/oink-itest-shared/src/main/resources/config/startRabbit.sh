#!/bin/bash

eval rabbitmqctl status

if [ $? -eq 0 ]; then
	echo RabbitMQ is already running
	exit 0
fi

eval rabbitmq-server -detached

if [ $? -ne 0 ]; then
	echo RabbitMQ could not be started
	exit 1
fi

exit

