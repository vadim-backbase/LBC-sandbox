package com.backbase.accesscontrol.dto;

import java.util.List;
import lombok.Data;

@Data
public class PersistenceDataGroupExtendedItemDto {

    private String id;
    private String name;
    private String description;
    private String type;
    private List<String> items;
    private String externalServiceAgreementId;
}
