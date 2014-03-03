package uk.org.openeyes.oink.integration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.naming.NamingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;

import uk.org.oink.modules.integration.JettyServer;

public class ITFacadeAndSilverlink {

	private static final String facadeWar = "./target/war/facade-web.war";
	private static final String silverlinkWar = "./target/war/silverlink-web.war";
	private static final String openmapsWar = "./target/war/openmap-web.war";
	private static final String facadeConfigLocation = "classpath:/facade.properties";
	private static final String silverlinkConfigLocation = "classpath:/silverlink.properties";
	private static JettyServer facadeServer;
	private static JettyServer silverlinkServer;
	private static JettyServer openmapsServer;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		facadeServer = setUpFacadeServer();
		silverlinkServer = setUpSilverlinkServer();
		openmapsServer = setUpOpenMapsServer();
		facadeServer.start();
		silverlinkServer.start();
		openmapsServer.start();
	}
	
	public static JettyServer setUpFacadeServer() throws NamingException {
		JettyServer facadeServer = new JettyServer(4455, facadeWar);
		facadeServer.addSimpleJDNIProperty("properties/location", facadeConfigLocation);
		return facadeServer;
	}
	
	public static JettyServer setUpSilverlinkServer() throws NamingException {
		JettyServer silverlinkServer = new JettyServer(4456,silverlinkWar);
		silverlinkServer.addSimpleJDNIProperty("properties/location", silverlinkConfigLocation);
		return silverlinkServer;
	}
	
	public static JettyServer setUpOpenMapsServer()  {
		return new JettyServer(4457, openmapsWar);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		facadeServer.stop();
		silverlinkServer.stop();
		openmapsServer.stop();
	}
	
	@Test
	public void testCanFindFacadeConfigProperties() throws MalformedURLException {
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource resource = loader.getResource(facadeConfigLocation);
		assertTrue(resource.exists());
	}
	
	public void testServerLoaded() throws InterruptedException {
		openmapsServer.join();
	}
	
	@Test
	public void testCanReachSilverlinkServer() throws ClientProtocolException, IOException, InterruptedException {
		openmapsServer.join();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("http://localhost:4455/facade/silverlink/Patient?gender=M");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		assertEquals(HttpStatus.OK.value(), response1.getStatusLine().getStatusCode());
	}
	

}
