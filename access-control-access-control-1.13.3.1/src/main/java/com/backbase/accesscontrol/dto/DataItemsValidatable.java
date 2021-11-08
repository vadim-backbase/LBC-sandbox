package com.backbase.accesscontrol.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class DataItemsValidatable {

    private String type;
    private List<String> items;
    private String serviceAgreementId;

}
