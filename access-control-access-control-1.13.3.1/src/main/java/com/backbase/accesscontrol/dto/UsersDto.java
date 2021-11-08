
package com.backbase.accesscontrol.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class UsersDto {

    private String legalEntityId;
    private List<String> users = new ArrayList<>();

    public UsersDto withLegalEntityId(String legalEntityId) {
        this.legalEntityId = legalEntityId;
        return this;
    }

    public UsersDto withUsers(List<String> users) {
        this.users = users;
        return this;
    }
}
