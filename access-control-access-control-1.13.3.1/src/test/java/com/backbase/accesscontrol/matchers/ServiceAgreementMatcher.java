package com.backbase.accesscontrol.matchers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import org.hamcrest.Matcher;

public class ServiceAgreementMatcher {

    public static Matcher<ServiceAgreement> getServiceAgreementMatcher(Matcher<?> idMatcher,
        Matcher<?> nameMatcher,
        Matcher<?> descriptionMatcher,
        Matcher<?> legalEntityMatcher) {
        return allOf(
            hasProperty("id", idMatcher),
            hasProperty("name", nameMatcher),
            hasProperty("description", descriptionMatcher),
            hasProperty("creatorLegalEntity", legalEntityMatcher)
        );
    }

    public static Matcher<ServiceAgreement> getServiceAgreementMatcher(Matcher<?> idMatcher,
        Matcher<?> nameMatcher,
        Matcher<?> descriptionMatcher,
        Matcher<?> creatorLegalEntityMatcher,
        Matcher<?> externalIdMatcher, Matcher<?> stateMatcher,
        Matcher<?> stateChangedAtMatcher,
        Matcher<?> additionsMather, Matcher<?> from, Matcher<?> until) {
        return allOf(
            hasProperty("id", idMatcher),
            hasProperty("name", nameMatcher),
            hasProperty("creatorLegalEntity", creatorLegalEntityMatcher),
            hasProperty("description", descriptionMatcher),
            hasProperty("externalId", externalIdMatcher),
            hasProperty("state", stateMatcher),
            hasProperty("stateChangedAt", stateChangedAtMatcher),
            hasProperty("additions", additionsMather),
            hasProperty("startDate", from),
            hasProperty("endDate", until)
        );
    }
}
