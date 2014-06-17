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

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RabbitClient {

	ConnectionFactory factory;
	
	private static final Logger log = LoggerFactory.getLogger(RabbitClient.class);

	public RabbitClient(String host, int port, String virtualHost,
			String username, String password) {
		factory = new ConnectionFactory();
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setHost(host);
		factory.setPort(port);
		factory.setVirtualHost(virtualHost);
	}
	
	public OINKResponseMessage sendAndRecieve(OINKRequestMessage message, String routingKey, String exchange) throws Exception {
		OinkMessageConverter conv = new OinkMessageConverter();
		byte[] msgBytes = conv.toByteArray(message);
		byte[] msgResp = sendAndRecieve(msgBytes, routingKey, exchange);
		if (msgResp == null) {
			throw new Exception("No response received");
		}
		return conv.responseMessageFromByteArray(msgResp);
	}

	public byte[] sendAndRecieve(byte[] message, String routingKey,
			String exchange) throws Exception {
		log.debug("Sending message to direct exchange:"+exchange+" with routing key:"+routingKey);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(exchange, "direct", true, false, null);
		String replyQueueName = channel.queueDeclare().getQueue();
		log.debug("Reply queue name is "+replyQueueName);
		channel.queueBind(replyQueueName, exchange, replyQueueName);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);
		String corrId = java.util.UUID.randomUUID().toString();
		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrId).replyTo(replyQueueName).build();

		channel.basicPublish(exchange, routingKey, props, message);
		log.debug("Waiting for delivery");
		QueueingConsumer.Delivery delivery = consumer.nextDelivery(20000);
		connection.close();
		if (delivery == null
				|| !delivery.getProperties().getCorrelationId()
						.equals(corrId)) {
			return null;
		} else {
			byte[] response = delivery.getBody();
			return response;

		}
	}

}