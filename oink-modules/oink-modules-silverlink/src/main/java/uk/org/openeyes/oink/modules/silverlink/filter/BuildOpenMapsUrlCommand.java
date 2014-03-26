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
package uk.org.openeyes.oink.modules.silverlink.filter;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.chain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.filterchain.FilterChainContext;
import uk.org.openeyes.oink.filterchain.command.FilterCommand;

/**
 * Sets the Openmaps URL of the HttpRequest using saved properties and
 * attributes of the OinkRequestMessage
 * 
 * @author Oliver Wilkie
 */
@Component
public class BuildOpenMapsUrlCommand extends FilterCommand {

	private final static Logger logger = LoggerFactory
			.getLogger(BuildOpenMapsUrlCommand.class);

	@Value("${openmaps.scheme}")
	private String scheme;
	@Value("${openmaps.host}")
	private String host;
	@Value("${openmaps.port}")
	private int port;
	@Value("${openmaps.fhirRoot}")
	private String fhirRoot; // No trailing slash

	@Override
	protected boolean execute(FilterChainContext context) throws Exception {
		try {
			OINKRequestMessage request = context.getRequest();
			String fhirResource = request.getResourcePath();
			Map<String, String> parameters = request.getParameters();

			GenericUrl builder = new GenericUrl();
			builder.setScheme(scheme);
			builder.setHost(host);
			builder.setPort(port);
			builder.setRawPath(mergePaths(fhirRoot, fhirResource));
			for (Entry<String, String> entry : parameters.entrySet()) {
				builder.set(entry.getKey(), entry.getValue());
			}

			HttpRequest httpRequest = context.getHttpRequest();
			httpRequest.setUrl(builder);
			return Command.CONTINUE_PROCESSING;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	private static String mergePaths(String fhirRoot, String fhirResource) {
		// fhirRoot should have no trailing char
		if (fhirRoot.endsWith("/")) {
			fhirRoot = fhirRoot.substring(0, fhirRoot.length() - 1);
		}
		// fhirResource should have no leading char
		if (fhirResource.startsWith("/")) {
			fhirResource = fhirResource.length() == 0 ? "" : fhirResource
					.substring(1);
		}
		return fhirRoot + "/" + fhirResource;
	}

}
