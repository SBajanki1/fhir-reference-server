package uk.nhs.fhir.makehtml.prep;

import ca.uhn.fhir.model.dstu2.resource.BaseResource;

public interface ResourcePreparer<T extends BaseResource> {
	public void prepare(T resource, String newBaseURL);
}
