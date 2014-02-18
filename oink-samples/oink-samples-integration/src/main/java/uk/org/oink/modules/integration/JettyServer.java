package uk.org.oink.modules.integration;

import java.io.File;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer {

	private Server server;
	private final int port;

	public JettyServer(int port, String warFile) {
		this.port = port;
		server = new Server();
		server.setThreadPool(getThreadPool());
		server.setConnectors(new Connector[] { getConnector() });
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
		context.setContextPath("/");
		return context;
	}

	private Connector getConnector() {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setMaxIdleTime(30000);
		connector.setPort(port);
		return connector;
	}

	private QueuedThreadPool getThreadPool() {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(100);
		return threadPool;
	}

	public void join() throws InterruptedException {
		server.join();
	}
}
