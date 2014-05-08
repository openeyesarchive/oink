package uk.org.openeyes.oink.hl7v2.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.Version;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.builder.ValidationRuleBuilder;
import ca.uhn.hl7v2.validation.builder.support.DefaultValidationWithoutTNBuilder;
import ca.uhn.hl7v2.validation.builder.support.NoValidationBuilder;
import ca.uhn.hl7v2.validation.impl.DefaultValidationWithoutTN;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public abstract class Hl7TestSupport {

	private Properties properties;

	protected Message loadMessage(String path) throws IOException, HL7Exception {
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
