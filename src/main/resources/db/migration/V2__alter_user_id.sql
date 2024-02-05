-- projects
ALTER TABLE projects ALTER COLUMN created_by TYPE varchar(64);

ALTER TABLE projects_users ALTER COLUMN user_id TYPE varchar(64);

-- versions
ALTER TABLE versions ALTER COLUMN created_by TYPE varchar(64);
