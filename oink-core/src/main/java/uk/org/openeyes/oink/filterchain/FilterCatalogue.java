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
package uk.org.openeyes.oink.filterchain;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.CatalogBase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Stores references to all {@link FilterChain} beans in the application context.
 * This class is annotated as a component and will be automatically initialised in an annotation-driven context.
 * The Catalogue serves as a {@link BeanPostProcessor} and automatically adds any {@link FilterChain} beans in the application context to itself.
 * @author Oli
 */
@Component
public class FilterCatalogue extends CatalogBase implements BeanPostProcessor,ApplicationListener<ContextRefreshedEvent> {
	
	static Logger log = Logger.getLogger(FilterCatalogue.class.getName());
	
	private int count = 0;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
			if (bean.getClass().isAnnotationPresent(FilterChain.class)) {
				FilterChain fc = bean.getClass().getAnnotation(FilterChain.class);
				String name = fc.name();
				
				log.log(Level.INFO, "Found "+bean.getClass().getName()+": adding to catalogue under key "+name);
				
				// Add to catalogue
				Command command = (Command) bean;
				addCommand(name, command);
			}
		return bean;
	}
	
	@Override
	public void addCommand(String name, Command command) {
		super.addCommand(name,command);
		count++;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		if (count == 0) {
			log.log(Level.WARNING, "No FilterChains were found.");
		}
	}
	
}
