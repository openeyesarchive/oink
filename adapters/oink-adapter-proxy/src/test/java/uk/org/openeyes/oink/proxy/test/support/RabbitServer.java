/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.proxy.test.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitServer implements Runnable {
	
	private final static Logger log = LoggerFactory.getLogger(RabbitServer.class);
	
	Thread t;

	ConnectionFactory factory;

	String exchange;
	String routingKey;

	byte[] receivedMessage;
	
	boolean stop;

	public RabbitServer(String host, int port, String virtualHost,
			String username, String password) {
		factory = new ConnectionFactory();
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setHost(host);
		factory.setPort(port);
		factory.setVirtualHost(virtualHost);
	}

	public void setConsumingDetails(String exchange, String routingKey) {
		this.exchange = exchange;
		this.routingKey = routingKey;
	}

	public byte[] getReceivedMessage() {
		return receivedMessage;
	}
	
	public void start() {
		t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		try {
			log.info("RabbitServer started");

			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.exchangeDeclare(exchange, "direct",true,true,null);
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, exchange, routingKey);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);

			while (!stop) {
				// Wait for RabbitQueue delivery
				QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
				if (delivery != null) {
					log.info("Message received");
					receivedMessage = delivery.getBody();
					stop = true;
				}
			}
			
			if (receivedMessage == null) {
				log.warn("RabbitServer stopping before any message was received");
			}

		} catch (Exception e) {
			log.error("RabbitServer threw an exception:"+e);
			e.printStackTrace();
		}

	}
	
	public void stop() throws InterruptedException {
		this.stop = true;
		t.join();
	}

}