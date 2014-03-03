package uk.org.openeyes.oink.modules.silverlink.filter;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.openeyes.oink.filterchain.FilterChain;
import uk.org.openeyes.oink.filterchain.command.impl.HttpExecuteCommand;
import uk.org.openeyes.oink.filterchain.command.impl.HttpResponseToOinkResponseCommand;
import uk.org.openeyes.oink.filterchain.command.impl.OinkRequestToHttpRequestCommand;

@FilterChain(name = PatientFilterChain.FILTER_KEY)
public class PatientFilterChain extends ChainBase {
	public final static String FILTER_KEY = "silverlinkPatientFilterChain";

	@Autowired
	public PatientFilterChain(OinkRequestToHttpRequestCommand a, BuildOpenMapsUrlCommand bb,
			HttpExecuteCommand b, HttpResponseToOinkResponseCommand c) {
		super(new Command[] { a, bb, b, c });
	}
}
