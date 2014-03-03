package uk.org.openeyes.oink.filterchain.command;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import uk.org.openeyes.oink.filterchain.FilterChainContext;

public abstract class FilterCommand implements Command {

	@Override
	public boolean execute(Context context) throws Exception {
		return execute((FilterChainContext) context);
	}
	
	protected abstract boolean execute(FilterChainContext context) throws Exception;

}
