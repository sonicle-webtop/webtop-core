@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- New table: data_sources
-- ----------------------------
CREATE TABLE "core"."data_sources" (
"data_source_id" varchar(32) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"name" varchar(255) NOT NULL,
"description" varchar(255),
"type" varchar(255) NOT NULL,
"server_name" varchar(255) NOT NULL,
"server_port" int4,
"database_name" varchar(255) NOT NULL,
"username" varchar(255),
"password" varchar(255),
"driver_raw_props" varchar(255),
"pool_raw_props" varchar(255)
);

ALTER TABLE "core"."data_sources" ADD PRIMARY KEY ("data_source_id");
CREATE INDEX "data_sources_ak1" ON "core"."data_sources" ("domain_id");


-- ----------------------------
-- New table: data_sources_queries
-- ----------------------------
CREATE TABLE "core"."data_sources_queries" (
"query_id" varchar(32) NOT NULL,
"data_source_id" varchar(32) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"name" varchar(255) NOT NULL,
"description" varchar(255),
"raw_sql" text,
"force_pagination" bool DEFAULT false NOT NULL
);

ALTER TABLE "core"."data_sources_queries" ADD PRIMARY KEY ("query_id");
ALTER TABLE "core"."data_sources_queries" ADD FOREIGN KEY ("data_source_id") REFERENCES "core"."data_sources" ("data_source_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "data_sources_queries_ak1" ON "core"."data_sources_queries" ("data_source_id");

-- ----------------------------
-- Add new fields to custom fields/panels table
-- ----------------------------
ALTER TABLE "core"."custom_fields" ADD COLUMN "data_source_query_id" varchar(32);
ALTER TABLE "core"."custom_panels" ADD COLUMN "properties" text;
