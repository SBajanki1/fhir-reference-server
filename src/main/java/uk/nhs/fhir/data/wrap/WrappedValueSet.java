package uk.nhs.fhir.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcept;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.valueset.FhirValueSetCompose;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.valueset.ValueSetFormatter;

public abstract class WrappedValueSet extends WrappedResource<WrappedValueSet> {

	public abstract Optional<String> getCopyright();
	public abstract void setCopyright(String copyRight);
	public abstract List<WrappedConceptMap> getConceptMaps();
	public abstract String getStatus();
	public abstract List<FhirIdentifier> getIdentifiers();
	public abstract Optional<String> getOid();
	public abstract Optional<String> getReference();
	public abstract Optional<String> getVersion();
	public abstract Optional<String> getDescription();
	public abstract Optional<String> getPublisher();
	public abstract Optional<String> getRequirements();
	public abstract Optional<Date> getDate();
	public abstract boolean hasComposeIncludeFilter();
	public abstract Optional<FhirCodeSystemConcepts> getCodeSystem();
	public abstract FhirValueSetCompose getCompose();
	public abstract List<FhirContacts> getContacts();
	public abstract Optional<Boolean> getExperimental();
	
	@Override
	public ResourceFormatter<WrappedValueSet> getDefaultViewFormatter(FhirFileRegistry otherResources) {
		return new ValueSetFormatter(this, otherResources);
	}

	@Override
	public List<FormattedOutputSpec<WrappedValueSet>> getFormatSpecs(String outputDirectory, FhirFileRegistry otherResources) {
		List<FormattedOutputSpec<WrappedValueSet>> specs = Lists.newArrayList();
		
		specs.add(new FormattedOutputSpec<WrappedValueSet>(this, new ValueSetFormatter(this, otherResources), outputDirectory, "render.html"));
		
		return specs;
	}
	
	public String getOutputFolderName() {
		return "ValueSet";
	}
	
	public void fixHtmlEntities() {
		Optional<String> copyRight = getCopyright();
	    if(copyRight.isPresent()) {
	        String updatedCopyRight = copyRight.get().replace("©", "&#169;");
	        updatedCopyRight = updatedCopyRight.replace("\\u00a9", "&#169;");
	        setCopyright(updatedCopyRight);
	    }
	}
	
	public List<FhirCodeSystemConcept> getConceptsToDisplay() {
		List<FhirCodeSystemConcept> conceptsForDisplay = Lists.newArrayList();
		
		Optional<FhirCodeSystemConcepts> inlineCodeSystem = getCodeSystem();
		if (inlineCodeSystem.isPresent()) {
			conceptsForDisplay.addAll(inlineCodeSystem.get().getConcepts());
		}
		
		FhirValueSetCompose compose2 = getCompose();
		for (FhirValueSetComposeInclude include : compose2.getIncludes()) {
			conceptsForDisplay.addAll(include.getConcepts());
		}
		for (FhirValueSetComposeInclude exclude : compose2.getExcludes()) {
			conceptsForDisplay.addAll(exclude.getConcepts());
		}
		return conceptsForDisplay;
	}
	
}
