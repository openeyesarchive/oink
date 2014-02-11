package uk.org.openeyes.oink.modules.silverlink;

import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;


/**
 * POJO handler that receives {@link OINKRequestMessage} for Silverlink data and sends back an {@link OINKResponseMessage}.  Main application
 * Silverlink logic starts here. The handler finds the Silverlink service able to handle the OINKRequestMessage and passes it to the service for processing.
 * 
 * Option 1: Handler has a list of Services. Asks each service in turn if it can handle the request.
 * Option 2: Handler has fixed references to services.
 * 
 * @author Oliver Wilkie
 *
 */
public class SilverlinkHandler {

	private SilverlinkPatientService patientService;
	private SilverlinkAppointmentService appointmentService;
	
	public SilverlinkHandler(SilverlinkPatientService patientService,
			SilverlinkAppointmentService appointmentService) {
		this.patientService = patientService;
		this.appointmentService = appointmentService;
	}	
	
	/**
	 * Entry method for handler
	 * @param request the incoming request message
	 * @return the returned response message
	 */
	public OINKResponseMessage handle(OINKRequestMessage request) {
		OINKResponseMessage.Builder builder = new OINKResponseMessage.Builder();
		String message = "Hello World!! This text message was generated by the SilverlinkHandler and returned over a Rabbit broker to the OINK Facade.";
		builder.setBody(message).setHTTPStatus(HttpStatus.OK);
		return builder.build();
	}

}
