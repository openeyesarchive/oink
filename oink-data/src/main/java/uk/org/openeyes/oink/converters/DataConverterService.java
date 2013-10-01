package uk.org.openeyes.oink.converters;

import javax.inject.Inject;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class DataConverterService implements ConversionService, ApplicationListener<ContextRefreshedEvent> {

	@Inject
    private ConfigurableListableBeanFactory beanFactory;
	
	private GenericConversionService service;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		service = new GenericConversionService();

		String[] beanNames = beanFactory.getBeanNamesForType(Converter.class);
        for (String beanName : beanNames) {
            
            // Get the bean and add to the super class
            service.addConverter(beanFactory.getBean(beanName, Converter.class));

            /*
            BeanDefinition converterBeanDefinition = beanFactory.getBeanDefinition(beanName);
            try {
                Class<?> converterClass = Class.forName(converterBeanDefinition.getBeanClassName());
                
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }*/
        }
	}

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return service.canConvert(sourceType, targetType);
	}

	@Override
	public boolean canConvert(TypeDescriptor sourceType,
			TypeDescriptor targetType) {
		return service.canConvert(sourceType, targetType);
	}

	@Override
	public <T> T convert(Object source, Class<T> targetType) {
		return service.convert(source, targetType);
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType,
			TypeDescriptor targetType) {
		return service.convert(source, sourceType, targetType);
	}
}
