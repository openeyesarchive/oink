package uk.org.openeyes.oink.filter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.CatalogBase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.annotation.FilterChain;

/**
 * Stores references to all {@link FilterChain} beans in the application context.
 * This class is annotated as a component and will be automatically initialised in an annotation-driven context.
 * The Catalogue serves as a {@link BeanPostProcessor} and automatically adds any {@link FilterChain} beans in the application context to itself.
 * @author Oli
 */
@Component
public class ChainCatalogue extends CatalogBase implements BeanPostProcessor,ApplicationListener<ContextRefreshedEvent> {
	
	static Logger log = Logger.getLogger(ChainCatalogue.class.getName());
	
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
