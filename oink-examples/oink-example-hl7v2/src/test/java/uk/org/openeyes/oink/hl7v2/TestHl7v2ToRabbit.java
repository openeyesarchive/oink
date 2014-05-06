package uk.org.openeyes.oink.hl7v2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class TestHl7v2ToRabbit {

	private static Properties testProperties;
	private static ConnectionFactory factory;
	
	@Autowired
	CamelContext ctx;

	@BeforeClass
	public static void setUp() throws IOException {
		// Load properties
		testProperties = new Properties();
		InputStream is = TestHl7v2ToRabbit.class
				.getResourceAsStream("/hl7v2-test.properties");
		testProperties.load(is);

		// Prepare RabbitMQ Client
		factory = new ConnectionFactory();
		factory.setHost(testProperties.getProperty("rabbit.host"));
		factory.setPort(Integer.parseInt(testProperties
				.getProperty("rabbit.port")));
		factory.setUsername(testProperties.getProperty("rabbit.username"));
		factory.setPassword(testProperties.getProperty("rabbit.password"));
		factory.setVirtualHost(testProperties.getProperty("rabbit.vhost"));
	}

	@Test
	public void testThisRouteCanRecieveHl7v2MessagesAndPassThemToRabbitUnchanged()
			throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException, HL7Exception, LLPException {

		// Get Sample Hl7v2 message
		String messageToSend = loadMessage("/samples/A01.txt");
		
		// Prepare RabbitQueue
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName,
				testProperties.getProperty("rabbit.defaultExchange"),
				"testEndpoint");
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);


		// Send to route via TCP
		HapiContext context = new DefaultHapiContext();
		Parser p = context.getPipeParser();
		Message adt = p.parse(messageToSend);
		ca.uhn.hl7v2.app.Connection hl7v2Conn = context.newClient(testProperties.getProperty("hl7v2.host"), Integer.parseInt(testProperties.getProperty("hl7v2.port")), false);
		Initiator initiator = hl7v2Conn.getInitiator();
		initiator.sendAndReceive(adt);
		hl7v2Conn.close();
		
		// Wait for RabbitQueue delivery
		QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
		
		assertNotNull(delivery);
		
		String receivedMessage = ctx.getTypeConverter().convertTo(String.class, delivery.getBody());
		
		//TODO Received message has a new line character on the end. Is this okay?
		assertEquals(messageToSend.trim().length(), receivedMessage.trim().length());
		assertEquals(messageToSend.trim(), receivedMessage.trim());
	}

	private String loadMessage(String path) throws IOException {
		InputStream is = getClass().getResourceAsStream(path);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
	}

}
