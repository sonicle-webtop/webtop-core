@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- New table: rememberme_tokens
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_rememberme_tokens";
CREATE SEQUENCE "core"."seq_rememberme_tokens";

DROP TABLE IF EXISTS "core"."rememberme_tokens";
CREATE TABLE "core"."rememberme_tokens" (
"rememberme_token_id" int8 DEFAULT nextval('"core".seq_rememberme_tokens'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"creation_timestamp" timestamptz NOT NULL,
"revision_timestamp" timestamptz NOT NULL,
"selector" varchar(32) NOT NULL,
"validator" varchar(255) NOT NULL,
"validator_prev" varchar(255),
"issued_at" timestamptz NOT NULL,
"expires_at" timestamptz NOT NULL,
"last_used_at" timestamptz,
"revoked" bool NOT NULL DEFAULT false,
"client_identifier" varchar(43),
"client_ip_address" varchar(45),
"client_user_agent" varchar(512)
);

ALTER TABLE "core"."rememberme_tokens" ADD PRIMARY KEY ("rememberme_token_id");
CREATE INDEX "rememberme_tokens_ak1" ON "core"."rememberme_tokens" ("domain_id", "user_id");
CREATE UNIQUE INDEX "rememberme_tokens_ak2" ON "core"."rememberme_tokens" ("selector");

-- ----------------------------
-- New table: trusted_devices
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_trusted_devices";
CREATE SEQUENCE "core"."seq_trusted_devices";

DROP TABLE IF EXISTS "core"."trusted_devices";
CREATE TABLE "core"."trusted_devices" (
"trusted_device_id" int8 DEFAULT nextval('"core".seq_trusted_devices'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"creation_timestamp" timestamptz NOT NULL,
"revision_timestamp" timestamptz NOT NULL,
"token" varchar(64) NOT NULL,
"client_identifier" varchar(43) NOT NULL,
"expires_at" timestamptz,
"last_used_at" timestamptz,
"revoked" bool NOT NULL DEFAULT false,
"client_ip_address" varchar(45),
"client_user_agent" varchar(512)
);

ALTER TABLE "core"."trusted_devices" ADD PRIMARY KEY ("trusted_device_id");
CREATE INDEX "trusted_devices_ak1" ON "core"."trusted_devices" ("domain_id", "user_id");
CREATE UNIQUE INDEX "trusted_devices_ak2" ON "core"."trusted_devices" ("token");

-- ----------------------------
-- Edit table: audit_known_devices
-- ----------------------------
ALTER TABLE "core"."audit_known_devices" 
ADD COLUMN "first_client_ip_address" varchar(45),
ADD COLUMN "last_client_ip_address" varchar(45),
ADD COLUMN "first_client_user_agent" varchar(512),
ADD COLUMN "last_client_user_agent" varchar(512),
ALTER COLUMN "device_id" TYPE varchar(43);

--COMMENT ON COLUMN "core"."audit_known_devices"."device_id" IS 'aka client_identifier';

DROP INDEX "core"."audit_known_devices_ak1";
CREATE UNIQUE INDEX "audit_known_devices_ak1" ON "core"."audit_known_devices" ("domain_id", "user_id", "device_id");

-- ----------------------------
-- Cleanup old TD data in settings
-- ----------------------------
DELETE FROM "core"."user_settings" WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" LIKE 'otp.trusteddevice@%';