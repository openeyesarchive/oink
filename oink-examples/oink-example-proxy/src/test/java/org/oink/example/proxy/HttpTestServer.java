package org.oink.example.proxy;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Ignore;

@Ignore
public class HttpTestServer {
	
	public final int HTTP_PORT;
	private Server server;
	private String context;
	
	private String requestMethod;
	private String requestPath;
	private String requestParams;
	private String requestBody;
	
	private int responseCode;
    private byte[] responseBody;
    private String responseContentType;
    
    public HttpTestServer(int port) {
    	HTTP_PORT = port;
    	context = "/";
    }
    
    public HttpTestServer(int port, String context) {
    	HTTP_PORT = port;
    	this.context = context;
    }
    
    public void start() throws Exception {
    	configureServer();
    	server.start();
    }
    
    public void stop() throws Exception {
    	server.stop();
    }
    
    public void join() throws InterruptedException {
    	server.join();
    }
    
    protected void configureServer() {
    	server = new Server(HTTP_PORT);
    	server.setHandler(getMockHandler());
    	
    }
    
    public Handler getMockHandler() {
    	Handler handler = new AbstractHandler() {
			
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request,
					HttpServletResponse response) throws IOException, ServletException {
				
	                requestBody = (IOUtils.toString(baseRequest.getInputStream(),"UTF-8"));
	                requestMethod = baseRequest.getMethod();
	                requestParams = baseRequest.getQueryString();
	                requestPath = baseRequest.getPathInfo();
	                
	                response.setStatus(responseCode);
	                response.setContentType(responseContentType);
	                IOUtils.write(responseBody, response.getOutputStream());
	                baseRequest.setHandled(true);
				
			}
		};
		return handler;
    }

    
    public void setResponse(int code, byte[] body, String contentType) {
    	this.responseCode = code;
    	this.responseBody = body;
    	this.responseContentType = contentType;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
 
    public String getRequestBody() {
        return requestBody;
    }

	public final String getRequestMethod() {
		return requestMethod;
	}

	public final String getRequestPath() {
		return requestPath;
	}

	public final String getRequestParams() {
		return requestParams;
	}
 

}
