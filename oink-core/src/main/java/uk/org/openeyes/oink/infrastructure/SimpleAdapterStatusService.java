package uk.org.openeyes.oink.infrastructure;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;

public class SimpleAdapterStatusService implements AdapterStatusService, CamelContextAware {

	private CamelContext camelContext;
	
	@Override
	public boolean isAlive() {
		if (camelContext == null) {
			return false;
		} else {
			return camelContext.getStatus().isStarted();
		}
	}

	@Override
	public CamelContext getCamelContext() {
		return camelContext;
	}

	@Override
	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}

}
