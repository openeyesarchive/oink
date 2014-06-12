package uk.org.openeyes.oink.it;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.HttpBasicAuthInterceptor;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.IRestfulClientFactory;

public class ITSupport {
	
	public static File getPropertyFileBySystemProperty(String systemProperty) {
		String path = System.getProperty(systemProperty);
		File f = new File(path);
		return f;
	}
	
	public static Properties getPropertiesBySystemProperty(String systemProperty) throws IOException {
		File f = getPropertyFileBySystemProperty(systemProperty);
		if (!f.exists()) {
			throw new FileNotFoundException("No file found at "+ f.getAbsolutePath() + " for system property "+systemProperty+" is it set correctly?");
		}
		FileInputStream fis = new FileInputStream(f);
		Properties p = new Properties();
		p.load(fis);
		fis.close();
		return p;
	}

	public static IGenericClient buildHapiClientForProxy(Properties proxyProps) {
		// See if Patient exists
		String proxyUri = (String) proxyProps.get("proxy.uri");

		// Create a context and get the client factory so it can be configured
		FhirContext ctx = new FhirContext();
		IRestfulClientFactory clientFactory = ctx.getRestfulClientFactory();

		// Create an HTTP Client Builder
		HttpClientBuilder builder = HttpClientBuilder.create();

		// This interceptor adds HTTP username/password to every request
		String username = (String) proxyProps.get("proxy.username");
		String password = (String) proxyProps.get("proxy.password");
		builder.addInterceptorFirst(new HttpBasicAuthInterceptor(username,
				password));
		
		builder.addInterceptorFirst(new HttpRequestInterceptor() {
			
			@Override
			public void process(HttpRequest req, HttpContext context)
					throws HttpException, IOException {
				req.addHeader("Accept", "application/json+fhir; charset=UTF-8");
			}
		});

		// Use the new HTTP client builder
		clientFactory.setHttpClient(builder.build());
		
		IGenericClient client = clientFactory.newGenericClient("http://"
				+ proxyUri);
		
		return client;
	}
	
	public static IGenericClient buildHapiClientForFacade(Properties proxyProps) {
		String proxyUri = (String) proxyProps.get("proxy.uri");

		// Create a context and get the client factory so it can be configured
		FhirContext ctx = new FhirContext();
		IRestfulClientFactory clientFactory = ctx.getRestfulClientFactory();

		// Create an HTTP Client Builder
		HttpClientBuilder builder = HttpClientBuilder.create();

		// This interceptor adds HTTP username/password to every request
		String username = (String) proxyProps.get("proxy.username");
		String password = (String) proxyProps.get("proxy.password");
		builder.addInterceptorFirst(new HttpBasicAuthInterceptor(username,
				password));
		
		builder.addInterceptorFirst(new HttpRequestInterceptor() {
			
			@Override
			public void process(HttpRequest req, HttpContext context)
					throws HttpException, IOException {
				req.addHeader("Accept", "application/json+fhir; charset=UTF-8");
			}
		});

		// Use the new HTTP client builder
		clientFactory.setHttpClient(builder.build());
		
		IGenericClient client = clientFactory.newGenericClient("http://"
				+ proxyUri);
		
		return client;
	}	
	
}
