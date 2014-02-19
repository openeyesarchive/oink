package uk.org.oink.modules.integration;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer {

	private Server server;
	private final int port;

	public JettyServer(int port, String warFile) {
		this.port = port;
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

	public void join() throws InterruptedException {
		server.join();
	}
}
