package com.backbase.presentation.legalentity.rest.spec.v2.legalentities.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ParticipantOf;

public class ParticipantOfValidator implements ConstraintValidator<ParticipantOfType, ParticipantOf> {
    
    @Override
    public boolean isValid(ParticipantOf value, ConstraintValidatorContext context) {
        int includesExistingCsa = value.getExistingCustomServiceAgreement() == null ? 0 : 1;
        int includesNewCsa = value.getNewCustomServiceAgreement() == null ? 0 : 1;
        int includesNewMsa = value.getNewMasterServiceAgreement() == null ? 0 : 1;
        
        // Confirm that there is exactly one type of SA provided in the request
        return includesExistingCsa + includesNewCsa + includesNewMsa == 1;
    }

}
