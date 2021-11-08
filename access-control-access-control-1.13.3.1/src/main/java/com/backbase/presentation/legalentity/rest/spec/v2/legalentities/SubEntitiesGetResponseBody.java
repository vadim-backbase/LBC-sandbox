
package com.backbase.presentation.legalentity.rest.spec.v2.legalentities;

import javax.annotation.Generated;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({

})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubEntitiesGetResponseBody
    extends LegalEntityBase
{


    @Override
    public SubEntitiesGetResponseBody withId(String id) {
        super.withId(id);
        return this;
    }

    @Override
    public SubEntitiesGetResponseBody withExternalId(String externalId) {
        super.withExternalId(externalId);
        return this;
    }

    @Override
    public SubEntitiesGetResponseBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public SubEntitiesGetResponseBody withType(LegalEntityType type) {
        super.withType(type);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
