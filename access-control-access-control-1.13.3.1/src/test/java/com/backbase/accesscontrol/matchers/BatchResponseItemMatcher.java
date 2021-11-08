package com.backbase.accesscontrol.matchers;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import java.util.List;

public class BatchResponseItemMatcher {

    public static boolean containsFailedResponseItem(List<BatchResponseItem> batchResponseItems,
        BatchResponseItem batchResponseItem) {
        return batchResponseItems.stream()
            .anyMatch(responseItem ->
                responseItem.getStatus().equals(batchResponseItem.getStatus())
                    && responseItem.getErrors().equals(batchResponseItem.getErrors())
                    && responseItem.getResourceId().equals(batchResponseItem.getResourceId())
            );
    }

    public static boolean containsSuccessfulResponseItem(List<BatchResponseItem> batchResponseItems,
        BatchResponseItem batchResponseItem) {
        return batchResponseItems.stream()
            .anyMatch(responseItem ->
                responseItem.getStatus().equals(batchResponseItem.getStatus())
                    && responseItem.getResourceId().equals(batchResponseItem.getResourceId())
                    && responseItem.getErrors().isEmpty()
            );
    }

    public static boolean containsSuccessfulResponseItem(List<BatchResponseItem> batchResponseItems,
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem batchResponseItem) {
        return batchResponseItems.stream()
            .anyMatch(responseItem ->
                responseItem.getStatus().toString().equals(batchResponseItem.getStatus().toString())
                    && responseItem.getResourceId().equals(batchResponseItem.getResourceId())
                    && responseItem.getErrors().isEmpty()
            );
    }

    public static boolean containsFailedResponseItem(List<BatchResponseItem> batchResponseItems,
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem batchResponseItem) {
        return batchResponseItems.stream()
            .anyMatch(responseItem ->
                responseItem.getStatus().toString().equals(batchResponseItem.getStatus().toString())
                    && responseItem.getErrors().equals(batchResponseItem.getErrors())
                    && responseItem.getResourceId().equals(batchResponseItem.getResourceId())
            );
    }
}
