package uk.org.openeyes.oink.facade;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.rabbit.RabbitRoute;

/**
 * An implementation of a {@link RoutingService} that handles mapping incoming requests to
 * an outgoing RabbitQueue. It also has an incoming routing key for responses.
 * 
 * A private {@link HttpMapper} does the actual routing work.
 * 
 * @author Oliver Wilkie
 */
public class FacadeRoutingService implements RoutingService {

	private final HttpMapper<RabbitRoute> mappings;

	private final String replyRoutingKey;

	public FacadeRoutingService(HttpMapper<RabbitRoute> mappings,
			String replyRouting) {
		this.replyRoutingKey = replyRouting;
		this.mappings = mappings;
	}

	@Override
	public RabbitRoute getRouting(String path, HttpMethod method) {
		return mappings.get(path, method);
	}

	@Override
	public String getReplyRoutingKey(String path, HttpMethod method) {
		return replyRoutingKey;
	}

}
