package uk.org.openeyes.oink.hl7v2.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;
import uk.org.openeyes.oink.proxy.test.support.RabbitServer;
import uk.org.openeyes.oink.test.Hl7Client;
import uk.org.openeyes.oink.test.Hl7Helper;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public abstract class Hl7ITSupport {
	
	private static final Logger log = LoggerFactory.getLogger(Hl7ITSupport.class);

	private Properties properties;
	
	public void testIncomingMessageIsProcessedAndRouted(String hl7msgPath, String oinkmsgPath) throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		// Choose a message to send
		Message m = Hl7Helper.loadHl7Message(hl7msgPath);
		
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
		Message responseMessage = Hl7Client.send(m, host, port);
		
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
}
