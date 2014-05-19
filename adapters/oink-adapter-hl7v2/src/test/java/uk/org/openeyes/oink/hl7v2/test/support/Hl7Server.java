package uk.org.openeyes.oink.hl7v2.test.support;

import java.util.Map;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class Hl7Server {

	HapiContext context;
	HL7Service server;

	Message receivedMessage;
	Message returnedMessage;

	public Hl7Server(int port, boolean useTls) {
		context = new DefaultHapiContext();
		context.setValidationContext(new NoValidation());
		server = context.newServer(port, useTls);
	}

	public void setMessageHandler(String messageType, String triggerEvent,
			ReceivingApplication handler) {
		ReceivingApplicationDecorator decorator = new ReceivingApplicationDecorator(
				handler);
		server.registerApplication(messageType, triggerEvent, decorator);
	}

	public void setExceptionHandler(
			ReceivingApplicationExceptionHandler exHandler) {
		server.setExceptionHandler(exHandler);
	}

	public void start() throws InterruptedException {
		server.startAndWait();
	}

	public void stop() {
		server.stopAndWait();
	}

	public final Message getReceivedMessage() {
		return receivedMessage;
	}

	public final Message getReturnedMessage() {
		return returnedMessage;
	}

	private class ReceivingApplicationDecorator implements
			ReceivingApplication {

		ReceivingApplication child;

		public ReceivingApplicationDecorator(ReceivingApplication child) {
			this.child = child;
		}

		@Override
		public Message processMessage(Message theMessage,
				Map<String, Object> theMetadata)
				throws ReceivingApplicationException, HL7Exception {
			Hl7Server.this.setReceivedMessage(theMessage);
			Message response = child
					.processMessage(theMessage, theMetadata);
			Hl7Server.this.setReturnedMessage(response);
			return response;
		}

		@Override
		public boolean canProcess(Message theMessage) {
			return child.canProcess(theMessage);
		}

	}

	public final void setReceivedMessage(Message receivedMessage) {
		this.receivedMessage = receivedMessage;
	}

	public final void setReturnedMessage(Message returnedMessage) {
		this.returnedMessage = returnedMessage;
	}

}