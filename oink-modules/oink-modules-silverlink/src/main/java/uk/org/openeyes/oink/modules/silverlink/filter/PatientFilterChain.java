package uk.org.openeyes.oink.modules.silverlink.filter;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;

import uk.org.openeyes.oink.annotation.FilterChain;

@FilterChain(name = PatientFilterChain.FILTER_KEY)
public class PatientFilterChain extends ChainBase {
	public final static String FILTER_KEY = "silverlinkPatientFilterChain";
	
	public PatientFilterChain() {
		super(new Command[] {new TestCommand()});
	}
	
	private static class TestCommand implements Command {

		@Override
		public boolean execute(Context arg0) throws Exception {
			System.out.println("Hello World! PatientFilterChain.");
			return Command.CONTINUE_PROCESSING;
		}
		
	}

//	public PatientFilterChain(OinkRequestToHttpRequestCommand a,
//			HttpRequestToServletRequestCommand b,
//			ServletRequestToOpenMapsCommand c,
//			OpenMapsToServletResponseCommand d,
//			ServletResponseToHttpResponseCommand e,
//			HttpResponseToOinkResponseCommand f) {
//		super(new Command[] { a, b, c, d, e, f });
//	}
}
