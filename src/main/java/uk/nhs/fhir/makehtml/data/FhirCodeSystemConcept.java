package uk.nhs.fhir.makehtml.data;

import java.util.Optional;

import com.google.common.base.Preconditions;

public class FhirCodeSystemConcept {

	private final String code;
	private final Optional<String> description;
	private final Optional<String> definition;

	public FhirCodeSystemConcept(String code, String description, String definition) {
		Preconditions.checkNotNull(code);
		
		this.code = code;
		this.description = Optional.ofNullable(description);
		this.definition = Optional.ofNullable(definition);
	}
	
	public Optional<String> getDescription() {
		return description;
	}

	public Optional<String> getDefinition() {
		return definition;
	}

	public String getCode() {
		return code;
	}
	
}
