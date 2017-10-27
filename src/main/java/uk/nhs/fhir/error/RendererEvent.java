package uk.nhs.fhir.error;

import java.io.File;
import java.util.Optional;

import uk.nhs.fhir.data.wrap.WrappedResource;

public class RendererEvent {

	private final Optional<String> message;
	private final File sourceFile;
	private final WrappedResource<?> resource;
	private final Optional<Exception> error;
	private final EventType eventType;
	
	public static RendererEvent warning(String message, File sourceFile, WrappedResource<?> resource) {
		return warning(message, sourceFile, resource, null);
	}
	
	public static RendererEvent warning(String message, File sourceFile, WrappedResource<?> resource, Exception exception) {
		return new RendererEvent(Optional.of(message), sourceFile, resource, exception, EventType.WARNING);
	}
	
	public static RendererEvent error(Optional<String> message, File sourceFile, WrappedResource<?> resource, Optional<Exception> error) {
		return new RendererEvent(message, sourceFile, resource, error.orElse(null), EventType.ERROR);
	}
	
	RendererEvent(Optional<String> message, File sourceFile, WrappedResource<?> resource, Exception error, EventType eventType) {
		this.message = message;
		this.sourceFile = sourceFile;
		this.resource = resource;
		this.error = Optional.ofNullable(error);
		this.eventType = eventType;
	}
	
	public Optional<String> getMessage() {
		return message;
	}

	public File getSourceFile() {
		return sourceFile;
	}
	
	public WrappedResource<?> getResource() {
		return resource;
	}
	
	public Optional<Exception> getError() {
		return error;
	}

	public EventType getEventType() {
		return eventType;
	}
}
