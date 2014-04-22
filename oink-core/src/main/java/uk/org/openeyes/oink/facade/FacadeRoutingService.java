package uk.org.openeyes.oink.facade;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.rabbit.RabbitRoute;

public class FacadeRoutingService implements RoutingService {
	
	private HttpMapper<RabbitRoute> mappings;
	
	private final String replyRoutingKey;
	
	public FacadeRoutingService(HttpMapper<RabbitRoute> mappings, String replyRouting) {
		this.replyRoutingKey = replyRouting;
		this.mappings = mappings;
	}

	@Override
	public RabbitRoute getRouting(String path, HttpMethod method) {
		return mappings.get(path, method);
	}
	
	public void setMappings(HttpMapper<RabbitRoute> mappings) {
		this.mappings = mappings;
	}

	@Override
	public String getReplyRoutingKey(String path, HttpMethod method) {
		return replyRoutingKey;
	}

}
