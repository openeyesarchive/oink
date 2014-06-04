package uk.org.openeyes.oink.infrastructure;

public class SimpleAdapterStatusService implements AdapterStatusService {

	@Override
	public boolean isAlive() {
		return true;
	}

}
