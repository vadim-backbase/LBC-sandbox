CREATE TABLE approval_service_agreement_ref
(
    id                                             NUMBER(38, 0)     NOT NULL,
    service_agreement_id                           VARCHAR2(36)      NULL,
    CONSTRAINT pk_approval_sa_ref                  PRIMARY KEY (id),
    CONSTRAINT fk_asar2aca                         FOREIGN KEY (id) REFERENCES access_control_approval(id)
);

COMMIT;

CREATE TABLE approval_service_agreement
(
    id                                             NUMBER(38, 0)                  NOT NULL,
    external_id                                    VARCHAR2(64),
    name                                           VARCHAR2(128)                  NOT NULL,
    description                                    VARCHAR2(255)                  NOT NULL,
    is_master                                      NUMBER(3)                      NOT NULL,
    creator_legal_entity_id                        VARCHAR2(36)                   NOT NULL,
    state                                          VARCHAR2(16) DEFAULT 'ENABLED' NOT NULL,
    start_date                                     TIMESTAMP,
    end_date                                       TIMESTAMP,
    CONSTRAINT pk_approval_service_agreement       PRIMARY KEY (id),
    CONSTRAINT uq_approval_sa_01                   UNIQUE (external_id),
    CONSTRAINT fk_asa2asar                         FOREIGN KEY (id) REFERENCES approval_service_agreement_ref (id)
);

COMMIT;

CREATE TABLE approval_add_prop_sa (
    id                                                NUMBER(38, 0)  NOT NULL,
    property_key                                      VARCHAR2(50)   NOT NULL,
    property_value                                    VARCHAR2(500),
    CONSTRAINT pk_approval_add_prop_sa                PRIMARY KEY (id,property_key),
    CONSTRAINT fk_aapsa2asa                           FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
);

COMMIT;

CREATE TABLE approval_service_agreement_aps
(
    id                             NUMBER(38, 0)               NOT NULL,
    assignable_permission_set_id   NUMBER(38, 0)               NOT NULL,
    type                           NUMBER(3)                   NOT NULL,
    CONSTRAINT pk_asa_aps          PRIMARY KEY (id, assignable_permission_set_id, type),
    CONSTRAINT fk_asaa2asa         FOREIGN KEY (id)   REFERENCES approval_service_agreement (id)
);

COMMIT;

CREATE TABLE approval_sa_participant
(
    id                                                       NUMBER(38, 0) NOT NULL,
    legal_entity_id                                          VARCHAR2(36)  NOT NULL,
    share_users                                              NUMBER(3)     NOT NULL,
    share_accounts                                           NUMBER(3)     NOT NULL,
    CONSTRAINT pk_approval_sa_participant                    PRIMARY KEY (id,legal_entity_id),
    CONSTRAINT fk_asap2asa                                   FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
);

COMMIT;

CREATE TABLE approval_sa_admins
(
    id                                                 NUMBER(38, 0) NOT NULL,
    legal_entity_id                                    VARCHAR2(36)  NOT NULL,
    user_id                                            VARCHAR2(36)  NOT NULL,
    CONSTRAINT pk_approval_sa_admins                   PRIMARY KEY (id,legal_entity_id,user_id),
    CONSTRAINT fk_asaa2asap                            FOREIGN KEY (id,legal_entity_id) REFERENCES approval_sa_participant(id,legal_entity_id)
);

COMMIT;

