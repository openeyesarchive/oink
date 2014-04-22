package uk.org.openeyes.oink.rabbit;

import uk.org.openeyes.oink.exception.HttpStatusCode;
import uk.org.openeyes.oink.exception.OinkException;

@HttpStatusCode(404)
public class NoRabbitMappingFoundException extends OinkException {

}
