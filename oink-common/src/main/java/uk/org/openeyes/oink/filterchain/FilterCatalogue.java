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
