package uk.org.openeyes.oink.filterchain.command.impl;

import java.io.IOException;

import org.apache.commons.chain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.filterchain.FilterChainContext;
import uk.org.openeyes.oink.filterchain.command.FilterCommand;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

@Component
public class HttpExecuteCommand extends FilterCommand {

	private static final Logger logger = LoggerFactory
			.getLogger(HttpExecuteCommand.class);

	@Override
	protected boolean execute(FilterChainContext context) throws Exception {
		try {
			HttpRequest request = context.getHttpRequest();
			logger.debug("Executing request:" + request.getUrl().toString() + " method:" + request.getRequestMethod());
			HttpResponse response = request.execute();
			context.setHttpResponse(response);
			return Command.CONTINUE_PROCESSING;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

}
