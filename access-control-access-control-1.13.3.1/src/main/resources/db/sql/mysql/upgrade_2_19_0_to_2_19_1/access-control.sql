CREATE TABLE approval_service_agreement_ref
(
    id                                             BIGINT           NOT NULL,
    service_agreement_id                           VARCHAR(36)      NULL,
    CONSTRAINT pk_approval_sa_ref                  PRIMARY KEY (id),
    CONSTRAINT fk_asar2aca                         FOREIGN KEY (id) REFERENCES access_control_approval(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_service_agreement
(
    id                                           BIGINT                                         NOT NULL,
    external_id                                  VARCHAR(64)                                    NULL,
    name                                         VARCHAR(128)                                   NOT NULL,
    description                                  VARCHAR(255)                                   NOT NULL,
    is_master                                    TINYINT                                        NOT NULL,
    creator_legal_entity_id                      VARCHAR(36)                                    NOT NULL,
    state                                        VARCHAR(16)                                    NOT NULL DEFAULT 'ENABLED',
    start_date                                   DATETIME                                       NULL,
    end_date                                     DATETIME                                       NULL,
    CONSTRAINT pk_approval_service_agreement     PRIMARY KEY (id),
    CONSTRAINT uq_approval_sa_01                 UNIQUE (external_id),
    CONSTRAINT fk_asa2asar                       FOREIGN KEY (id) REFERENCES approval_service_agreement_ref(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_add_prop_sa
(
    id                                                   BIGINT         NOT NULL,
    property_key                                         VARCHAR(50)    NOT NULL,
    property_value                                       VARCHAR(500),
    CONSTRAINT pk_approval_add_prop_sa                   PRIMARY KEY (id, property_key),
    CONSTRAINT fk_aapsa2asa                              FOREIGN KEY (id) REFERENCES approval_service_agreement(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE approval_service_agreement_aps
(
    id                              BIGINT      NOT NULL,
    assignable_permission_set_id    BIGINT      NOT NULL,
    type                            TINYINT     NOT NULL,
    CONSTRAINT pk_asa_aps           PRIMARY KEY (id, assignable_permission_set_id, type),
    CONSTRAINT fk_asaa2asa          FOREIGN KEY (id) REFERENCES approval_service_agreement(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_sa_participant
(
    id                                                         BIGINT      NOT NULL,
    legal_entity_id                                            VARCHAR(36) NOT NULL,
    share_users                                                TINYINT     NOT NULL,
    share_accounts                                             TINYINT     NOT NULL,
    CONSTRAINT pk_approval_sa_participant                      PRIMARY KEY (id,legal_entity_id),
    CONSTRAINT fk_asap2asa                                     FOREIGN KEY (id) REFERENCES approval_service_agreement(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_sa_admins
(
    id                                                  BIGINT      NOT NULL,
    legal_entity_id                                     VARCHAR(36) NOT NULL,
    user_id                                             VARCHAR(36) NOT NULL,
    CONSTRAINT pk_approval_sa_admins                    PRIMARY KEY (id,legal_entity_id,user_id),
    CONSTRAINT fk_asaa2asap                             FOREIGN KEY (id,legal_entity_id) REFERENCES approval_sa_participant(id,legal_entity_id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
