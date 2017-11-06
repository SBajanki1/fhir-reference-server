package uk.nhs.fhir.makehtml;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.error.FhirErrorHandler;

public class LoggingErrorHandler implements FhirErrorHandler {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingErrorHandler.class);

	private boolean foundErrors = false;
	private boolean foundWarnings = false;
	
	@Override
	public void ignore(String info, Optional<Exception> throwable) {
		// nothing
	}

	@Override
	public void log(String info, Optional<Exception> throwable) {
		foundWarnings = true;
		
		LOG.error(info);
		if (throwable.isPresent()) {
			throwable.get().printStackTrace();
		}
	}

	@Override
	public void error(Optional<String> info, Optional<Exception> throwable) {
		foundErrors = true;
		
		if (throwable.isPresent() && info.isPresent()) {
			throw new IllegalStateException(info.get(), throwable.get());
		} else if (info.isPresent()) {
			throw new IllegalStateException(info.get());
		} else if (throwable.isPresent()) {
			throw new IllegalStateException(throwable.get());
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void displayOutstandingEvents() {
		// all information already shown - nothing to do
	}

	@Override
	public boolean foundErrors() {
		return foundErrors;
	}

	@Override
	public boolean foundWarnings() {
		return foundWarnings;
	}

}
