package uk.org.openeyes.oink.filterchain.command.impl;

import org.apache.commons.chain.Command;
import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.filterchain.FilterChainContext;
import uk.org.openeyes.oink.filterchain.command.FilterCommand;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

@Component
public class HttpExecuteCommand extends FilterCommand {

	@Override
	protected boolean execute(FilterChainContext context) throws Exception {
		HttpRequest request = context.getHttpRequest();
		HttpResponse response = request.execute();
		context.setHttpResponse(response);
		return Command.CONTINUE_PROCESSING;
	}

}
