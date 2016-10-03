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

import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author Tim Coates
 */
public class StrutureDefinitionResourceProvider implements IResourceProvider {

    /**
     * Get the Type that this IResourceProvider handles, so that the servlet
     * can say it handles that type.
     * 
     * @return Class type, used in generating Conformance profile resource.
     */
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return StructureDefinition.class;
    }

    /**
     * Code to call the validation process, whatever that happens to be...
     *
     * See: http://hapifhir.io/doc_rest_operations.html#Type_Level_-_Validate
     *
     * @param resourceWeAreTesting
     * @param theMode
     * @param theProfile
     * @return
     */
    @Validate
    public MethodOutcome validatePatient(@ResourceParam StructureDefinition resourceWeAreTesting,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) {

        // Actually do our validation: The UnprocessableEntityException
        // results in an HTTP 422, which is appropriate for business rule failure
        if(resourceWeAreTesting.getIdentifierFirstRep().isEmpty()) {
            /* It is also possible to pass an OperationOutcome resource
             * to the UnprocessableEntityException if you want to return
             * a custom populated OperationOutcome. Otherwise, a simple one
             * is created using the string supplied below.
             */
            throw new UnprocessableEntityException("No identifier supplied");
        }

        // This method returns a MethodOutcome object
        MethodOutcome retVal = new MethodOutcome();

        // You may also add an OperationOutcome resource to return
        // This part is optional though:
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue().setSeverity(IssueSeverityEnum.WARNING).setDiagnostics("One minor issue detected");


        retVal.setOperationOutcome(outcome);

        return retVal;
    }

    /**
     * Instance level GET of a resource...
     *
     * @param theId ID value identifying the resource.
     *
     * @return A StructureDefinition resource
     */
    @Read
    public StructureDefinition getResourceById(@IdParam IdDt theId) {
        throw new NotImplementedException("Not yet built");
    }
}
