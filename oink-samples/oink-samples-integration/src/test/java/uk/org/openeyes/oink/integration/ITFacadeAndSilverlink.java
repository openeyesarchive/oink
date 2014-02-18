package uk.org.openeyes.oink.integration;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import uk.org.oink.modules.integration.JettyServer;

public class ITFacadeAndSilverlink {

	private static final String facadeWar = "./target/war/facade-web.war";
	private static final String silverlinkWar = "./target/war/silverlink-web.war";
	private static JettyServer facadeServer;
	private static JettyServer silverlinkServer;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		facadeServer = new JettyServer(4455, facadeWar);
		silverlinkServer = new JettyServer(4456, silverlinkWar);
		facadeServer.start();
		silverlinkServer.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		facadeServer.stop();
		silverlinkServer.stop();
	}

	@Test
	public void testCanReachSilverlinkServer() throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("http://localhost:4456");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		assertEquals(HttpStatus.OK.value(), response1.getStatusLine().getStatusCode());
	}

}
