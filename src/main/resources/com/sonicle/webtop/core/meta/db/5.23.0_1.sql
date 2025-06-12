@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- New table: api_keys
-- ----------------------------
DROP TABLE IF EXISTS "core"."api_keys";
CREATE TABLE "core"."api_keys" (
"api_key_id" varchar(32) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"creation_timestamp" timestamptz NOT NULL,
"revision_timestamp" timestamptz NOT NULL,
"name" varchar(255) NOT NULL,
"description" varchar(255),
"short_token" varchar(8),
"long_token" varchar(255),
"expires_at" timestamptz,
"last_used_at" timestamptz
);

ALTER TABLE "core"."api_keys" ADD PRIMARY KEY ("api_key_id");
CREATE INDEX "api_keys_ak1" ON "core"."api_keys" ("domain_id");
