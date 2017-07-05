/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import uk.nhs.fhir.datalayer.DataLoaderMessages;
import uk.nhs.fhir.datalayer.DataSourceFactory;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.resourcehandlers.stu3.StrutureDefinitionProvider;
import uk.nhs.fhir.resourcehandlers.stu3.ValueSetProvider;
import uk.nhs.fhir.servlethelpers.ExtensionsList;
import uk.nhs.fhir.servlethelpers.RawResourceRender;
import uk.nhs.fhir.servlethelpers.ServletStreamArtefact;
import uk.nhs.fhir.servlethelpers.ServletStreamExample;
import uk.nhs.fhir.servlethelpers.ServletStreamRawFile;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.resourcehandlers.stu3.CustomServerConformanceProvider;
import uk.nhs.fhir.resourcehandlers.stu3.ImplementationGuideProvider;
import uk.nhs.fhir.resourcehandlers.stu3.OperationDefinitionProvider;
import uk.nhs.fhir.util.PropertyReader;

/**
 * This is effectively the core of a HAPI RESTFul server.
 *
 * We create a datastore in initialize method, which we pass to each ResourceProvider so that all resources can be persisted to/from the same datastore.
 *
 * @author Tim Coates, Adam Hatherly
 */
@WebServlet(urlPatterns = {"/3.0.1/*"}, displayName = "STU3 FHIR Servlet", loadOnStartup = 1)
public class STU3RestfulServlet extends RestfulServer {

    private static final Logger LOG = Logger.getLogger(STU3RestfulServlet.class.getName());
    private static final FHIRVersion fhirVersion = FHIRVersion.STU3;
    private static String logLevel = PropertyReader.getProperty("logLevel");
    private static final long serialVersionUID = 1L;
    private static Datasource dataSource = null;
    private static ResourceWebHandler webber = null;
    private static RawResourceRender myRawResourceRenderer = null;

    //private static String css = FileLoader.loadFileOnClasspath("/style.css");
    //private static String hl7css = FileLoader.loadFileOnClasspath("/hl7style.css");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.info("Requested URI: " + request.getRequestURI());

        String requestedPath = request.getRequestURI().substring(6);
        LOG.info("Request path: " + requestedPath);
        
        if(requestedPath.endsWith(".css")) {
            // Stylesheets
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "text/css", request.getRequestURI());
        } else if (requestedPath.endsWith("favicon.ico")) {
        	// favicon.ico
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "image/x-icon", PropertyReader.getProperty("faviconFile"));
        } else if (requestedPath.startsWith("/images/") || request.getRequestURI().startsWith("/js/")) {
        	// Image and JS files
        	ServletStreamRawFile.streamRawFileFromClasspath(response, null, request.getRequestURI());
        } else if (requestedPath.startsWith("/artefact")) {
        	ServletStreamArtefact.streamArtefact(request, response, fhirVersion, dataSource);
        } else if (requestedPath.startsWith("/Examples/")) {
        	ServletStreamExample.streamExample(request, response, fhirVersion, dataSource, myRawResourceRenderer);
        } else if (requestedPath.startsWith("/Extensions")) {
        	ExtensionsList.loadExtensions(request, response, fhirVersion, webber);
        } else if (requestedPath.equals("/dataLoadStatusReport")) {
	    	response.setStatus(200);
			response.setContentType("text/plain");
			PrintWriter outputStream = response.getWriter();
	        outputStream.write(DataLoaderMessages.getProfileLoadMessages());
        } else {
            super.doGet(request, response);
        }
    }

    /**
     * This is where we start, called when our servlet is first initialised. For simplicity, we do the datastore setup once here.
     *
     *
     * @throws ServletException
     */
    @Override
    protected void initialize() throws ServletException {
    	
    	// Explicitly set this as an STU3 FHIR server
    	super.setFhirContext(FhirContext.forDstu3());

        // We set our logging level based on the config file property.
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("INFO")) {
           LOG.setLevel(Level.INFO);
        }
        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
        
        // We create an instance of our persistent layer (either MongoDB or
        // Filesystem), which we'll pass to each resource type handler as we create them
        dataSource = DataSourceFactory.getDataSource();
        webber = new ResourceWebHandler(dataSource, fhirVersion);
        myRawResourceRenderer = new RawResourceRender(webber);
        
        // Pass our resource handler to the other servlets
        IndexServlet.setResourceHandler(webber);

        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(new StrutureDefinitionProvider(dataSource));
        //resourceProviders.add(new PatientProvider(dataSource));
        //resourceProviders.add(new DocumentReferenceProvider(dataSource));
        //resourceProviders.add(new PractitionerProvider(dataSource));
        //resourceProviders.add(new OrganizationProvider(dataSource));
        //resourceProviders.add(new BundleProvider(dataSource));
        resourceProviders.add(new ValueSetProvider(dataSource));
        resourceProviders.add(new OperationDefinitionProvider(dataSource));
        resourceProviders.add(new ImplementationGuideProvider(dataSource));
        //resourceProviders.add(new ConformanceProvider(dataSource));
        setResourceProviders(resourceProviders);
        registerInterceptor(new STU3PlainContent(webber));
        LOG.info("resourceProviders added");
        
        setServerConformanceProvider(new CustomServerConformanceProvider());
        LOG.info("Custom Conformance provider added");
    }
}
