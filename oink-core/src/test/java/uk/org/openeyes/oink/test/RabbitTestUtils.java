package uk.org.openeyes.oink.test;

import java.io.IOException;
import java.util.Properties;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitTestUtils {

	public static boolean isRabbitMQAvailable(Properties props) {
		ConnectionFactory factory = buildFactory(props.getProperty("rabbit.host"),
				Integer.parseInt(props.getProperty("rabbit.port")),
				props.getProperty("rabbit.username"), props.getProperty("rabbit.password"),
				props.getProperty("rabbit.vhost"));
		Connection conn = null;
		try {
			conn = factory.newConnection();
			return conn.isOpen();
		} catch (Exception e) {
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (IOException e) {}
			}
		}
	}
	
	protected static ConnectionFactory buildFactory(String host, int port,
			String uname, String pwd, String vhost) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(port);
		factory.setUsername(uname);
		factory.setPassword(pwd);
		factory.setVirtualHost(vhost);
		return factory;
	}
	
}
