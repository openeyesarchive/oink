package uk.org.openeyes.oink.hl7v2.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public abstract class Hl7ITSupport {
	
	private static final Logger log = LoggerFactory.getLogger(Hl7ITSupport.class);

	private Properties properties;
	
	public void testIncomingMessageIsProcessedAndRouted(String hl7msgPath, String oinkmsgPath) throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		// Choose a message to send
		Message m = loadHl7Message(hl7msgPath);
		
		// Prepare RabbitServer
		RabbitServer server = new RabbitServer(getProperty("rabbit.host"),
				Integer.parseInt(getProperty("rabbit.port")),
				getProperty("rabbit.vhost"), getProperty("rabbit.username"),
				getProperty("rabbit.password"));
		server.setConsumingDetails(getProperty("rabbit.defaultExchange"), getProperty("rabbit.outboundRoutingKey"));
		server.start();
		
		// Send HL7v2 message
		String host = getProperty("hl7v2.host");
		int port = Integer.parseInt(getProperty("hl7v2.port"));
		Message responseMessage = HL7Client.send(m, host, port);
		
		Thread.sleep(1000);
		
		// Check received Rabbit message
		byte[] receivedMessage = server.getReceivedMessage();
		server.stop();
		
		assertNotNull(receivedMessage);
		
		OinkMessageConverter conv = new OinkMessageConverter();
		
		OINKRequestMessage request = conv.fromByteArray(receivedMessage);		
		String expectedJson = loadResourceAsString(oinkmsgPath);
		String actualJson = conv.toJsonString(request);		
		JSONAssert.assertEquals(expectedJson,actualJson, false);
	}

	protected Message loadHl7Message(String path) throws IOException, HL7Exception {
		InputStream is = getClass().getResourceAsStream(path);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String message = writer.toString();
		HapiContext context = new DefaultHapiContext();

		context.setValidationContext(new NoValidation());
		Parser p = context.getGenericParser();
		Message adt = p.parse(message);
		return adt;
	}

	protected void setProperties(String path) throws IOException {
		// Load properties
		properties = new Properties();
		InputStream is = getClass().getResourceAsStream(path);
		properties.load(is);
	}

	protected String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static String loadResourceAsString(String resourcePath) throws IOException {
		InputStream is = Hl7ITSupport.class.getResourceAsStream(resourcePath);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
	}

	protected Message sendHl7Message(Message adt, String host, int port)
			throws NumberFormatException, HL7Exception, LLPException,
			IOException {
		HapiContext context = new DefaultHapiContext();
		ca.uhn.hl7v2.app.Connection hl7v2Conn = context.newClient(host, port,
				false);
		Initiator initiator = hl7v2Conn.getInitiator();
		Message response = initiator.sendAndReceive(adt);
		hl7v2Conn.close();
		return response;
	}

	protected static Channel getChannel(ConnectionFactory factory)
			throws IOException {
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		return channel;
	}

	protected static ConnectionFactory initRabbit(String host, int port,
			String uname, String pwd, String vhost) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(port);
		factory.setUsername(uname);
		factory.setPassword(pwd);
		factory.setVirtualHost(vhost);
		return factory;
	}

	protected static String setupRabbitQueue(Channel channel, String exchange,
			String routingKey) throws IOException {
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, exchange, routingKey);
		return queueName;
	}

	public byte[] receiveRabbitMessage(Channel channel, String queueName,
			int timeout) throws IOException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException {
		
		// Prepare RabbitQueue

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);
		// Wait for RabbitQueue delivery
		QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeout);
		if (delivery != null) {
			return delivery.getBody();
		} else {
			return null;
		}
	}
	
	public static class HL7Client {
		
		public static Message send(Message message, String host, int port) throws HL7Exception, LLPException, IOException {
			HapiContext context = new DefaultHapiContext();
			ca.uhn.hl7v2.app.Connection hl7v2Conn = context.newClient(host, port,
					false);
			Initiator initiator = hl7v2Conn.getInitiator();
			Message response = initiator.sendAndReceive(message);
			hl7v2Conn.close();
			return response;
		}
		
	}

	public class HL7Server {

		HapiContext context;
		HL7Service server;

		Message receivedMessage;
		Message returnedMessage;

		public HL7Server(int port, boolean useTls) {
			context = new DefaultHapiContext();
			context.setValidationContext(new NoValidation());
			server = context.newServer(port, useTls);
		}

		public void setMessageHandler(String messageType, String triggerEvent,
				ReceivingApplication handler) {
			ReceivingApplicationDecorator decorator = new ReceivingApplicationDecorator(
					handler);
			server.registerApplication(messageType, triggerEvent, decorator);
		}

		public void setExceptionHandler(
				ReceivingApplicationExceptionHandler exHandler) {
			server.setExceptionHandler(exHandler);
		}

		public void start() throws InterruptedException {
			server.startAndWait();
		}

		public void stop() {
			server.stopAndWait();
		}

		public final Message getReceivedMessage() {
			return receivedMessage;
		}

		public final Message getReturnedMessage() {
			return returnedMessage;
		}

		private class ReceivingApplicationDecorator implements
				ReceivingApplication {

			ReceivingApplication child;

			public ReceivingApplicationDecorator(ReceivingApplication child) {
				this.child = child;
			}

			@Override
			public Message processMessage(Message theMessage,
					Map<String, Object> theMetadata)
					throws ReceivingApplicationException, HL7Exception {
				HL7Server.this.setReceivedMessage(theMessage);
				Message response = child
						.processMessage(theMessage, theMetadata);
				HL7Server.this.setReturnedMessage(response);
				return response;
			}

			@Override
			public boolean canProcess(Message theMessage) {
				return child.canProcess(theMessage);
			}

		}

		public final void setReceivedMessage(Message receivedMessage) {
			this.receivedMessage = receivedMessage;
		}

		public final void setReturnedMessage(Message returnedMessage) {
			this.returnedMessage = returnedMessage;
		}

	}

	public class RabbitClient {

		ConnectionFactory factory;

		public RabbitClient(String host, int port, String virtualHost,
				String username, String password) {
			factory = new ConnectionFactory();
			factory.setUsername(username);
			factory.setPassword(password);
			factory.setHost(host);
			factory.setPort(port);
			factory.setVirtualHost(virtualHost);
		}

		public byte[] sendAndRecieve(byte[] message, String routingKey,
				String exchange) throws Exception {
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			String replyQueueName = channel.queueDeclare().getQueue();
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(replyQueueName, true, consumer);
			String corrId = java.util.UUID.randomUUID().toString();
			BasicProperties props = new BasicProperties.Builder()
					.correlationId(corrId).replyTo(replyQueueName).build();

			channel.basicPublish(exchange, routingKey, props, message);
			QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
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

	public class RabbitServer implements Runnable {
		
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
}
