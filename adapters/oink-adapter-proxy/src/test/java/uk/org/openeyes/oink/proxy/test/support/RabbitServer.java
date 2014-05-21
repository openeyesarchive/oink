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