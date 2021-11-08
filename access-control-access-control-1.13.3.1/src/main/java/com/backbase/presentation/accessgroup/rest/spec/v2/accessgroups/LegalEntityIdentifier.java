package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.SharesEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Size;
import java.util.Objects;

public class LegalEntityIdentifier {
    @JsonProperty("externalIdIdentifier")
    private String externalIdIdentifier;

    @JsonProperty("shares")
    private SharesEnum shares;

    public LegalEntityIdentifier externalIdIdentifier(String externalIdIdentifier) {
        this.externalIdIdentifier = externalIdIdentifier;
        return this;
    }

    /**
     * Legal Entity External Identifier
     * @return externalIdIdentifier
     */

    @Size(min=1)
    public String getExternalIdIdentifier() {
        return externalIdIdentifier;
    }

    public void setExternalIdIdentifier(String externalIdIdentifier) {
        this.externalIdIdentifier = externalIdIdentifier;
    }

    public LegalEntityIdentifier withExternalIdentifier(String externalIdIdentifier) {
        this.externalIdIdentifier = externalIdIdentifier;
        return this;
    }

    /**
     * Specifies for the legal entity what it is sharing in the service agreements to be returned: accounts,
     * users, users and accounts
     * @return SharesEnum
     */
    public SharesEnum getShares() {
        return shares;
    }

    public void setShares(SharesEnum shares) {
        this.shares = shares;
    }

    public LegalEntityIdentifier withShares(SharesEnum shares) {
        this.shares = shares;
        return this;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LegalEntityIdentifier legalEntityIdentifier = (LegalEntityIdentifier) o;
        return Objects.equals(this.externalIdIdentifier, legalEntityIdentifier.externalIdIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalIdIdentifier);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LegalEntityIdentifier {\n");

        sb.append("    externalIdIdentifier: ").append(toIndentedString(externalIdIdentifier)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
