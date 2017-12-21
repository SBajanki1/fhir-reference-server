package uk.nhs.fhir.error;

import java.util.Optional;

import uk.nhs.fhir.makehtml.RendererEventType;
import uk.nhs.fhir.makehtml.RendererEventResponse;
import uk.nhs.fhir.makehtml.RendererEventConfig;

public abstract class AbstractEventHandler implements EventHandler {
	
	public void event(RendererEventType errorType, String message) {
		event(errorType, message, Optional.empty());
	}
	
	public void event(RendererEventType errorType, String message, Optional<Exception> error) {
		RendererEventResponse responseType = RendererEventConfig.getResponse(errorType);
		
		switch (responseType) {
			case IGNORE:
				ignore(message, error);
				break;
			case LOG_WARNING:
				log(message, error);
				break;
			case THROW:
				error(Optional.of(message), error);
				break;
			default:
				throw new IllegalStateException("Unexpected event type: " + responseType.toString());
		}
	}

}
