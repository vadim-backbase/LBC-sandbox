package com.backbase.accesscontrol.matchers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;

import com.backbase.audit.client.model.AuditMessage;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationDataItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserPermission;
import java.util.List;
import java.util.Objects;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class MatcherUtil {


    public static <T> Matcher<InternalRequest<T>> hasPayload(T payload) {
        return new BaseMatcher<InternalRequest<T>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has payload ");
            }

            @Override
            public boolean matches(Object o) {
                if (o instanceof InternalRequest) {
                    Object data = ((InternalRequest) o).getData();
                    return payload.equals(data);
                }
                return false;
            }
        };
    }

    public static boolean containsFailedResponseItem(InternalRequest<List<BatchResponseItem>> response,
        BatchResponseItem batchResponseItem) {
        return response.getData().stream()
            .anyMatch(responseItem ->
                responseItem.getStatus().equals(batchResponseItem.getStatus())
                    && responseItem.getErrors().equals(batchResponseItem.getErrors())
                    && responseItem.getResourceId().equals(batchResponseItem.getResourceId())
            );
    }

    public static boolean containsSuccessfulResponseItem(InternalRequest<List<BatchResponseItem>> response,
        BatchResponseItem batchResponseItem) {
        return response.getData().stream()
            .anyMatch(responseItem ->
                responseItem.getStatus().equals(batchResponseItem.getStatus())
                    && responseItem.getResourceId().equals(batchResponseItem.getResourceId())
            );
    }

    public static boolean containsFailedResponseItem(InternalRequest<List<BatchResponseItemExtended>> response,
        BatchResponseItemExtended batchResponseItem) {

        return containsFailedResponseItem(response.getData(), batchResponseItem);
    }

    public static boolean containsFailedResponseItem(List<BatchResponseItemExtended> response,
        BatchResponseItemExtended batchResponseItem) {
        return response.stream()
            .anyMatch(responseItem ->
                responseItem.getStatus().equals(batchResponseItem.getStatus())
                    && responseItem.getErrors().equals(batchResponseItem.getErrors())
                    && (
                    (Objects.isNull(responseItem.getResourceId()) && Objects.isNull(batchResponseItem.getResourceId()))
                        || responseItem.getResourceId().equals(batchResponseItem.getResourceId()))
            );
    }

    public static boolean containsSuccessfulResponseItem(InternalRequest<List<BatchResponseItemExtended>> response,
        BatchResponseItemExtended batchResponseItem) {

        return containsSuccessfulResponseItem(response.getData(), batchResponseItem);
    }

    public static boolean containsSuccessfulResponseItem(List<BatchResponseItemExtended> response,
        BatchResponseItemExtended batchResponseItem) {
        return response.stream()
            .anyMatch(responseItem ->
                responseItem.getStatus().equals(batchResponseItem.getStatus())
                    && responseItem.getResourceId().equals(batchResponseItem.getResourceId())
            );
    }

    public static Matcher<AuditMessage> getAuditMessageMatcher(Matcher<?> statusMatcher,
        Matcher<?> eventMetaDataMatcher) {

        return Matchers.allOf(
            hasProperty("status", statusMatcher),
            hasProperty("eventMetaData", eventMetaDataMatcher)
        );
    }

    public static Matcher<AuditMessage> getAuditMessageMatcherWithDescription(Matcher<?> statusMatcher,
        Matcher<?> descriptionMatcher, Matcher<?> eventMetaDataMatcher) {

        return Matchers.allOf(
            hasProperty("status", statusMatcher),
            hasProperty("eventDescription", descriptionMatcher),
            hasProperty("eventMetaData", eventMetaDataMatcher)
        );
    }

    public static Matcher<PresentationUserPermission> getPermissionsMatcher(Matcher<String> resourceMatcher,
        Matcher<?> functionMatcher, Matcher<?> privilegesMatcher) {
        return allOf(
            hasProperty("resource", resourceMatcher),
            hasProperty("businessFunction", functionMatcher),
            hasProperty("privileges", privilegesMatcher)
        );
    }

    public static Matcher<PresentationDataItem> getDataItemMatcher(Matcher<?> dataIdMatcher, Matcher<?> dataTypeMatcher) {
        return allOf(
            hasProperty("id", dataIdMatcher),
            hasProperty("dataType", dataTypeMatcher)
        );
    }

    public static Matcher<PresentationUserDataItemPermission> getDataItemPermissionMatcher(Matcher<?> dataItemMatcher,
        Matcher<?> permissionMatcher) {
        return allOf(
            hasProperty("dataItem", dataItemMatcher),
            hasProperty("permissions", permissionMatcher)

        );
    }

}
