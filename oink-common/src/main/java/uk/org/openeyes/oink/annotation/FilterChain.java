package uk.org.openeyes.oink.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.filter.ChainCatalogue;

/**
 * A custom annotation to label classes that extend {@link ChainBase}.
 * The mandatory name is used to add the FilterChain to a FilterChainCatalogue.
 * @see ChainCatalogue
 * @author Oliver Wilkie
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface FilterChain {
	
	String name();

}
