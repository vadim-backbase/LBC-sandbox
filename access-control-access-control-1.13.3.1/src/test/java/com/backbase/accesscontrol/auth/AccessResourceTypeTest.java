package com.backbase.accesscontrol.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AccessResourceTypeTest {

    @Test
    public void shouldReturnPandpNone() {
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.NONE,
            AccessResourceType.NONE.getType());
    }

    @Test
    public void shouldReturnPandpAccount() {
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.ACCOUNT,
            AccessResourceType.ACCOUNT.getType());
    }

    @Test
    public void shouldReturnPandpUser() {
        assertEquals(com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER,
            AccessResourceType.USER.getType());
    }

    @Test
    public void shouldReturnPandpUserOrAccount() {
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER_OR_ACCOUNT,
            AccessResourceType.USER_OR_ACCOUNT.getType());
    }
}