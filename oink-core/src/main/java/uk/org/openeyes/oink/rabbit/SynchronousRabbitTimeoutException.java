package uk.org.openeyes.oink.rabbit;

import uk.org.openeyes.oink.exception.HttpStatusCode;
import uk.org.openeyes.oink.exception.OinkException;

@HttpStatusCode(504)
public class SynchronousRabbitTimeoutException extends OinkException {

}
