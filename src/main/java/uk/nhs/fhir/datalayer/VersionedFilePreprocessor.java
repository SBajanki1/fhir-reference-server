package uk.nhs.fhir.datalayer;

import static uk.nhs.fhir.datalayer.DataLoaderMessages.addMessage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.IResourceHelper;
import uk.nhs.fhir.resourcehandlers.ResourceHelperFactory;
import uk.nhs.fhir.util.DateUtils;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FileLoader;

public class VersionedFilePreprocessor {
	
	private static final Logger LOG = Logger.getLogger(VersionedFilePreprocessor.class.getName());
	private static String fileExtension = FhirServerProperties.getProperty("fileExtension");
	private static final FilenameFilter XML_FILE_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(fileExtension);
        }
    };

	protected static void copyFHIRResourcesIntoVersionedDirectory(FHIRVersion fhirVersion, ResourceType resourceType) throws IOException {
		logStart(fhirVersion, resourceType);
		
		String outputDirectory = ensureVersionedFolderExists(fhirVersion, resourceType);
		
        String resourcePath = resourceType.getFilesystemPath(fhirVersion);
		List<File> fileList = findXmlFiles(resourcePath);
        
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.fine("Pre-processing " + resourceType + " resource from file: " + thisFile.getName());
                
                String resourceID = null;
                VersionNumber versionNo = null; 
                		
                try {
                	
                	IResourceHelper helper = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType);
                	ResourceMetadata newEntity = helper.getMetadataFromResource(thisFile);
                	resourceID = newEntity.getResourceID();
                	versionNo = newEntity.getVersionNo();
                	
                } catch (Exception ex) {
                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " error: " + ex.getMessage() + " - IGNORING");
                	ex.printStackTrace();
                }
                
                if (versionNo == null) {
                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - Version number was missing or invalid");
                	LOG.severe("Unable to process file as it is has an invalid version: " + thisFile.getName());
                } else if (!versionNo.isValid()) {
                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - Version number was missing or invalid");
                	LOG.severe("Unable to process file as it is has an invalid version: " + thisFile.getName());
                } else if (resourceID == null) {
                	addMessage("[!] FAILED to load: " + thisFile.getName() + " (" + resourceType + ") - No resource ID provided in the URL");
                	LOG.severe("Unable to process file as it is has an invalid resource ID: " + thisFile.getName());
                } else {
	                // Now, try to build a new versioned filename and copy the file to it
                	String newFilename = resourceID + "-versioned-" + versionNo + ".xml";
                	
                	LOG.fine("Copying new profile into versioned directory with new filename: " + newFilename);
                	addMessage("  - Copying new " + resourceType + " into versioned directory with new filename: " + newFilename);
                	File newFile = new File(outputDirectory + "/" + newFilename);
                	FileUtils.copyFile(thisFile, newFile);
                	
                	// And also copy other resources (diffs, details, bindings, etc).
                	copyOtherResources(thisFile, newFile);
                }
            }
        }
        
        addMessage("Finished pre-processing " + resourceType + " files from disk.");
        addMessage("--------------------------------------------------------------------------------------");
    }

	private static void logStart(FHIRVersion fhirVersion, ResourceType resourceType) {
		//profileLoadMessages.clear();
		LOG.fine("Starting pre-processor to convert files into versioned files prior to loading into the server for " + fhirVersion);
		addMessage("--------------------------------------------------------------------------------------");
		addMessage("Loading " + resourceType + " files from disk: " + DateUtils.printCurrentDateTime());
	}

	private static String ensureVersionedFolderExists(FHIRVersion fhirVersion, ResourceType resourceType) throws IOException {
		String versionedPath = resourceType.getVersionedFilesystemPath(fhirVersion);
		FileUtils.forceMkdir(new File(versionedPath));
		return versionedPath;
	}

	private static List<File> findXmlFiles(String versionDirectoryPath) {
        File folder = new File(versionDirectoryPath);
        
        File[] fileList = folder.listFiles(XML_FILE_FILTER);
        
        if (fileList == null) {
        	return new ArrayList<>();
        } else {
        	return Arrays.asList(fileList);
        }
	}
	
	/**
	 * If the FHIR resoutce also has other generated resources (e.g. details view, diff, bindings, etc.) then also
	 * copy those into the relevant versioned directory along with our resource
	 * 
	 * @param oldFile Original filename
	 * @param newFile New filename of resource
	 */
	protected static void copyOtherResources(File oldFile, File newFile) {
		
		String oldDir = oldFile.getParent();
		String oldName = FileLoader.removeFileExtension(oldFile.getName());
		File resourceDir = new File(oldDir + "/" + oldName);
		//System.out.println(resourceDir.getAbsolutePath());
		
		String newDir = newFile.getParent();
		String newName = FileLoader.removeFileExtension(newFile.getName());
		File targetDir = new File(newDir + "/" + newName);
		//System.out.println(targetDir.getAbsolutePath());
		
		if(resourceDir.exists() && resourceDir.isDirectory()) { 
			try {
				// Create target dir
				FileUtils.forceMkdir(targetDir);
				
				// Now, loop through and copy any files into the target directory
	            File[] fileList = resourceDir.listFiles();
	            if (fileList != null) {
	    	        for (File thisFile : fileList) {
	    	        	FileUtils.copyFileToDirectory(thisFile, targetDir);
	    	        }
	            }
			} catch (IOException e) {
				LOG.severe("Unable to copy supporting resources!");
				e.printStackTrace();
			}
		}
	}

}
