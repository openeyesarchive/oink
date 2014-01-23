package uk.org.openeyes.oink.messaging;

/**
 * Simple POJO encapsulating a RabbitMQ routing key and exchange
 * @author Oliver Wilkie
 */
public class RabbitRoute {
	
	private String routingKey;
	private String exchange;
	
	public RabbitRoute(String routingKey, String exchange) {
		this.routingKey = routingKey;
		this.exchange = exchange;
	}
	
	public String getRoutingKey() {
		return routingKey;
	}
	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RabbitRoute other = (RabbitRoute) obj;
		if (exchange == null) {
			if (other.exchange != null)
				return false;
		} else if (!exchange.equals(other.exchange))
			return false;
		if (routingKey == null) {
			if (other.routingKey != null)
				return false;
		} else if (!routingKey.equals(other.routingKey))
			return false;
		return true;
	}

}
