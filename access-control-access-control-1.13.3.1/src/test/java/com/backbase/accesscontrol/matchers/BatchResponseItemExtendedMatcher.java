package com.backbase.accesscontrol.matchers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hamcrest.Matcher;

public class BatchResponseItemExtendedMatcher {

    private static Matcher<BatchResponseItemExtended> getMatcher(BatchResponseItemExtended item) {
        List<Matcher<? super BatchResponseItemExtended>> matchers = new ArrayList<>();

        matchers.add(hasProperty("resourceId", is(item.getResourceId())));
        matchers.add(hasProperty("status", is(item.getStatus())));

        if (Objects.nonNull(item.getExternalServiceAgreementId())) {
            matchers.add(hasProperty("externalServiceAgreementId", is(item.getExternalServiceAgreementId())));
        }

        if (!item.getErrors().isEmpty()) {
            matchers.add(hasProperty("errors", hasItem(item.getErrors().get(0))));
        }
        return allOf(matchers);
    }

    public static List<Matcher<? super BatchResponseItemExtended>> getMatchers(List<BatchResponseItemExtended> items) {
        return items.stream().map(BatchResponseItemExtendedMatcher::getMatcher).collect(Collectors.toList());
    }
}
