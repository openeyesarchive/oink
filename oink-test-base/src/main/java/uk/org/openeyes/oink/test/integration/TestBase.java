/*******************************************************************************
 * OpenEyes Interop Toolkit
 * Copyright (C) 2013  OpenEyes Foundation (http://www.openeyes.org.uk)
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
 ******************************************************************************/
package uk.org.openeyes.oink.test.integration;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

public abstract class TestBase {

	private static String versionSuffix = "-1.0.0-SNAPSHOT";
	
	protected static String getVersionSuffix() {
		return versionSuffix;
	}
	
	protected static void setupJettyInstance(Server server, int port, String warFile, String contextPath) throws Exception {
		
		   server = new Server();

		   QueuedThreadPool threadPool = new QueuedThreadPool();
		   threadPool.setMaxThreads(100);
		   server.setThreadPool(threadPool);

		   SelectChannelConnector connector = new SelectChannelConnector();
		   connector.setPort(Integer.valueOf(port));
		   connector.setMaxIdleTime(30000);
		   connector.setConfidentialPort(8443);
		   server.setConnectors(new Connector[] {connector});

		   WebAppContext webApp = new WebAppContext();
		   webApp.setWar(warFile);
		   webApp.setContextPath(contextPath);
		   server.addHandler(webApp);

		   System.setProperty("org.eclipse.jetty.util.log.DEBUG","false");
		   server.start();

		   server.setStopAtShutdown(true);

		   server.setSendServerVersion(true);
	}
	
	protected static void stopJettyInstance(Server server) throws Exception {
		if(server != null) {
			server.stop();
			server.join();
		}
	}
	
	/*
	 * Below is example usage of the methods above in your own test class:
	 * 
	@BeforeClass
	public static void beforeClass() throws Exception {
		
		// Start instances
		setupJettyInstance(serverPresentation, 8080, "../oink-presentation-jpa/target/oink-presentation-jpa" + versionSuffix + ".war", "/presentation");
		setupJettyInstance(serverApp, 8081, "../oink-app-jpa/target/oink-app-jpa" + versionSuffix + ".war", "/app");
		setupJettyInstance(serverRepo, 8082, "../oink-repo-jpa/target/oink-repo-jpa" + versionSuffix + ".war", "/repo");
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		
		// Stop instances
		stopJettyInstance(serverPresentation);
		stopJettyInstance(serverApp);
		stopJettyInstance(serverRepo);
	}
	*/
}
