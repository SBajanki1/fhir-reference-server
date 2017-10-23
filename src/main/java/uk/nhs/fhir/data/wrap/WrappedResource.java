package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.data.metadata.ArtefactType;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.SupportingArtefact;
import uk.nhs.fhir.data.metadata.SupportingArtefact.OrderByWeight;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ConceptMap;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2OperationDefinition;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2StructureDefinition;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ValueSet;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3CodeSystem;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ConceptMap;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3OperationDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3StructureDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ValueSet;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.StringUtil;

public abstract class WrappedResource<T extends WrappedResource<T>> {

    private static final Logger LOG = LoggerFactory.getLogger(WrappedResource.class.getName());
	
	public abstract IBaseResource getWrappedResource();
	public abstract IBaseMetaType getSourceMeta();
	public abstract FhirVersion getImplicitFhirVersion();
	public abstract Optional<String> getUrl();
	public abstract void setUrl(String url);
	
	// Name as used in the resource's URL
	public abstract String getName();
	
	// Update any fields which may need entities escaping
	public abstract void fixHtmlEntities();
	
	public abstract void addHumanReadableText(String textSection);

	public Class<? extends IBaseResource> getFhirClass() {
		return getWrappedResource().getClass();
	}

	public String getOutputFolderName() {
		return getResourceType().getDisplayName();
	}
	
	protected abstract ResourceMetadata getMetadataImpl(File source);
	
	public abstract ResourceType getResourceType();
    
    public ResourceMetadata getMetadata(File source) {
    	ResourceMetadata resourceMetadata = getMetadataImpl(source);
    	
    	ArrayList<SupportingArtefact> artefacts = getArtefacts(source);

		Collections.sort(artefacts, new OrderByWeight());
		
		resourceMetadata.setArtefacts(artefacts);
		
		return resourceMetadata;
	}
    
	private ArrayList<SupportingArtefact> getArtefacts(File source) {
		ArrayList<SupportingArtefact> artefacts = new ArrayList<>();
		
		String resourceFilename = FileLoader.removeFileExtension(source.getName());
		File dir = new File(source.getParent());
		File artefactDir = new File(dir.getAbsolutePath() + "/" + resourceFilename);
		
		LOG.debug("Looking for artefacts in directory:" + artefactDir.getAbsolutePath());
		
		if (artefactDir.exists() 
		  && artefactDir.isDirectory()) { 
			// Now, loop through and find any artefact files
            File[] fileList = artefactDir.listFiles();
            if (fileList != null) {
    	        for (File thisFile : fileList) {
    	        	// Add this to our list of artefacts (if we can identify what it is!
    	        	ArtefactType type = ArtefactType.getFromFilename(getResourceType(), thisFile.getName());
    	        	if (type != null) {
    	        		SupportingArtefact artefact = new SupportingArtefact(thisFile, type); 
    	        		artefacts.add(artefact);
    	        	}
    	        }
            }
		}

		return artefacts;
	}
    
	public boolean isDstu2() {
		return getImplicitFhirVersion().equals(FhirVersion.DSTU2);
	};
	public boolean isStu3() {
		return getImplicitFhirVersion().equals(FhirVersion.STU3);
	};
	
	public Optional<String> getIdFromUrl() {
		Optional<String> url = getUrl();
		
		if (url.isPresent()
		  && url.get().contains("/")
		  && !url.get().endsWith("/")) {
			String[] urlParts = url.get().split("/"); 
			String lastPart = urlParts[urlParts.length-1];
			return Optional.of(lastPart);
		}
		
		return Optional.empty();
	}
	
	private Optional<IBaseMetaType> getMeta() {
		IBaseMetaType metaInfo = getSourceMeta();
		if (!metaInfo.isEmpty()) {
			return Optional.of(metaInfo);
		} else {
			return Optional.empty();
		}
	}

	public Optional<String> getVersionId() {
		Optional<IBaseMetaType> metaInfo = getMeta();
		if (metaInfo.isPresent()) {
			return Optional.ofNullable(metaInfo.get().getVersionId());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<String> getLastUpdated() {
		Optional<IBaseMetaType> metaInfo = getMeta();
		if (metaInfo.isPresent()) {
			Date lastUpdated = metaInfo.get().getLastUpdated();
			if (lastUpdated != null) {
				return Optional.of(StringUtil.dateToString(lastUpdated));
			}
		}
		
		return Optional.empty();
	}
	
	public static WrappedResource<?> fromBaseResource(IBaseResource resource) {
		if (resource instanceof ca.uhn.fhir.model.dstu2.resource.StructureDefinition) {
			return new WrappedDstu2StructureDefinition((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
			return new WrappedStu3StructureDefinition((org.hl7.fhir.dstu3.model.StructureDefinition)resource);
		} 
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ValueSet) {
			return new WrappedDstu2ValueSet((ca.uhn.fhir.model.dstu2.resource.ValueSet)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
			return new WrappedStu3ValueSet((org.hl7.fhir.dstu3.model.ValueSet)resource);
		} 
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.OperationDefinition) {
			return new WrappedDstu2OperationDefinition((ca.uhn.fhir.model.dstu2.resource.OperationDefinition)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.OperationDefinition) {
			return new WrappedStu3OperationDefinition((org.hl7.fhir.dstu3.model.OperationDefinition)resource);
		}
		
		else if (resource instanceof org.hl7.fhir.dstu3.model.CodeSystem) {
			return new WrappedStu3CodeSystem((org.hl7.fhir.dstu3.model.CodeSystem)resource);
		}
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ConceptMap) {
			return new WrappedDstu2ConceptMap((ca.uhn.fhir.model.dstu2.resource.ConceptMap)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ConceptMap) {
			return new WrappedStu3ConceptMap((org.hl7.fhir.dstu3.model.ConceptMap)resource);
		}
		
		else {
			throw new IllegalStateException("Couldn't make a WrappedResource for " + resource.getClass().getCanonicalName());
		}
	}
	
	public IParser newXmlParser() {
		return FhirContexts.xmlParser(getImplicitFhirVersion());
	}
}
