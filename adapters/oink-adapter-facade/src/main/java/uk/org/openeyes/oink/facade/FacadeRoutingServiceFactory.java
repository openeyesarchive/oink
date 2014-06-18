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
package uk.org.openeyes.oink.facade;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.common.HttpMapperEntry;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.rabbit.RabbitRoute;

/**
 * 
 * Builds a {@link FacadeRoutingService} using information in the adapters properties file.
 * 
 * facade.mapping.1.service    (Optional) Used to route to different end-systems
 * facade.mapping.1.resource   (Optional) E.g. Patient, Practitioner or Organization
 * facade.mapping.1.routingKey (Mandatory) The rabbit routing key
 * facade.mapping.1.method	   (Optional) GET, POST or PUT etc
 * facade.mapping.2.service
 * facade.mapping.2.resource
 *   ..   ...    ..  ..
 * 
 * @author Oliver Wilkie
 */
public class FacadeRoutingServiceFactory {

	private static final Logger log = LoggerFactory
			.getLogger(FacadeRoutingServiceFactory.class);

	private static final String MAPPING_KEY = "facade.mapping";
	private static final String MAPPING_DEFAULT_EXCHANGE_KEY = "rabbit.defaultExchange";
	private static final String MAPPING_DEFAULT_REPLY_ROUTING_KEY = "rabbit.responseRoutingKey";

	private Properties adapterProperties;

	public FacadeRoutingServiceFactory(Properties props) {
		this.adapterProperties = props;
	}

	public FacadeRoutingService createInstance() {
		HttpMapper<RabbitRoute> mappings;
		String defaultReplyRoutingKey;

		log.debug("Using props: "+adapterProperties.toString());
		
		Configuration config = ConfigurationConverter
				.getConfiguration(adapterProperties);
		
		String defaultExchange = config.getString(MAPPING_DEFAULT_EXCHANGE_KEY);
		defaultReplyRoutingKey = config
				.getString(MAPPING_DEFAULT_REPLY_ROUTING_KEY);

		Configuration facadeConfig = config.subset(MAPPING_KEY);

		int numOfMappings = getNumberOfMappingsGiven(facadeConfig);

		List<HttpMapperEntry<RabbitRoute>> entries = new LinkedList<HttpMapperEntry<RabbitRoute>>();
		for (int i = 1; i <= numOfMappings; i++) {
			Configuration mappingCfg = getMappingsDetailsForIndex(i,
					facadeConfig);

			String service = mappingCfg.getString("service");
			if (service != null && !service.equals("*")
					&& isReservedResourceWord(service)) {
				throw new InvalidParameterException(
						"A service name cannot be a Resource name");
			}

			String resource = mappingCfg.getString("resource");
			if (resource != null && !resource.equals("*")
					&& !isReservedResourceWord(resource)) {
				throw new InvalidParameterException(
						"A resource must be a recognised Resource name e.g. Patient  (case-sensitive)");
			}

			String method = mappingCfg.getString("method");
			HttpMethod methodEnum;
			if (method != null && !method.equals("*")
					&& !isValidHttpMethod(method)) {
				throw new InvalidParameterException(
						"A resource must be a recognised method verb e.g. POST,GET,PUT  (case-sensitive)");
			} else if (method == null || method.equals("*")) {
				methodEnum = HttpMethod.ANY;
			} else {
				methodEnum = HttpMethod.valueOf(method);
			}

			String routingKey = mappingCfg.getString("routingKey");
			if (routingKey == null || routingKey.isEmpty()) {
				throw new InvalidParameterException(
						"A routingKey must be given for every mapping entry");
			}

			StringBuilder uri = new StringBuilder();
			if (!(service == null || service.isEmpty())) {
				uri.append(service);
				uri.append("/");
			}

			if (!(resource == null || resource.isEmpty())) {
				uri.append(resource);
			}

			uri.append("*");

			RabbitRoute route = new RabbitRoute(routingKey, defaultExchange);

			HttpMapperEntry<RabbitRoute> entry = new HttpMapperEntry<RabbitRoute>(
					uri.toString(), methodEnum, route);

			entries.add(entry);

		}

		mappings = new HttpMapper<RabbitRoute>(entries);
		return new FacadeRoutingService(mappings, defaultReplyRoutingKey);
	}

	private int getNumberOfMappingsGiven(Configuration facadeConfig) {
		if (facadeConfig.isEmpty()) {
			log.error("No mapping entries are in the program's config file. The facade will not map any requests!");
			return 0;
		}

		int numOfMappings = 0;

		while (facadeConfig.getKeys(++numOfMappings + ".").hasNext()) {
			numOfMappings++;
		}

		log.info("Found " + numOfMappings + " mapping entries");

		return numOfMappings;
	}

	private Configuration getMappingsDetailsForIndex(int i,
			Configuration facadeConfig) {
		return facadeConfig.subset(Integer.toString(i));
	}

	private static boolean isReservedResourceWord(String s) {
		try {
			return ResourceType.valueOf(s) != null;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static boolean isValidHttpMethod(String m) {
		try {
			return HttpMethod.valueOf(m) != null;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

}
