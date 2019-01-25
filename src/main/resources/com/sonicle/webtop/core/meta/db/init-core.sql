@DataSource[default@com.sonicle.webtop.core]

CREATE SCHEMA "core";

-- ----------------------------
-- Sequence structure for seq_activities
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_activities";
CREATE SEQUENCE "core"."seq_activities";

-- ----------------------------
-- Sequence structure for seq_causals
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_causals";
CREATE SEQUENCE "core"."seq_causals";

-- ----------------------------
-- Sequence structure for seq_im_chats
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_im_chats";
CREATE SEQUENCE "core"."seq_im_chats";

-- ----------------------------
-- Sequence structure for seq_im_messages
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_im_messages";
CREATE SEQUENCE "core"."seq_im_messages";

-- ----------------------------
-- Sequence structure for seq_messages_queue
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_messages_queue";
CREATE SEQUENCE "core"."seq_messages_queue";

-- ----------------------------
-- Sequence structure for seq_roles_associations
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_roles_associations";
CREATE SEQUENCE "core"."seq_roles_associations";

-- ----------------------------
-- Sequence structure for seq_roles_permissions
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_roles_permissions";
CREATE SEQUENCE "core"."seq_roles_permissions";

-- ----------------------------
-- Sequence structure for seq_shares
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_shares";
CREATE SEQUENCE "core"."seq_shares";

-- ----------------------------
-- Sequence structure for seq_snoozed_reminders
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_snoozed_reminders";
CREATE SEQUENCE "core"."seq_snoozed_reminders";

-- ----------------------------
-- Sequence structure for seq_syslog
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_syslog";
CREATE SEQUENCE "core"."seq_syslog";

-- ----------------------------
-- Sequence structure for seq_upgrade_statements
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_upgrade_statements";
CREATE SEQUENCE "core"."seq_upgrade_statements";

-- ----------------------------
-- Sequence structure for seq_users_associations
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_users_associations";
CREATE SEQUENCE "core"."seq_users_associations";

