package com.backbase.accesscontrol.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainConstants {

    public static final String ADMIN_PERMISSIONS = "0";
    public static final String REGULAR_PERMISSIONS = "1";

    public static final String CUSTOMERS_DATA_GROUP_TYPE = "CUSTOMERS";
}