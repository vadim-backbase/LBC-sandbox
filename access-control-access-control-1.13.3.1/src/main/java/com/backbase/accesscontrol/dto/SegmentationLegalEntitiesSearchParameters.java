package com.backbase.accesscontrol.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class SegmentationLegalEntitiesSearchParameters {
    private String query;
    private String businessFunction;
    private String serviceAgreementId;
    private String userId;
    private String legalEntityId;
    private String privilege;
    private Integer from;
    private String cursor;
    private Integer size;

    /**
     * Constructor.
     *
     * @param query                             - query string used to filter data(LE name or LE ID)
     * @param businessFunction                  - string used to filter data according businessFunction
     * @param userId                            - string used to filter data according userId
     * @param privilege                         - string used to filter data according privilege
     * @param from                              - from parameter used for pagination
     * @param cursor                            - cursor used for pagination
     * @param size                              - size used for paginatio
     */
    public SegmentationLegalEntitiesSearchParameters(String query, String businessFunction, String userId,
        String privilege, Integer from, String cursor, Integer size) {
        this.query = query;
        this.businessFunction = businessFunction;
        this.userId = userId;
        this.privilege = privilege;
        this.from = from;
        this.cursor = cursor;
        this.size = size;
    }
}
