
package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({

})
@JsonIgnoreProperties(ignoreUnknown = true)
public class RootLegalEntityGetResponseBody
    extends LegalEntityBase
{


    @Override
    public RootLegalEntityGetResponseBody withId(String id) {
        super.withId(id);
        return this;
    }

    @Override
    public RootLegalEntityGetResponseBody withExternalId(String externalId) {
        super.withExternalId(externalId);
        return this;
    }

    @Override
    public RootLegalEntityGetResponseBody withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public RootLegalEntityGetResponseBody withType(Type type) {
        super.withType(type);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
