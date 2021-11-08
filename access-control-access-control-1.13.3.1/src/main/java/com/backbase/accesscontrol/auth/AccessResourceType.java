package com.backbase.accesscontrol.auth;

public enum AccessResourceType {

    NONE {
        @Override
        public com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType getType() {
            return com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.NONE;
        }
    },
    USER {
        @Override
        public com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType getType() {
            return com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER;
        }
    },
    ACCOUNT {
        @Override
        public com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType getType() {
            return com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.ACCOUNT;
        }
    },
    USER_OR_ACCOUNT {
        @Override
        public com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType getType() {
            return com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users
                .AccessResourceType.USER_OR_ACCOUNT;
        }
    },
    USER_AND_ACCOUNT {
        @Override
        public com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType getType() {
            return com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users
                .AccessResourceType.USER_AND_ACCOUNT;
        }
    };

    public abstract com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users
        .AccessResourceType getType();

}
