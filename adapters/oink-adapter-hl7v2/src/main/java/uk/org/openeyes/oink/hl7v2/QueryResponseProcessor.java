package uk.org.openeyes.oink.hl7v2;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import ca.uhn.hl7v2.model.Message;

public abstract class QueryResponseProcessor extends Hl7v2Processor {
	
	public abstract Message buildQuery(OINKRequestMessage request) throws Exception;
	
	protected boolean isSearchByIdentifierNumber(OINKRequestMessage request) {
		String idvalue = getQueryParameterValue(request, "identifier");
		return idvalue != null;
	}
	
	protected String getQueryParameterValue(OINKRequestMessage request, String key) {
		List<NameValuePair> params = URLEncodedUtils.parse(
				request.getParameters(), Charset.forName("UTF-8"));
		for (NameValuePair param : params) {
			if (param.getName().equals(key)) {
				String value = param.getValue();
				return value;
			}
		}
		return null;
	}	
	
	protected String extractIdentifierValue(OINKRequestMessage request) {
		String value = getQueryParameterValue(request, "identifier");
		if (value == null) {
			return null;
		}
		String[] split = value.split("\\|");
		if (split.length == 2) {
			String system = split[1];
			return system;
		}
		return null;
		
	}		
	
	protected String extractSystem(OINKRequestMessage request) {
		String value = getQueryParameterValue(request, "identifier");
		if (value == null) {
			return null;
		}
		String[] split = value.split("\\|");
		if (split.length == 2) {
			String system = split[0];
			return system;
		}
		return null;
		
	}	
	
	protected boolean isSearchByNHSNumber(OINKRequestMessage request) {
		String idvalue = getQueryParameterValue(request, "identifier");
		if (idvalue != null) {
			return idvalue.startsWith("NHS");
		}
		return false;
	}
	
	protected boolean isSearchByPASNumber(OINKRequestMessage request) {
		String idvalue = getQueryParameterValue(request, "identifier");
		if (idvalue != null) {
			return idvalue.startsWith("PAS");
		}
		return false;
	}
	
	protected boolean isSearchByFamilyName(OINKRequestMessage request) {
		String idvalue = getQueryParameterValue(request, "family");
		return idvalue != null;	
	}
	
	protected boolean isSearchByGivenName(OINKRequestMessage request) {
		String idvalue = getQueryParameterValue(request, "given");
		return idvalue != null;	
	}
	
	protected boolean isSearchByName(OINKRequestMessage request) {
		return isSearchByFamilyName(request) || isSearchByGivenName(request);
	}

}
