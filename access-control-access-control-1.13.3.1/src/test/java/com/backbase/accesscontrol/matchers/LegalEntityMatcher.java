package com.backbase.accesscontrol.matchers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;

import com.backbase.accesscontrol.domain.LegalEntity;
import java.util.Collection;
import org.hamcrest.Matcher;

public class LegalEntityMatcher {

    public static Matcher<LegalEntity> getLegalEntityMatcher(Matcher<?> idMatcher,
        Matcher<?> externalIdMatcher,
        Matcher<?> nameMatcher,
        Matcher<?> parentMatcher,
        Matcher<Collection<?>> childrenMatcher,
        Matcher<Collection<?>> legalEntityAncestorsMatcher) {
        return allOf(
            hasProperty("id", idMatcher),
            hasProperty("externalId", externalIdMatcher),
            hasProperty("name", nameMatcher),
            hasProperty("parent", parentMatcher),
            hasProperty("children", childrenMatcher),
            hasProperty("legalEntityAncestors", legalEntityAncestorsMatcher)
        );
    }
}
