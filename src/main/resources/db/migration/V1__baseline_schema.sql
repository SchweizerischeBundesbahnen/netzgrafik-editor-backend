-- projects
CREATE SEQUENCE projects_seq
    START WITH 100 CACHE 20;

CREATE TABLE projects
(
    id          bigint       NOT NULL DEFAULT nextval('projects_seq') PRIMARY KEY,
    name        varchar(255) NOT NULL,
    created_at  timestamp    NOT NULL,
    created_by  varchar(10)  NOT NULL,
    summary     varchar(255) NOT NULL,
    description text         NOT NULL,
    is_archived boolean      NOT NULL
);

CREATE TABLE projects_users
(
    project_id bigint      NOT NULL,
    user_id    varchar(10) NOT NULL,
    is_editor  boolean     NOT NULL,

    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_project_id FOREIGN KEY (project_id) REFERENCES projects (id)
);

-- variants
CREATE SEQUENCE variants_seq
    START WITH 100 CACHE 20;

CREATE TABLE variants
(
    id          bigint  NOT NULL DEFAULT nextval('variants_seq') PRIMARY KEY,
    project_id  bigint  NOT NULL,
    is_archived boolean NOT NULL,

    CONSTRAINT project_id_fkey FOREIGN KEY (project_id) REFERENCES projects (id)
);

-- versions
CREATE SEQUENCE versions_seq
    START WITH 100 CACHE 20;

CREATE TABLE versions
(
    id               bigint       NOT NULL DEFAULT nextval('versions_seq') PRIMARY KEY,
    variant_id       bigint       NOT NULL,
    release_version  int          NOT NULL,
    snapshot_version int,
    name             varchar(255) NOT NULL,
    comment          varchar(255) NOT NULL,
    created_at       timestamp    NOT NULL,
    created_by       varchar(10)  NOT NULL,
    model            jsonb        NOT NULL,

    CONSTRAINT variant_id_fkey FOREIGN KEY (variant_id) REFERENCES variants (id),
    UNIQUE (variant_id, release_version, snapshot_version, created_by)
);


/* [jooq ignore start] */
-- sadly the generated H2 DDL does not support the WHERE condition (might work in future versions of JOOQ / H2)
CREATE UNIQUE INDEX idx_unique_release_version ON versions (variant_id, release_version) WHERE snapshot_version IS NULL;
/* [jooq ignore stop] */
