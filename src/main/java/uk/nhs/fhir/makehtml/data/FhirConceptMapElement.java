package uk.nhs.fhir.makehtml.data;

import java.util.List;

import com.google.common.collect.Lists;

public class FhirConceptMapElement {

	private final String code;
	private final List<FhirConceptMapElementTarget> targets = Lists.newArrayList();
	
	public FhirConceptMapElement(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
	public void addTarget(FhirConceptMapElementTarget target) {
		targets.add(target);
	}
	
	public List<FhirConceptMapElementTarget> getTargets() {
		return targets;
	}
}
