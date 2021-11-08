package com.backbase.accesscontrol.dto;

import com.backbase.dbs.user.api.client.v2.model.GetUser;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ServiceAgreementData<T> {

    private T request;
    private Map<String, GetUser> usersByExternalId;

}
