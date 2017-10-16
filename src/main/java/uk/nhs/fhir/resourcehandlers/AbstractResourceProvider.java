package uk.nhs.fhir.resourcehandlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.PropertyReader;

public abstract class AbstractResourceProvider implements IResourceProvider, IResourceHelper {

	private static final Logger LOG = Logger.getLogger(AbstractResourceProvider.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");

    protected Datasource myDatasource = null;
    protected FhirContext ctx = null;
    protected ResourceType resourceType = null;
    protected FHIRVersion fhirVersion = null;
    protected Class<? extends IBaseResource> fhirClass = null;

    public AbstractResourceProvider(Datasource dataSource) {
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
        myDatasource = dataSource;
        LOG.fine("Created StrutureDefinitionProvider handler to respond to requests for StrutureDefinition resource types.");
    }
    
    /**
     * Get the Type that this IResourceProvider handles, so that the servlet can say it handles that type.
     *
     * @return Class type, used in generating Conformance profile resource.
     */
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return fhirClass;
    }
    
    /**
     * Search by name, so will respond to queries of the form: /StructureDefinition?name:contains=blah
     *
     * @param theNamePart
     * @return
     */
    @Search
	public IBundleProvider searchByName(@RequiredParam(name = StructureDefinition.SP_NAME) StringParam theNamePart) {
    	LOG.fine("Request for resources matching name: " + theNamePart);
    	return new PagedBundleProvider(PagedBundleProvider.SEARCH_BY_NAME, myDatasource,
										fhirVersion, resourceType, theNamePart.getValue());
    }
    
    /**
     * Search by URL, so will respond to queries of the form:
     * /StructureDefinition?url=http://acme.org/fhir/StructureDefinition/123
     *
     * @param theURL
     * @return
     */
    @Search
    public IBundleProvider searchByURL(@RequiredParam(name = StructureDefinition.SP_URL) StringParam theURL) {
    	LOG.fine("Request for resources matching URL: " + theURL);
    	return new PagedBundleProvider(PagedBundleProvider.SEARCH_BY_URL, myDatasource,
    									fhirVersion, resourceType, theURL.getValue());
    }

    /**
     * Overall search, will return ALL resources so responds to (for example): /StructureDefinition
     *
     * @return
     */
    @Search
    public IBundleProvider getAllResources() {
        LOG.fine("Request for ALL resources");
        return new PagedBundleProvider(PagedBundleProvider.SEARCH_BY_TYPE, myDatasource,
        								fhirVersion, resourceType);
    }

}
