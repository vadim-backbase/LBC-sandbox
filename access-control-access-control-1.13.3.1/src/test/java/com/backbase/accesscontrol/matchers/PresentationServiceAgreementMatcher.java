package com.backbase.accesscontrol.matchers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreement;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class PresentationServiceAgreementMatcher {

    public static Matcher<PresentationServiceAgreement> getPresentationServiceAgreementMatcher(
        PresentationServiceAgreement serviceAgreement) {
        return getPresentationServiceAgreementMatcher(
            is(serviceAgreement.getId()),
            is(serviceAgreement.getExternalId()),
            is(serviceAgreement.getName()),
            is(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status
                .fromValue(serviceAgreement.getStatus().toString())),
            is(serviceAgreement.getIsMaster()),
            is(serviceAgreement.getCreatorLegalEntity()),
            is(serviceAgreement.getCreatorLegalEntityName()),
            is(serviceAgreement.getNumberOfParticipants()),
            is(serviceAgreement.getValidFromDate()),
            is(serviceAgreement.getValidFromTime()),
            is(serviceAgreement.getValidUntilDate()),
            is(serviceAgreement.getValidUntilTime())
        );
    }

    public static Matcher<PresentationServiceAgreement> getPresentationServiceAgreementMatcher(
        PersistenceServiceAgreement serviceAgreement) {
        return getPresentationServiceAgreementMatcher(
            is(serviceAgreement.getId()),
            is(serviceAgreement.getExternalId()),
            is(serviceAgreement.getName()),
            is(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status
                .fromValue(serviceAgreement.getStatus().toString())),
            is(serviceAgreement.getIsMaster()),
            is(serviceAgreement.getCreatorLegalEntity()),
            is(serviceAgreement.getCreatorLegalEntityName()),
            is(serviceAgreement.getNumberOfParticipants()),
            Matchers.is(DateFormatterUtil.utcFormatDateOnly(serviceAgreement.getValidFrom())),
            is(DateFormatterUtil.utcFormatTimeOnly(serviceAgreement.getValidFrom())),
            is(DateFormatterUtil.utcFormatDateOnly(serviceAgreement.getValidUntil())),
            is(DateFormatterUtil.utcFormatTimeOnly(serviceAgreement.getValidUntil()))

        );
    }

    public static Matcher<PresentationServiceAgreement> getPresentationServiceAgreementMatcher(Matcher<?> idMatcher,
        Matcher<?> externalIdMatcher,
        Matcher<?> nameMatcher,
        Matcher<?> statusMatcher,
        Matcher<?> isMasterMatcher,
        Matcher<?> creatorIdMatcher,
        Matcher<?> creatorNameMatcher,
        Matcher<?> numberOfParticipantsMatcher,
        Matcher<?> validFromDateMatcher,
        Matcher<?> validFromTimeMatcher,
        Matcher<?> validUntilDateMatcher,
        Matcher<?> validUntilTimeMatcher) {
        return allOf(
            hasProperty("id", idMatcher),
            hasProperty("externalId", externalIdMatcher),
            hasProperty("name", nameMatcher),
            hasProperty("status", statusMatcher),
            hasProperty("isMaster", isMasterMatcher),
            hasProperty("creatorLegalEntity", creatorIdMatcher),
            hasProperty("creatorLegalEntityName", creatorNameMatcher),
            hasProperty("numberOfParticipants", numberOfParticipantsMatcher),
            hasProperty("validFromDate", validFromDateMatcher),
            hasProperty("validFromTime", validFromTimeMatcher),
            hasProperty("validUntilDate", validUntilDateMatcher),
            hasProperty("validUntilTime", validUntilTimeMatcher)
        );
    }
}
