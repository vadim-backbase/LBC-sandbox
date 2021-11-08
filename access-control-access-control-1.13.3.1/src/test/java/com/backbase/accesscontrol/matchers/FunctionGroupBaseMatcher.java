package com.backbase.accesscontrol.matchers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupBase;
import org.hamcrest.Matcher;

public class FunctionGroupBaseMatcher {

    public static <T extends FunctionGroupBase> Matcher<T> getFunctionGroupTMatcher(
        Matcher<?> nameMatcher,
        Matcher<?> descriptionMatcher,
        Matcher<?> permissionsMatcher) {
        return allOf(
            hasProperty("name", nameMatcher),
            hasProperty("description", descriptionMatcher),
            hasProperty("permissions", permissionsMatcher)
        );
    }


    public static Matcher<Permission> getPermissionMatcher(Matcher<?> functionIdMatcher,
        Matcher<?> assignedPrivilegesMatcher) {
        return allOf(
            hasProperty("functionId", functionIdMatcher),
            hasProperty("assignedPrivileges", assignedPrivilegesMatcher)

        );
    }

    public static Matcher<Privilege> getPrivilegeMatcher(Matcher<?> privilegeMatcher) {
        return allOf(
            hasProperty("privilege", privilegeMatcher)
        );
    }
}
