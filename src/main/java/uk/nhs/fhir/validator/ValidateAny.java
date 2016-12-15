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
package uk.nhs.fhir.validator;

import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import java.util.logging.Logger;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * This class can in theory be used to validate any resource. It is called from
 * the various resource handlers, to do the validation process.
 *
 * @author Tim Coates
 */
public class ValidateAny {

    private static final Logger LOG = Logger.getLogger(ValidateAny.class.getName());
    
    public static MethodOutcome validateStructureDefinition(FhirContext ctx, @ResourceParam IBaseResource resourceToTest) {
        MethodOutcome retval = new MethodOutcome();

        FhirValidator validator = ValidatorFactory.getValidator(ctx);

        // Pass a resource in to be validated.
        ValidationResult result = null;
        try {
            result = validator.validateWithResult(resourceToTest);
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
        OperationOutcome oo = (OperationOutcome) result.toOperationOutcome();
        for (int i = 0; i < result.getMessages().size(); i++) {
            LOG.warning(result.getMessages().get(i).toString());
        }
        
        retval.setOperationOutcome(oo);

        if (result.isSuccessful()) {
            LOG.info("Validation passed");
        } else {
            LOG.warning("Validation failed");
        }
        return retval;
    }
}
