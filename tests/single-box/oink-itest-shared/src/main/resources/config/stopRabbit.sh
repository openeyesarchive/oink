#!/bin/bash

eval rabbitmqctl status

if [ $? -ne 0 ]; then
	echo RabbitMQ isnt available to stop
	exit 0
fi

eval rabbitmqctl stop

if [ $? -ne 0 ]; then
	echo RabbitMQ could not be stopped
	exit 1
fi

exit

