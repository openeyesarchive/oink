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
package uk.org.openeyes.oink.it.multi;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.datagen.adapters.PersonToHL7v24PIDAdapter;
import uk.org.openeyes.oink.datagen.domain.Person;
import uk.org.openeyes.oink.datagen.generators.person.PersonGenerator;
import uk.org.openeyes.oink.datagen.generators.person.PersonGeneratorFactory;
import uk.org.openeyes.oink.datagen.mocks.mpi.MPIImpl;
import uk.org.openeyes.oink.datagen.mocks.mpi.MPIRepo.GetAllContext;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ACK;
import ca.uhn.hl7v2.model.v24.message.ADT_A01;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ITLoadTestHL7v24ToOpenEyes {

	private static Logger logger = LoggerFactory
			.getLogger(ITLoadTestHL7v24ToOpenEyes.class);

	private static Properties properties;

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {
		properties = new Properties();
		InputStream is = ITLoadTestHL7v24ToOpenEyes.class
				.getResourceAsStream("ITLoadTestHL7v24ToOpenEyes.properties");
		properties.load(is);
	}

	@Test
	public void test() throws Exception {

		PersonToHL7v24PIDAdapter adapter = new PersonToHL7v24PIDAdapter();
		PersonGenerator g = PersonGeneratorFactory.getInstance("uk");

		int quantity = Integer.parseInt(properties.getProperty("test.quantity",
				"1000"));
		int batchSize = 100;

		MPIImpl mpi = new MPIImpl();

		mpi.start();

		logger.debug("Generating {} patients...", quantity);

		if (mpi.getRepo().getSize() < 1) {

			mpi.getRepo().deleteAll();

			// Generate in batches
			for (int i = 0; i < quantity / batchSize; i++) {
				List<Person> patients = g.generate(batchSize);

				mpi.getRepo().addPatients(patients);
			}
		}

		// Connect to queue

		String connectionString = String.format("amqp://%s:%s@%s:%s%s",
				properties.getProperty("rabbitmq.username", "guest"),
				properties.getProperty("rabbitmq.password", "guest"),
				properties.getProperty("rabbitmq.host", "localhost"),
				properties.getProperty("rabbitmq.port", "5672"),
				properties.getProperty("rabbitmq.vhost", ""));

		logger.debug("URI: " + connectionString);

		String queueNameIn = properties.getProperty("rabbitmq.routingKey.in",
				"oink.it.in");
		String queueNameError = properties.getProperty(
				"rabbitmq.routingKey.error", "oink.it.err");

		ConnectionFactory factory = new ConnectionFactory();
		factory.setUri(connectionString);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(queueNameIn, true, false, false, null);
		channel.queueDeclare(queueNameError, true, false, false, null);

		channel.queuePurge(queueNameIn);
		channel.queuePurge(queueNameError);

		int totalSize = mpi.getRepo().getSize();
		int pageSize = 100;
		GetAllContext context = mpi.getRepo().getAllInit();

		logger.debug("Publishing ADT messages ...");

		for (int i = 0; i < (totalSize / pageSize); i++) {

			List<Person> patients = mpi.getRepo().getAllByPage(context, i,
					pageSize);

			for (Person patient : patients) {
				ADT_A01 adt = adapter.convert(patient);
				String adtMessageString = adt.toString();

				AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
				BasicProperties persistentBasic = bob.priority(0)
						.contentType("text/plain; charset=utf8").build();
				channel.basicPublish("", queueNameIn, persistentBasic,
						adtMessageString.getBytes());
			}

			logger.debug("{}...", i);
		}

		HapiContext hapiContext = new DefaultHapiContext();
		hapiContext.setValidationContext(new NoValidation());
		ca.uhn.hl7v2.app.Connection hl7v2Conn = hapiContext.newClient(
				(String) properties.get("hl7v2.host"), (Integer) Integer
						.parseInt(properties.getProperty("hl7v2.port")), false);
		Initiator initiator = hl7v2Conn.getInitiator();
		Parser p = hapiContext.getGenericParser();
		
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueNameIn, true, consumer);

		int messagesConsumer = 0;
		while (messagesConsumer < totalSize) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			messagesConsumer++;

			Message m = p.parse(message);

			ACK response = (ACK) initiator.sendAndReceive(m);

			if (!response.getMSA().getAcknowledgementCode().getValue()
					.equalsIgnoreCase("AA")) {

				AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
				BasicProperties persistentBasic = bob.priority(0)
						.contentType("text/plain; charset=utf8").build();
				channel.basicPublish("", queueNameError, persistentBasic,
						message.getBytes());
			}
		}

		hapiContext.close();

		mpi.stop();

		logger.debug("Patients: {}", totalSize);
	}
}
