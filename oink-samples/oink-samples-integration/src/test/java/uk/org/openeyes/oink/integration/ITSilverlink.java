/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package uk.org.openeyes.oink.integration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.naming.NamingException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.org.oink.modules.integration.JettyServer;

public class ITSilverlink {

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
		facadeServer.addSimpleJDNIProperty("properties/location",
				facadeConfigLocation);
		return facadeServer;
	}

	public static JettyServer setUpSilverlinkServer() throws NamingException {
		JettyServer silverlinkServer = new JettyServer(4456, silverlinkWar);
		silverlinkServer.addSimpleJDNIProperty("properties/location",
				silverlinkConfigLocation);
		return silverlinkServer;
	}

	public static JettyServer setUpOpenMapsServer() {
		return new JettyServer(4457, openmapsWar);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		facadeServer.stop();
		silverlinkServer.stop();
		openmapsServer.stop();
	}

	// @Test
	public void testFoo() throws InterruptedException {
		openmapsServer.join();
	}

	// @Test
	public void testCanFindFacadeConfigProperties()
			throws MalformedURLException {
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource resource = loader.getResource(facadeConfigLocation);
		assertTrue(resource.exists());
	}

	@Test
	public void testSimpleRoundTrip() throws ClientProtocolException,
			IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(
				"http://localhost:4455/silverlink/Patient?_id=72428");
		CloseableHttpResponse response = httpclient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		String received = EntityUtils.toString(entity);

		// Check message received OK
		assertEquals(200, response.getStatusLine().getStatusCode());

		// Check message is JSON
		assertTrue(isValidJSON(received));
		// We can't compare expected with received because some fields are
		// Time-based
		// InputStream inputStream =
		// getClass().getResourceAsStream("Patient.json");
		// StringWriter writer = new StringWriter();
		// IOUtils.copy(inputStream, writer, "UTF-8");
		// String expected = writer.toString();
	}

	private boolean isValidJSON(final String json) {
		boolean valid = false;
		try {
			final JsonParser parser = new ObjectMapper().getJsonFactory()
					.createJsonParser(json);
			while (parser.nextToken() != null) {
			}
			valid = true;
		} catch (JsonParseException jpe) {
			jpe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return valid;
	}

	// @Test
	public void testCanReachSilverlinkServer() throws ClientProtocolException,
			IOException, InterruptedException {
		// openmapsServer.join();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(
				"http://localhost:4455/silverlink/Patient?gender=M");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		assertEquals(HttpStatus.OK.value(), response1.getStatusLine()
				.getStatusCode());
	}

}
