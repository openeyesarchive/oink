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
package uk.org.openeyes.oink.modules.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * 
 * Routes incoming requests to the correct facade.
 * 
 * If facade F has a service name and resources r1, r2
 * http://domain/<serviceName>/r1
 * http://domain/<serviceName>/r2
 * Will map to Facade
 * 
 * If facade F has no service name and resource r1, r2
 * http://domain/r1
 * Will map to Facade
 * 
 * Note 
 * 
 * @author Oliver Wilkie
 */
public class SimpleFacadeHandlerMapping extends SimpleUrlHandlerMapping {
	
	private final static Logger log = LoggerFactory.getLogger(SimpleFacadeHandlerMapping.class);

	private final List<Facade> facades;

	public SimpleFacadeHandlerMapping(List<Facade> facades) {
		super();
		this.facades = facades;
		Map<String, Object> mappings = new HashMap<String, Object>();
		for (Facade facade : facades) {
			StringBuilder facadeBaseSb = new StringBuilder();
			facadeBaseSb.append(facade.getFhirBase());
			facadeBaseSb.append("**");
			if (mappings.containsKey(facadeBaseSb.toString())) {
				logger.error("Tried to register a Facade but there is already a Facade registered with the same mapping");
			}
			mappings.put(facadeBaseSb.toString(), facade);
		}
		setUrlMap(mappings);
	}

	public List<Facade> getMappedFacades() {
		return facades;
	}

}
