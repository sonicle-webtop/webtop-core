@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- New table: audit_log
-- ----------------------------
CREATE SEQUENCE "core"."seq_audit_log";

CREATE TABLE "core"."audit_log" (
"audit_log_id" int8 DEFAULT nextval('"core".seq_audit_log') NOT NULL,
"timestamp" timestamptz(6) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"service_id" varchar(255) NOT NULL,
"context" varchar(50) NOT NULL,
"action" varchar(50) NOT NULL,
"reference_id" varchar(255),
"session_id" varchar(255),
"data" text
)
WITH (OIDS=FALSE)

;

ALTER TABLE "core"."audit_log" ADD PRIMARY KEY ("audit_log_id");
CREATE INDEX "audit_log_ak1" ON "core"."audit_log" ("domain_id", "service_id", "context", "action", "reference_id");

-- ----------------------------
-- New table: tags
-- ----------------------------
CREATE TABLE "core"."tags" (
"tag_id" varchar(22) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(255) NOT NULL,
"built_in" bool NOT NULL,
"name" varchar(50) NOT NULL,
"color" varchar(20) NOT NULL
)
WITH (OIDS=FALSE)

;

ALTER TABLE "core"."tags" ADD PRIMARY KEY ("tag_id");
CREATE INDEX "tags_ak1" ON "core"."tags" USING btree ("domain_id", "user_id");
CREATE INDEX "tags_ak2" ON "core"."tags" USING btree ("built_in", "name");

-- ----------------------------
-- Table structure for custom_fields
-- ----------------------------
CREATE TABLE "core"."custom_fields" (
"custom_field_id" varchar(22) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"service_id" varchar(255) NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"creation_timestamp" timestamptz(6) NOT NULL,
"name" varchar(255) NOT NULL,
"description" varchar(255),
"type" varchar(20) NOT NULL,
"searchable" bool DEFAULT false NOT NULL,
"previewable" bool DEFAULT false NOT NULL,
"properties" text,
"values" text,
"label_i18n" text
)
WITH (OIDS=FALSE)

;

ALTER TABLE "core"."custom_fields" ADD PRIMARY KEY ("custom_field_id");
CREATE INDEX "custom_fields_ak1" ON "core"."custom_fields" USING btree ("domain_id", "service_id", "revision_status", "name");
CREATE UNIQUE INDEX "custom_fields_ak2" ON "core"."custom_fields" USING btree ("domain_id", "service_id", "name") WHERE revision_status::text <> 'D'::text;
CREATE INDEX "custom_fields_ak3" ON "core"."custom_fields" USING btree ("domain_id", "service_id", "revision_status", "searchable");
CREATE INDEX "custom_fields_ak4" ON "core"."custom_fields" USING btree ("domain_id", "service_id", "revision_status", "previewable");

-- ----------------------------
-- New table: custom_panels
-- ----------------------------
CREATE TABLE "core"."custom_panels" (
"custom_panel_id" varchar(22) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"service_id" varchar(255) NOT NULL,
"order" int2 NOT NULL,
"name" varchar(50) NOT NULL,
"description" varchar(255),
"title_i18n" text
)
WITH (OIDS=FALSE)

;

ALTER TABLE "core"."custom_panels" ADD PRIMARY KEY ("custom_panel_id");
CREATE INDEX "custom_panels_ak1" ON "core"."custom_panels" USING btree ("domain_id", "service_id", "order", "name");

-- ----------------------------
-- New table: custom_panels_fields
-- ----------------------------
CREATE TABLE "core"."custom_panels_fields" (
"custom_panel_id" varchar(22) NOT NULL,
"custom_field_id" varchar(22) NOT NULL,
"order" int2 NOT NULL
)
WITH (OIDS=FALSE)

;

ALTER TABLE "core"."custom_panels_fields" ADD PRIMARY KEY ("custom_panel_id", "custom_field_id");
ALTER TABLE "core"."custom_panels_fields" ADD FOREIGN KEY ("custom_panel_id") REFERENCES "core"."custom_panels" ("custom_panel_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "core"."custom_panels_fields" ADD FOREIGN KEY ("custom_field_id") REFERENCES "core"."custom_fields" ("custom_field_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "custom_panels_fields_ak1" ON "core"."custom_panels_fields" USING btree ("custom_panel_id", "order");
CREATE INDEX "custom_panels_fields_ak2" ON "core"."custom_panels_fields" USING btree ("custom_field_id");

-- ----------------------------
-- New table: custom_panels_tags
-- ----------------------------
CREATE TABLE "core"."custom_panels_tags" (
"custom_panel_id" varchar(22) NOT NULL,
"tag_id" varchar(22) NOT NULL
)
WITH (OIDS=FALSE)

;

ALTER TABLE "core"."custom_panels_tags" ADD PRIMARY KEY ("custom_panel_id", "tag_id");
ALTER TABLE "core"."custom_panels_tags" ADD FOREIGN KEY ("custom_panel_id") REFERENCES "core"."custom_panels" ("custom_panel_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "core"."custom_panels_tags" ADD FOREIGN KEY ("tag_id") REFERENCES "core"."tags" ("tag_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- New table: licenses
-- ----------------------------
CREATE TABLE "core"."licenses" (
"service_id" varchar(255) NOT NULL,
"product_id" varchar(255) NOT NULL,
"internet_name" varchar(255) NOT NULL,
"license" text
)
WITH (OIDS=FALSE)

;

ALTER TABLE "core"."licenses" ADD PRIMARY KEY ("service_id", "product_id", "internet_name");
