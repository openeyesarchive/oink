/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
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
			Map<String, String[]> parameters = request.getParameters();

			GenericUrl builder = new GenericUrl();
			builder.setScheme(scheme);
			builder.setHost(host);
			builder.setPort(port);
			builder.setRawPath(mergePaths(fhirRoot, fhirResource));
			for (Entry<String, String[]> entry : parameters.entrySet()) {
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
