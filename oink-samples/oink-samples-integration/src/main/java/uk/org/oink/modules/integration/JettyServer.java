package uk.org.oink.modules.integration;

import java.io.File;
import java.util.Properties;

import javax.naming.NamingException;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer {

	private Server server;
	private final int port;
	private Properties properties;
	
	public static final String[] JETTY_PLUS_CONFIGURATION_CLASSES =
		   {
		       "org.eclipse.jetty.webapp.WebInfConfiguration",
		       "org.eclipse.jetty.webapp.WebXmlConfiguration",
		       "org.eclipse.jetty.webapp.MetaInfConfiguration",
		       "org.eclipse.jetty.webapp.FragmentConfiguration",
		       "org.eclipse.jetty.plus.webapp.EnvConfiguration",
		       "org.eclipse.jetty.plus.webapp.PlusConfiguration",
		       "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"
		   };

	public JettyServer(int port, String warFile) {
		this.port = port;
		properties = new Properties();
		server = new Server(port);
		String resolvedWarFile = resolveWarPath(warFile);
		server.setHandler(getWebAppContext(resolvedWarFile));
		server.setStopAtShutdown(true);
	}

	public void start() throws Exception {
		server.start();
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
			server.join();
		}
	}

	/*
	 * Define an env entry with Server scope. 
	 * At runtime, the webapp accesses
	 * this as java:comp/env/key This is equivalent to putting an env-entry
	 * in web.xml: 
	 * <env-entry> 
	 * 	<env-entry-name>key</env-entry-name>
	 * 	<env-entry-type>java.lang.String</env-entry-type>
	 * 	<env-entry-value>value</env-entry-value> 
	 * </env-entry>
	 */
	public void addSimpleJDNIProperty(String key, String value)
			throws NamingException {
		EnvEntry wiggle = new EnvEntry(server, key, value, false);
	}

	private static String resolveWarPath(String war) {
		File file = new File(war);
		if (!file.exists()) {
			throw new Error("Specified WAR file does not exist!");
		}
		return war;
	}

	private WebAppContext getWebAppContext(String warFile) {
		WebAppContext context = new WebAppContext();
		context.setWar(warFile);
		context.setConfigurationClasses(JETTY_PLUS_CONFIGURATION_CLASSES);
		context.setContextPath("/");
		return context;
	}

	public void join() throws InterruptedException {
		server.join();
	}
}
