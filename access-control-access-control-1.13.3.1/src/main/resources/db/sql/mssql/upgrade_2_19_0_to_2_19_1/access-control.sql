CREATE TABLE [approval_service_agreement_ref](
    [id]                                           [BIGINT]       NOT NULL,
    [service_agreement_id]                         [NVARCHAR](36) NULL,
    CONSTRAINT pk_approval_sa_ref                  PRIMARY KEY CLUSTERED (id),
    CONSTRAINT fk_asar2aca                         FOREIGN KEY (id) REFERENCES access_control_approval(id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_service_agreement] (
  [id]                                      [BIGINT]        NOT NULL,
  [external_id]                             [NVARCHAR](64),
  [name]                                    [NVARCHAR](128) NOT NULL,
  [description]                             [NVARCHAR](255) NOT NULL,
  [is_master]                               [TINYINT]       NOT NULL,
  [creator_legal_entity_id]                 [NVARCHAR](36)  NOT NULL,
  [state]                                   [NVARCHAR](16)  NOT NULL DEFAULT ('ENABLED'),
  [start_date]                               DATETIME,
  [end_date]                                 DATETIME,
  CONSTRAINT pk_approval_service_agreement   PRIMARY KEY CLUSTERED (id),
  CONSTRAINT fk_asa2asar                     FOREIGN KEY (id) REFERENCES approval_service_agreement_ref (id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

GO

CREATE UNIQUE INDEX uq_approval_sa_01
    ON approval_service_agreement (external_id)
    WHERE external_id IS NOT NULL;

GO

CREATE TABLE [approval_add_prop_sa] (
  [id]                                               [BIGINT]        NOT NULL,
  [property_key]                                     [NVARCHAR](50)  NOT NULL,
  [property_value]                                   [NVARCHAR](500),
  CONSTRAINT pk_approval_add_prop_sa                 PRIMARY KEY CLUSTERED (id,property_key),
  CONSTRAINT fk_aapsa2asa                            FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

GO

CREATE TABLE [approval_service_agreement_aps](
  [id]                             [BIGINT]        NOT NULL,
  [assignable_permission_set_id]   [BIGINT]        NOT NULL,
  [type]                           [TINYINT]       NOT NULL,
  CONSTRAINT pk_asa_aps            PRIMARY KEY CLUSTERED (id, assignable_permission_set_id, type),
  CONSTRAINT fk_asaa2asa           FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
     ON UPDATE NO ACTION
     ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_sa_participant] (
  [id]                                                     [BIGINT]       NOT NULL,
  [legal_entity_id]                                        [NVARCHAR](36) NOT NULL,
  [share_users]                                            [TINYINT]      NOT NULL,
  [share_accounts]                                         [TINYINT]      NOT NULL,
  CONSTRAINT pk_approval_sa_participant                    PRIMARY KEY CLUSTERED (id,legal_entity_id),
  CONSTRAINT fk_asap2asa                                   FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
     ON DELETE NO ACTION
     ON UPDATE NO ACTION
);

GO

CREATE TABLE [approval_sa_admins] (
   [id]                                               [BIGINT]       NOT NULL,
   [legal_entity_id]                                  [NVARCHAR](36) NOT NULL,
   [user_id]                                          [NVARCHAR](36) NOT NULL,
   CONSTRAINT pk_approval_sa_admins                   PRIMARY KEY CLUSTERED (id,legal_entity_id,user_id),
   CONSTRAINT fk_asaa2asap                            FOREIGN KEY (id,legal_entity_id) REFERENCES approval_sa_participant(id,legal_entity_id)
       ON DELETE NO ACTION
       ON UPDATE NO ACTION
);

GO