package com.backbase.accesscontrol.util;

import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for function groups and data groups.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataFunctionGroupUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFunctionGroupUtil.class);


    /**
     * Separates presentationIdentifier requests to valid and invalid.
     *
     * @param presentationIdentifierList - list of {@link PresentationIdentifier}
     * @param indexValidaCorrelation - list of booleans indicating if th request is being processed or not
     * @param invalidRequestPayloads - invalid requests
     * @param validRequest - valid requests
     */
    public static void separateValidFromInvalidRequests(List<PresentationIdentifier> presentationIdentifierList,
        List<Boolean> indexValidaCorrelation, List<PresentationIdentifier> invalidRequestPayloads,
        List<PresentationIdentifier> validRequest) {

        for (PresentationIdentifier identifier : presentationIdentifierList) {
            if (isIdentifierValid(identifier)) {
                indexValidaCorrelation.add(true);
                validRequest.add(identifier);
            } else {
                indexValidaCorrelation.add(false);
                invalidRequestPayloads.add(identifier);
            }
        }
    }

    /**
     * Validating identifier.
     *
     * @param request - identifier
     * @return is identifier valid
     */
    public static boolean isIdentifierValid(PresentationIdentifier request) {
        return (request.getIdIdentifier() != null ^ request.getNameIdentifier() != null);
    }

    /**
     * Merge BatchResponseItemExtended, validData is one that is retrieved from pandp <br/> and other one invalidData
     * which is found like bad request in presentation. <br/> <br/> mapOrderOfRequests is map by which will be the order
     * of the merged list.<br/> It keeps only the order of valid and invalid requests.
     *
     * @param mapOrderOfRequests keeps the order of valid and invalid requests
     */
    public static List<BatchResponseItemExtended> mergeResponses(List<Boolean> mapOrderOfRequests,
        List<BatchResponseItemExtended> validData, List<BatchResponseItemExtended> invalidData) {
        long expectedValidElements = mapOrderOfRequests.stream()
            .filter(a -> a).count();
        if (validData.size() + invalidData.size() != mapOrderOfRequests.size() && expectedValidElements == validData
            .size()) {
            LOGGER.warn("Response and request data mismatch valid: {},invalid: {}, mapOrder: {}", validData,
                invalidData, mapOrderOfRequests);
            throw getInternalServerErrorException("Response and request data mismatch");
        }

        Iterator<BatchResponseItemExtended> validDataIterator = validData.iterator();
        Iterator<BatchResponseItemExtended> invalidDataIterator = invalidData.iterator();

        return mapOrderOfRequests
            .stream()
            .map(isValid -> getNextBatchResponse(validDataIterator, invalidDataIterator, isValid))
            .collect(Collectors.toList());
    }


    private static BatchResponseItemExtended getNextBatchResponse(Iterator<BatchResponseItemExtended> validDataIterator,
        Iterator<BatchResponseItemExtended> invalidDataIterator, Boolean isValid) {
        if (isValid) {
            return validDataIterator.next();
        } else {
            return invalidDataIterator.next();
        }
    }

}