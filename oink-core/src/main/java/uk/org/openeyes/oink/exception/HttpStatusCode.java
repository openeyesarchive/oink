package uk.org.openeyes.oink.exception;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) // Make this annotation accessible at runtime via reflection.
public @interface HttpStatusCode {

	int value();
}
