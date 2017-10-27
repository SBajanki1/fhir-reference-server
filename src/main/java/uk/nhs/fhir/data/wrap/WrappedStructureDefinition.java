package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.structdef.FhirMapping;

public abstract class WrappedStructureDefinition extends WrappedResource<WrappedStructureDefinition> {
	
	public abstract void checkUnimplementedFeatures();

	public abstract String getName();
	public abstract String getKind();
	public abstract String getStatus();
	public abstract Boolean getAbstract();

	public abstract Optional<String> getConstrainedType();
	public abstract String getBase();
	public abstract Optional<String> getDisplay();
	public abstract Optional<String> getPublisher();
	public abstract Optional<Date> getDate();
	public abstract Optional<String> getCopyright();
	public abstract Optional<String> getFhirVersion();
	public abstract Optional<String> getContextType();
	public abstract Optional<String> getDescription();

	public abstract List<FhirContacts> getContacts();
	public abstract List<String> getUseContexts();
	public abstract List<FhirMapping> getMappings();
	public abstract List<String> getUseLocationContexts();
	
	public abstract ExtensionType getExtensionType();

	public abstract boolean missingSnapshot();
	
	protected abstract void setCopyright(String updatedCopyRight);

	public abstract FhirCardinality getRootCardinality();
	public abstract Optional<String> getDifferentialRootDescription();
	public abstract String getDifferentialRootDefinition();
	public abstract Optional<String> extensionBaseTypeDesc();

	public boolean isExtension() {
		if (getConstrainedType().isPresent()) {
			return getConstrainedType().get().equals("Extension");
		} else {
			throw new IllegalStateException("Not sure whether this is an extension - no constrained type present");
		}
	}
	
	public void fixHtmlEntities() {
		Optional<String> copyRight = getCopyright();
	    if(copyRight.isPresent()) {
	        String updatedCopyRight = copyRight.get().replace("©", "&#169;");
	        updatedCopyRight = updatedCopyRight.replace("\\u00a9", "&#169;");
	        setCopyright(updatedCopyRight);
	    }
	}
	
	public ResourceMetadata getMetadataImpl(File source) {
		String name = getName();
		boolean isExtension = isExtension();
		
		String baseType;
    	String extensionCardinality;
    	List<String> extensionContexts;
    	String extensionDescription;
		if (isExtension) {
			// Extra metadata for extensions
			extensionCardinality = getRootCardinality().toString();
			extensionContexts = getUseContexts();
    		
			extensionDescription = getDifferentialRootDescription().orElse(getDifferentialRootDefinition());
    		
			baseType = extensionBaseTypeDesc().get();
		} else {
			extensionCardinality = null;
    		extensionContexts = new ArrayList<String>();
			extensionDescription = null;
    		
    		baseType = getConstrainedType().get();
		}
		
		String url = getUrl().get();
        String resourceID = getIdFromUrl().orElse(name);
        String displayGroup = baseType;
        VersionNumber versionNo = new VersionNumber(getVersion().get());
        String status = getStatus();
        
        return new ResourceMetadata(name, source, ResourceType.STRUCTUREDEFINITION,
        		isExtension, Optional.of(baseType), displayGroup, false,
				resourceID, versionNo, status, null, extensionCardinality,
				extensionContexts, extensionDescription, getImplicitFhirVersion(), url);
	}
	
	@Override
	public ResourceType getResourceType() {
		return ResourceType.STRUCTUREDEFINITION;
	}
}
