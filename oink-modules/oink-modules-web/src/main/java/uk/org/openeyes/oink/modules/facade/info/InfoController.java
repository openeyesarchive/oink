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
package uk.org.openeyes.oink.modules.facade.info;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javatuples.Pair;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.modules.facade.Facade;
import uk.org.openeyes.oink.modules.facade.SimpleFacadeHandlerMapping;

@Controller
@RequestMapping(value="/")
public class InfoController {

	@Autowired
	SimpleFacadeHandlerMapping facadeMapping;

	@Autowired
	CachingConnectionFactory rabbitConnectionFactory;

	@Value("${rabbit.management.port}")
	private Integer rabbitManagementPort;

	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		
		// put date
		Date date = new Date();
		Locale l = Locale.ENGLISH;
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, l);
		String formattedDate = dateFormat.format(date);
		model.put("serverTime", formattedDate );

		// put oink version
		model.put("oinkVersion", getClass().getPackage()
				.getImplementationVersion());

		// put rabbit broker uri
		String rabbitBroker = "amqp://" + rabbitConnectionFactory.getHost()
				+ ":" + Integer.toString(rabbitConnectionFactory.getPort());

		model.put("rabbitBrokerHost", rabbitConnectionFactory.getHost());
		model.put("rabbitBrokerPort", rabbitConnectionFactory.getPort());

		// put rabbit connection status
		boolean rabbitConnectionOk = false;
		try {
			Connection c = rabbitConnectionFactory.createConnection();
			rabbitConnectionOk = c.isOpen();
			c.close();
		} catch (AmqpException e) {
			rabbitConnectionOk = false;
		}
		model.put("rabbitConnectionOk", rabbitConnectionOk);

		// put rabbit management port
		model.put("rabbitManagementPort", rabbitManagementPort);
		
		List<Facade> facades = facadeMapping.getMappedFacades();
		List<Pair<String, HttpMethod>> exposedResources = new LinkedList<>();
		for (Facade f : facades) {
			List<Pair<String, HttpMethod>> resources = f.getResources();
			for (Pair<String, HttpMethod> resource : resources) {
				String facadeBase = f.getFhirBase();
				exposedResources.add(new Pair<String, HttpMethod>(facadeBase+resource.getValue0(), resource.getValue1()));
			}
		}
		model.put("exposedResources", exposedResources);

		return new ModelAndView("info.jsp", model);
	}

}
