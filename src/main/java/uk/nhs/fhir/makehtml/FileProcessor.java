package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.makehtml.data.FhirRelease;
import uk.nhs.fhir.makehtml.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.html.RendererError;
import uk.nhs.fhir.util.HAPIUtils;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
    
	public void processFile(String outPath, String newBaseURL, File folder, File thisFile) throws Exception {
		if (thisFile.isFile()) {
			
		    String inFilePath = thisFile.getPath();
		    LOG.info("\n\n=========================================\nProcessing file: " + inFilePath + "\n=========================================");

		    IBaseResource resource = parseFile(thisFile);
		    
			WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(resource);
			
			if (wrappedResource instanceof WrappedStructureDefinition
			  && ((WrappedStructureDefinition)wrappedResource).missingSnapshot()) {
				RendererError.handle(RendererError.Key.RESOURCE_WITHOUT_SNAPSHOT, "Resource at " + thisFile.getAbsolutePath() + " doesn't have a snapshot element");
				return;
			}

		    wrappedResource.saveAugmentedResource(thisFile, wrappedResource, outPath, newBaseURL);
		    wrappedResource.saveFormattedOutputs(thisFile, outPath, newBaseURL);
		}
	}

	private IBaseResource parseFile(File thisFile) throws IOException {
		
		List<FhirVersion> successfullyParsedVersions = Lists.newArrayList();
		
		FhirVersion[] versionsToTry = new FhirVersion[]{FhirVersion.DSTU2, FhirVersion.STU3};
		
		IBaseResource resource;
		for (FhirVersion version : versionsToTry) {
			resource = tryParse(thisFile, version, successfullyParsedVersions);
			if (resource != null) {
				return resource;
			}
		}
		
		// Couldn't confirm that any was correct. If we only successfully parsed a single version, use that.
		if (successfullyParsedVersions.size() == 1) {
			FhirVersion onlyParsableVersion = successfullyParsedVersions.get(0);
			return parseFile(HAPIUtils.xmlParser(onlyParsableVersion), thisFile, onlyParsableVersion);
		}
		
		// Use directory structure as a final backup. This should hopefully be unnecessary once URLs consistently include Fhir Version.
		for (String pathPart : thisFile.getAbsolutePath().split("/")) {
			for (FhirVersion versionToTry : versionsToTry) {
				if (pathPart.equals(versionToTry.toString())) {
					IParser xmlParser = HAPIUtils.xmlParser(versionToTry);
					try (FileReader fr = new FileReader(thisFile)) {
						return xmlParser.parseResource(fr);
					} catch (IOException | DataFormatException e) {
						throw new IllegalStateException(e);
					}
				}
			}
		}
		
		throw new IllegalStateException("Couldn't work out appropriate FHIR version for " + thisFile.getAbsolutePath() + ", successfully parsed for " 
				+ String.join(", ", successfullyParsedVersions.stream().map(version -> version.toString()).collect(Collectors.toList())));
	}
	
	private IBaseResource tryParse(File thisFile, FhirVersion versionToTry, List<FhirVersion> successfullyParsedVersions) {
		IParser xmlParser = HAPIUtils.xmlParser(versionToTry);
		IBaseResource resource = parseFile(xmlParser, thisFile, versionToTry);
		
		if (resource != null) {
			FhirVersion versionFromResource = getResourceVersion(resource);
			
			if (versionFromResource == null
			  || versionFromResource.equals(versionToTry)) {
				successfullyParsedVersions.add(versionToTry);
			}
			
			if (versionToTry.equals(versionFromResource)) {
				return resource;
			}
		}
		
		return null;
	}

	private FhirVersion getResourceVersion(IBaseResource resource) {
		
		if (resource instanceof ca.uhn.fhir.model.dstu2.resource.StructureDefinition) {
			StructureDefinition dstu2StructureDefinition = (ca.uhn.fhir.model.dstu2.resource.StructureDefinition)resource;
			
			String url = dstu2StructureDefinition.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			String fhirVersion = dstu2StructureDefinition.getFhirVersion();
			if (!Strings.isNullOrEmpty(fhirVersion)) {
				return FhirRelease.forString(fhirVersion).getVersion();
			}
			
			return null;
			
		} else if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
			
			org.hl7.fhir.dstu3.model.StructureDefinition stu3StructureDefinition = (org.hl7.fhir.dstu3.model.StructureDefinition)resource;
			
			String url = stu3StructureDefinition.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return FhirRelease.forString(stu3StructureDefinition.getFhirVersion()).getVersion();
			
		} else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ValueSet) {
			
			ca.uhn.fhir.model.dstu2.resource.ValueSet dstu2ValueSet = (ca.uhn.fhir.model.dstu2.resource.ValueSet)resource;
			
			String url = dstu2ValueSet.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return null;
			
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
			
			org.hl7.fhir.dstu3.model.ValueSet stu3ValueSet = (org.hl7.fhir.dstu3.model.ValueSet)resource;
			
			String url = stu3ValueSet.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return null;
			
		} else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.OperationDefinition) {
			
			ca.uhn.fhir.model.dstu2.resource.OperationDefinition dstu2OperationDefinition = (ca.uhn.fhir.model.dstu2.resource.OperationDefinition)resource;
			
			String url = dstu2OperationDefinition.getUrl();
			if (!Strings.isNullOrEmpty(url)) {
				FhirVersion version = fromResourceUrl(url);
				
				if (version != null) {
					return version;
				}
			}
			
			return null;
		} else {
			throw new IllegalStateException("Need to support class " + resource.getClass().getCanonicalName());
		}
	}
	
	private FhirVersion fromResourceUrl(String urlString) {
		//e.g. https://fhir.nhs.uk/STU3/StructureDefinition/extension-optoutsource-1
		
		try {
			URL url = new URL(urlString);
			String path = url.getPath();
			
			if (path.startsWith("/STU3")) {
				return FhirVersion.STU3;
			} else if (path.startsWith("/DSTU2")) {
				return FhirVersion.DSTU2;
			} else {
				RendererError.handle(RendererError.Key.RESOURCE_URL_WITHOUT_FHIR_VERSION, "URL " + urlString + " doesn't have a FHIR version prefix");
				return null;
			}
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	private IBaseResource parseFile(IParser xmlParser, File thisFile, FhirVersion expectedVersion) {
		try (FileReader fr = new FileReader(thisFile)) {
			return xmlParser.parseResource(fr);
		} catch (IOException | DataFormatException e) {}
		
		return null;
	}
}
