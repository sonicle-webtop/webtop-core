@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- New table: auth_tokens
-- Backs the user-session bearer + refresh-token flow used by REST clients
-- (mobile app and similar). Tokens are stored only as SHA-256 hex digests;
-- plaintext tokens never hit the database.
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_auth_tokens";
CREATE SEQUENCE "core"."seq_auth_tokens";

DROP TABLE IF EXISTS "core"."auth_tokens";
CREATE TABLE "core"."auth_tokens" (
"auth_token_id" int8 DEFAULT nextval('"core".seq_auth_tokens'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"creation_timestamp" timestamptz NOT NULL,
"revision_timestamp" timestamptz NOT NULL,
"token" varchar(128) NOT NULL,
"type" varchar(8) NOT NULL,
"parent_id" int8,
"device_label" varchar(255),
"issued_at" timestamptz NOT NULL,
"expires_at" timestamptz NOT NULL,
"last_used_at" timestamptz,
"revoked_at" timestamptz,
"client_ip_address" varchar(45),
"client_user_agent" varchar(512)
);

ALTER TABLE "core"."auth_tokens" ADD PRIMARY KEY ("auth_token_id");
ALTER TABLE "core"."auth_tokens" ADD CONSTRAINT "auth_tokens_type_check" CHECK ("type" IN ('access', 'refresh'));
CREATE UNIQUE INDEX "auth_tokens_ak1" ON "core"."auth_tokens" ("token");
CREATE INDEX "auth_tokens_ak2" ON "core"."auth_tokens" ("domain_id", "user_id");
CREATE INDEX "auth_tokens_ak3" ON "core"."auth_tokens" ("parent_id");
CREATE INDEX "auth_tokens_ak4" ON "core"."auth_tokens" ("expires_at");

-- ----------------------------
-- New table: ai_usage
-- One row per AI completion call (mail "Ask A.I." menu, future RAG/embedding,
-- and any other webtop service consuming AIManager). Records token spend per
-- (domain, user) so reports can aggregate by day/week/month and an in-memory
-- daily counter can gate calls when a per-user/global cap is set.
-- Stores metrics only — never prompt or response content.
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_ai_usage";
CREATE SEQUENCE "core"."seq_ai_usage";

DROP TABLE IF EXISTS "core"."ai_usage";
CREATE TABLE "core"."ai_usage" (
"ai_usage_id" int8 DEFAULT nextval('"core".seq_ai_usage'::regclass) NOT NULL,
"timestamp" timestamptz NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"service_id" varchar(255) NOT NULL,
"operation" varchar(64) NOT NULL,
"backend_type" varchar(20) NOT NULL,
"model" varchar(100),
"prompt_tokens" int4,
"completion_tokens" int4,
"total_tokens" int4,
"duration_ms" int4,
"success" bool NOT NULL,
"error_short" text
);

ALTER TABLE "core"."ai_usage" ADD PRIMARY KEY ("ai_usage_id");
CREATE INDEX "ai_usage_ak1" ON "core"."ai_usage" ("domain_id", "user_id", "timestamp");
CREATE INDEX "ai_usage_ak2" ON "core"."ai_usage" ("timestamp");