-- ----------------------------
-- Table structure for activities
-- ----------------------------
DROP TABLE IF EXISTS "core"."activities";
CREATE TABLE "core"."activities" (
"activity_id" int4 DEFAULT nextval('"core".seq_activities'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"revision_status" varchar(1) NOT NULL,
"description" varchar(255) NOT NULL,
"read_only" bool DEFAULT false NOT NULL,
"external_id" varchar(100)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for autosave
-- ----------------------------
DROP TABLE IF EXISTS "core"."autosave";
CREATE TABLE "core"."autosave" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"webtop_client_id" char(36) NOT NULL,
"service_id" varchar(255) NOT NULL,
"context" varchar(50) NOT NULL,
"key" varchar(100) NOT NULL,
"value" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for causals
-- ----------------------------
DROP TABLE IF EXISTS "core"."causals";
CREATE TABLE "core"."causals" (
"causal_id" int4 DEFAULT nextval('"core".seq_causals'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"master_data_id" varchar(36),
"revision_status" varchar(1) NOT NULL,
"description" varchar(255) NOT NULL,
"read_only" bool DEFAULT false NOT NULL,
"external_id" varchar(100)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for domain_settings
-- ----------------------------
DROP TABLE IF EXISTS "core"."domain_settings";
CREATE TABLE "core"."domain_settings" (
"domain_id" varchar(20) NOT NULL,
"service_id" varchar(255) NOT NULL,
"key" varchar(255) NOT NULL,
"value" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for domains
-- ----------------------------
DROP TABLE IF EXISTS "core"."domains";
CREATE TABLE "core"."domains" (
"domain_id" varchar(20) NOT NULL,
"internet_name" varchar(255) NOT NULL,
"enabled" bool NOT NULL,
"description" varchar(100) NOT NULL,
"user_auto_creation" bool NOT NULL,
"dir_uri" varchar(255) NOT NULL,
"dir_admin" varchar(255),
"dir_password" varchar(255),
"dir_connection_security" varchar(10),
"dir_case_sensitive" bool NOT NULL,
"dir_password_policy" bool NOT NULL,
"dir_parameters" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for file_types
-- ----------------------------
DROP TABLE IF EXISTS "core"."file_types";
CREATE TABLE "core"."file_types" (
"extension" varchar(255) NOT NULL,
"type" varchar(255) NOT NULL,
"subtype" varchar(255) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for im_chats
-- ----------------------------
DROP TABLE IF EXISTS "core"."im_chats";
CREATE TABLE "core"."im_chats" (
"id" int4 DEFAULT nextval('"core".seq_im_chats'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"chat_jid" varchar(255) NOT NULL,
"owner_jid" varchar(255) NOT NULL,
"name" varchar(255) NOT NULL,
"is_group_chat" bool NOT NULL,
"last_seen_activity" timestamptz(6),
"with_jid" varchar(255)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for im_messages
-- ----------------------------
DROP TABLE IF EXISTS "core"."im_messages";
CREATE TABLE "core"."im_messages" (
"id" int4 DEFAULT nextval('"core".seq_im_messages'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"chat_jid" varchar(255) NOT NULL,
"sender_jid" varchar(255) NOT NULL,
"sender_resource" varchar(255),
"timestamp" timestamptz(6) NOT NULL,
"delivery_timestamp" timestamptz(6),
"action" varchar(10) NOT NULL,
"text" text,
"data" text,
"message_uid" varchar(255) NOT NULL,
"stanza_id" varchar(255)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for local_vault
-- ----------------------------
DROP TABLE IF EXISTS "core"."local_vault";
CREATE TABLE "core"."local_vault" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"password_type" varchar(15) NOT NULL,
"password" varchar(128)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for master_data
-- ----------------------------
DROP TABLE IF EXISTS "core"."master_data";
CREATE TABLE "core"."master_data" (
"domain_id" varchar(20) NOT NULL,
"master_data_id" varchar(36) NOT NULL,
"parent_master_data_id" varchar(36),
"external_id" varchar(36),
"type" varchar(3) NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int4 DEFAULT 0 NOT NULL,
"lock_status" varchar(1),
"description" varchar(50),
"address" varchar(100),
"city" varchar(50),
"postal_code" varchar(20),
"state" varchar(30),
"country" varchar(50),
"telephone" varchar(50),
"fax" varchar(50),
"mobile" varchar(50),
"email" varchar(320),
"notes" varchar(2000),
"distance" int4
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for media_types
-- ----------------------------
DROP TABLE IF EXISTS "core"."media_types";
CREATE TABLE "core"."media_types" (
"extension" varchar(255) NOT NULL,
"media_type" varchar(255) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for messages_queue
-- ----------------------------
DROP TABLE IF EXISTS "core"."messages_queue";
CREATE TABLE "core"."messages_queue" (
"queue_id" int4 DEFAULT nextval('"core".seq_messages_queue'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"message_type" varchar(255) NOT NULL,
"message_raw" text NOT NULL,
"queued_on" timestamptz(6) NOT NULL,
"pid" varchar(36)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS "core"."roles";
CREATE TABLE "core"."roles" (
"role_uid" varchar(36) NOT NULL,
"domain_id" varchar(20),
"name" varchar(50),
"description" varchar(100)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for roles_associations
-- ----------------------------
DROP TABLE IF EXISTS "core"."roles_associations";
CREATE TABLE "core"."roles_associations" (
"role_association_id" int4 DEFAULT nextval('"core".seq_roles_associations'::regclass) NOT NULL,
"user_uid" varchar(36) NOT NULL,
"role_uid" varchar(36) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for roles_permissions
-- ----------------------------
DROP TABLE IF EXISTS "core"."roles_permissions";
CREATE TABLE "core"."roles_permissions" (
"role_permission_id" int4 DEFAULT nextval('"core".seq_roles_permissions'::regclass) NOT NULL,
"role_uid" varchar(36) NOT NULL,
"service_id" varchar(255) NOT NULL,
"key" varchar(255) NOT NULL,
"action" varchar(255) NOT NULL,
"instance" varchar(255) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for servicestore_entries
-- ----------------------------
DROP TABLE IF EXISTS "core"."servicestore_entries";
CREATE TABLE "core"."servicestore_entries" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"service_id" varchar(255) NOT NULL,
"context" varchar(50) NOT NULL,
"key" varchar(1024) NOT NULL,
"value" varchar(1024) NOT NULL,
"frequency" int4 NOT NULL,
"last_update" timestamptz(6) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for settings
-- ----------------------------
DROP TABLE IF EXISTS "core"."settings";
CREATE TABLE "core"."settings" (
"service_id" varchar(255) NOT NULL,
"key" varchar(255) NOT NULL,
"value" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for settings_db
-- ----------------------------
DROP TABLE IF EXISTS "core"."settings_db";
CREATE TABLE "core"."settings_db" (
"service_id" varchar(255) NOT NULL,
"key" varchar(255) NOT NULL,
"is_system" bool DEFAULT false NOT NULL,
"is_domain" bool DEFAULT false NOT NULL,
"is_user" bool DEFAULT false NOT NULL,
"hidden" bool DEFAULT false NOT NULL,
"type" varchar(20) NOT NULL,
"help" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for shares
-- ----------------------------
DROP TABLE IF EXISTS "core"."shares";
CREATE TABLE "core"."shares" (
"share_id" int4 DEFAULT nextval('"core".seq_shares'::regclass) NOT NULL,
"user_uid" varchar(36) NOT NULL,
"service_id" varchar(255) NOT NULL,
"key" varchar(255) NOT NULL,
"instance" varchar(255) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for shares_data
-- ----------------------------
DROP TABLE IF EXISTS "core"."shares_data";
CREATE TABLE "core"."shares_data" (
"share_id" int4 NOT NULL,
"user_uid" varchar(36) NOT NULL,
"value" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for snoozed_reminders
-- ----------------------------
DROP TABLE IF EXISTS "core"."snoozed_reminders";
CREATE TABLE "core"."snoozed_reminders" (
"snoozed_reminder_id" int4 DEFAULT nextval('"core".seq_snoozed_reminders'::regclass) NOT NULL,
"domain_id" varchar(20),
"user_id" varchar(100),
"service_id" varchar(255),
"type" varchar(50),
"instance_id" varchar(255),
"remind_on" timestamptz(6),
"title" varchar(100),
"date" timestamptz(6),
"timezone" varchar(50)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for syslog
-- ----------------------------
DROP TABLE IF EXISTS "core"."syslog";
CREATE TABLE "core"."syslog" (
"syslog_id" int8 DEFAULT nextval('"core".seq_syslog'::regclass) NOT NULL,
"timestamp" timestamptz(6),
"domain_id" varchar(20),
"user_id" varchar(100),
"service_id" varchar(255),
"action" varchar(50),
"sw_name" varchar(50),
"ip_address" varchar(39),
"user_agent" varchar(512),
"session_id" varchar(255),
"data" varchar(255)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for upgrade_statements
-- ----------------------------
DROP TABLE IF EXISTS "core"."upgrade_statements";
CREATE TABLE "core"."upgrade_statements" (
"upgrade_statement_id" int4 DEFAULT nextval('"core".seq_upgrade_statements'::regclass) NOT NULL,
"tag" varchar(20) NOT NULL,
"service_id" varchar(255) NOT NULL,
"sequence_no" int2 NOT NULL,
"script_name" varchar(255) NOT NULL,
"statement_type" varchar(10) NOT NULL,
"statement_data_source" varchar(255),
"statement_body" text,
"run_status" varchar(255),
"run_timestamp" timestamp(6),
"run_message" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for user_settings
-- ----------------------------
DROP TABLE IF EXISTS "core"."user_settings";
CREATE TABLE "core"."user_settings" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"service_id" varchar(255) NOT NULL,
"key" varchar(255) NOT NULL,
"value" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS "core"."users";
CREATE TABLE "core"."users" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"type" varchar(1) NOT NULL,
"enabled" bool NOT NULL,
"user_uid" varchar(36) NOT NULL,
"display_name" varchar(100),
"secret" varchar(16)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for users_associations
-- ----------------------------
DROP TABLE IF EXISTS "core"."users_associations";
CREATE TABLE "core"."users_associations" (
"user_association_id" int4 DEFAULT nextval('"core".seq_users_associations'::regclass) NOT NULL,
"user_uid" varchar(36) NOT NULL,
"group_uid" varchar(36) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for users_info
-- ----------------------------
DROP TABLE IF EXISTS "core"."users_info";
CREATE TABLE "core"."users_info" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"searchfield" text,
"title" varchar(30),
"first_name" varchar(60),
"last_name" varchar(60),
"nickname" varchar(60),
"gender" varchar(6),
"email" varchar(320),
"telephone" varchar(50),
"fax" varchar(50),
"pager" varchar(50),
"mobile" varchar(50),
"address" varchar(100),
"city" varchar(50),
"postal_code" varchar(20),
"state" varchar(30),
"country" varchar(30),
"company" varchar(100),
"function" varchar(100),
"custom1" varchar(255),
"custom2" varchar(255),
"custom3" varchar(255)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Alter Sequences Owned By 
-- ----------------------------

-- ----------------------------
-- Indexes structure for table activities
-- ----------------------------
CREATE INDEX "activities_ak1" ON "core"."activities" USING btree ("domain_id", "user_id", "revision_status");
CREATE INDEX "activities_ak2" ON "core"."activities" USING btree ("external_id", "domain_id");

-- ----------------------------
-- Primary Key structure for table activities
-- ----------------------------
ALTER TABLE "core"."activities" ADD PRIMARY KEY ("activity_id");

-- ----------------------------
-- Indexes structure for table autosave
-- ----------------------------
CREATE INDEX "autosave_ak1" ON "core"."autosave" USING btree ("domain_id", "user_id", "service_id", "context", "key");

-- ----------------------------
-- Primary Key structure for table autosave
-- ----------------------------
ALTER TABLE "core"."autosave" ADD PRIMARY KEY ("domain_id", "user_id", "webtop_client_id", "service_id", "context", "key");

-- ----------------------------
-- Indexes structure for table causals
-- ----------------------------
CREATE INDEX "causals_ak1" ON "core"."causals" USING btree ("domain_id", "user_id", "master_data_id", "revision_status");
CREATE INDEX "causals_ak2" ON "core"."causals" USING btree ("external_id", "domain_id");

-- ----------------------------
-- Primary Key structure for table causals
-- ----------------------------
ALTER TABLE "core"."causals" ADD PRIMARY KEY ("causal_id");

-- ----------------------------
-- Primary Key structure for table domain_settings
-- ----------------------------
ALTER TABLE "core"."domain_settings" ADD PRIMARY KEY ("domain_id", "service_id", "key");

-- ----------------------------
-- Indexes structure for table domains
-- ----------------------------
CREATE INDEX "domains_ak1" ON "core"."domains" USING btree ("domain_id", "enabled");
CREATE INDEX "domains_ak2" ON "core"."domains" USING btree ("internet_name", "enabled");

-- ----------------------------
-- Primary Key structure for table domains
-- ----------------------------
ALTER TABLE "core"."domains" ADD PRIMARY KEY ("domain_id");

-- ----------------------------
-- Primary Key structure for table file_types
-- ----------------------------
ALTER TABLE "core"."file_types" ADD PRIMARY KEY ("extension", "type", "subtype");

-- ----------------------------
-- Indexes structure for table im_chats
-- ----------------------------
CREATE INDEX "im_chats_ak1" ON "core"."im_chats" USING btree ("domain_id", "user_id", "revision_status");
CREATE INDEX "im_chats_ak2" ON "core"."im_chats" USING btree ("domain_id", "user_id", "chat_jid");

-- ----------------------------
-- Primary Key structure for table im_chats
-- ----------------------------
ALTER TABLE "core"."im_chats" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table im_messages
-- ----------------------------
CREATE INDEX "im_messages_ak1" ON "core"."im_messages" USING btree ("domain_id", "user_id", "chat_jid", "timestamp");
CREATE INDEX "im_messages_ak2" ON "core"."im_messages" USING btree ("domain_id", "user_id", "chat_jid", "delivery_timestamp");
CREATE INDEX "im_messages_ak3" ON "core"."im_messages" USING btree ("domain_id", "user_id", "chat_jid", "text");

-- ----------------------------
-- Primary Key structure for table im_messages
-- ----------------------------
ALTER TABLE "core"."im_messages" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table local_vault
-- ----------------------------
ALTER TABLE "core"."local_vault" ADD PRIMARY KEY ("domain_id", "user_id");

-- ----------------------------
-- Indexes structure for table master_data
-- ----------------------------
CREATE INDEX "master_data_ak1" ON "core"."master_data" USING btree ("domain_id", "parent_master_data_id", "type", "revision_status", "description");
CREATE INDEX "master_data_ak2" ON "core"."master_data" USING btree ("domain_id", "type", "revision_status", "description");
CREATE INDEX "master_data_ak3" ON "core"."master_data" USING btree ("external_id", "domain_id");

-- ----------------------------
-- Primary Key structure for table master_data
-- ----------------------------
ALTER TABLE "core"."master_data" ADD PRIMARY KEY ("domain_id", "master_data_id");

-- ----------------------------
-- Primary Key structure for table media_types
-- ----------------------------
ALTER TABLE "core"."media_types" ADD PRIMARY KEY ("extension", "media_type");

-- ----------------------------
-- Indexes structure for table messages_queue
-- ----------------------------
CREATE INDEX "messages_queue_ak1" ON "core"."messages_queue" USING btree ("domain_id", "user_id");
CREATE INDEX "messages_queue_ak2" ON "core"."messages_queue" USING btree ("pid");

-- ----------------------------
-- Primary Key structure for table messages_queue
-- ----------------------------
ALTER TABLE "core"."messages_queue" ADD PRIMARY KEY ("queue_id");

-- ----------------------------
-- Indexes structure for table roles
-- ----------------------------
CREATE INDEX "roles_ak1" ON "core"."roles" USING btree ("domain_id");

-- ----------------------------
-- Primary Key structure for table roles
-- ----------------------------
ALTER TABLE "core"."roles" ADD PRIMARY KEY ("role_uid");

-- ----------------------------
-- Indexes structure for table roles_associations
-- ----------------------------
CREATE UNIQUE INDEX "roles_associations_ak1" ON "core"."roles_associations" USING btree ("user_uid", "role_uid");
CREATE INDEX "roles_associations_ak2" ON "core"."roles_associations" USING btree ("role_uid");

-- ----------------------------
-- Primary Key structure for table roles_associations
-- ----------------------------
ALTER TABLE "core"."roles_associations" ADD PRIMARY KEY ("role_association_id");

-- ----------------------------
-- Indexes structure for table roles_permissions
-- ----------------------------
CREATE UNIQUE INDEX "roles_permissions_ak1" ON "core"."roles_permissions" USING btree ("role_uid", "service_id", "key", "action", "instance");
CREATE INDEX "roles_permissions_ak2" ON "core"."roles_permissions" USING btree ("role_uid", "service_id", "key", "instance");
CREATE INDEX "roles_permissions_ak3" ON "core"."roles_permissions" USING btree ("service_id", "key", "instance");

-- ----------------------------
-- Primary Key structure for table servicestore_entries
-- ----------------------------
ALTER TABLE "core"."servicestore_entries" ADD PRIMARY KEY ("domain_id", "user_id", "service_id", "context", "key");

-- ----------------------------
-- Primary Key structure for table settings
-- ----------------------------
ALTER TABLE "core"."settings" ADD PRIMARY KEY ("service_id", "key");

-- ----------------------------
-- Indexes structure for table settings_db
-- ----------------------------
CREATE INDEX "settings_db_ak1" ON "core"."settings_db" USING btree ("service_id", "key", "is_system", "hidden");
CREATE INDEX "settings_db_ak2" ON "core"."settings_db" USING btree ("service_id", "key", "is_domain", "hidden");
CREATE INDEX "settings_db_ak3" ON "core"."settings_db" USING btree ("service_id", "key", "is_user", "hidden");

-- ----------------------------
-- Primary Key structure for table settings_db
-- ----------------------------
ALTER TABLE "core"."settings_db" ADD PRIMARY KEY ("service_id", "key");

-- ----------------------------
-- Indexes structure for table shares
-- ----------------------------
CREATE UNIQUE INDEX "shares_ak1" ON "core"."shares" USING btree ("user_uid", "service_id", "key", "instance");

-- ----------------------------
-- Primary Key structure for table shares
-- ----------------------------
ALTER TABLE "core"."shares" ADD PRIMARY KEY ("share_id");

-- ----------------------------
-- Primary Key structure for table shares_data
-- ----------------------------
ALTER TABLE "core"."shares_data" ADD PRIMARY KEY ("share_id", "user_uid");

-- ----------------------------
-- Primary Key structure for table syslog
-- ----------------------------
ALTER TABLE "core"."syslog" ADD PRIMARY KEY ("syslog_id");

-- ----------------------------
-- Indexes structure for table upgrade_statements
-- ----------------------------
CREATE INDEX "upgrade_statements_ak1" ON "core"."upgrade_statements" USING btree ("tag", "service_id", "sequence_no");
CREATE INDEX "upgrade_statements_ak2" ON "core"."upgrade_statements" USING btree ("tag", "statement_type", "run_status");

-- ----------------------------
-- Primary Key structure for table upgrade_statements
-- ----------------------------
ALTER TABLE "core"."upgrade_statements" ADD PRIMARY KEY ("upgrade_statement_id");

-- ----------------------------
-- Primary Key structure for table user_settings
-- ----------------------------
ALTER TABLE "core"."user_settings" ADD PRIMARY KEY ("domain_id", "user_id", "service_id", "key");

-- ----------------------------
-- Indexes structure for table users
-- ----------------------------
CREATE UNIQUE INDEX "users_ak1" ON "core"."users" USING btree ("user_uid");
CREATE INDEX "users_ak3" ON "core"."users" USING btree ("domain_id", "type", "enabled");
CREATE INDEX "users_ak4" ON "core"."users" USING btree ("type", "user_uid");

-- ----------------------------
-- Primary Key structure for table users
-- ----------------------------
ALTER TABLE "core"."users" ADD PRIMARY KEY ("domain_id", "user_id", "type");

-- ----------------------------
-- Indexes structure for table users_associations
-- ----------------------------
CREATE UNIQUE INDEX "users_associations_ak1" ON "core"."users_associations" USING btree ("user_uid", "group_uid");
CREATE INDEX "users_associations_ak2" ON "core"."users_associations" USING btree ("group_uid");

-- ----------------------------
-- Primary Key structure for table users_associations
-- ----------------------------
ALTER TABLE "core"."users_associations" ADD PRIMARY KEY ("user_association_id");

-- ----------------------------
-- Primary Key structure for table users_info
-- ----------------------------
ALTER TABLE "core"."users_info" ADD PRIMARY KEY ("domain_id", "user_id");

-- ----------------------------
-- Align service version
-- ----------------------------
DELETE FROM "core"."settings" WHERE ("settings"."service_id" = 'com.sonicle.webtop.core') AND ("settings"."key" = 'manifest.version');
INSERT INTO "core"."settings" ("service_id", "key", "value") VALUES ('com.sonicle.webtop.core', 'manifest.version', '5.5.2');
