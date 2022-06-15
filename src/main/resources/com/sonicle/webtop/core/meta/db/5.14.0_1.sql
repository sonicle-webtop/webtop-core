@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Historicize OLD audit_log (and related)
-- ----------------------------
@IgnoreErrors
ALTER TABLE "core"."audit_log_bck" RENAME TO "audit_log_bck.old";
DROP INDEX IF EXISTS "core"."audit_log_ak1";
DROP INDEX IF EXISTS "core"."audit_log_ak2";
ALTER TABLE "core"."audit_log" RENAME TO "audit_log.old";
ALTER SEQUENCE "core"."seq_audit_log" RENAME TO "seq_audit_log.old";

-- ----------------------------
-- Table audit_log
-- ----------------------------
CREATE SEQUENCE "core"."seq_audit_log";

CREATE TABLE "core"."audit_log" (
"audit_log_id" int8 NOT NULL DEFAULT nextval('"core".seq_audit_log'::regclass),
"timestamp" timestamptz NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"software_name" varchar(255),
"session_id" varchar(255),
"service_id" varchar(255) NOT NULL,
"context" varchar(50) NOT NULL,
"action" varchar(50) NOT NULL,
"reference_id" varchar(255),
"data" varchar(10000)
);
ALTER TABLE "core"."audit_log" ADD PRIMARY KEY ("audit_log_id");
CREATE INDEX "audit_log_ak1" ON "core"."audit_log" ("domain_id", "service_id", "context", "action", "reference_id");
CREATE INDEX "audit_log_ak2" ON "core"."audit_log" ("domain_id", "service_id", "context", "reference_id");

-- ----------------------------
-- Table access_log
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_access_log";
CREATE SEQUENCE "core"."seq_access_log";

CREATE TABLE "core"."access_log" (
"access_log_id" int8 NOT NULL DEFAULT nextval('"core".seq_access_log'::regclass),
"timestamp" timestamptz NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"software_name" varchar(255),
"session_id" varchar(255),
"service_id" varchar(255) NOT NULL,
"context" varchar(50) NOT NULL,
"action" varchar(50) NOT NULL,
"reference_id" varchar(255),
"data" varchar(10000)
);
ALTER TABLE "core"."access_log" ADD PRIMARY KEY ("access_log_id");
CREATE INDEX "access_log_ak1" ON "core"."access_log" ("domain_id", "user_id", "software_name", "session_id", "service_id", "context", "action");
CREATE INDEX "access_log_ak2" ON "core"."access_log" ("domain_id", "service_id", "context", "timestamp");

-- ----------------------------
-- Copy data into audit_log
-- ----------------------------
INSERT INTO "core"."audit_log" ("timestamp", "domain_id", "user_id", "session_id", "service_id", "context", "action", "reference_id", "data")
SELECT "timestamp", "domain_id", "user_id", "session_id", "service_id", "context", "action", "reference_id", "data"
FROM "core"."audit_log.old"
WHERE "service_id" <> 'com.sonicle.webtop.core' OR "context" <> 'AUTH'
ORDER BY "audit_log_id" ASC;

-- ----------------------------
-- Copy data into access_log
-- ----------------------------
INSERT INTO "core"."access_log" ("timestamp", "domain_id", "user_id", "session_id", "service_id", "context", "action", "reference_id", "data")
SELECT "timestamp", "domain_id", "user_id", "session_id", "service_id", "context", "action", "reference_id", "data"
FROM "core"."audit_log.old"
WHERE "service_id" = 'com.sonicle.webtop.core' AND "context" = 'AUTH'
ORDER BY "audit_log_id" ASC;

-- ----------------------------
-- Clear OLD audit_log
-- ----------------------------
DROP VIEW IF EXISTS "core"."vw_auth_details";
DROP VIEW IF EXISTS "core"."vw_access_log";
DROP TABLE IF EXISTS "core"."audit_log.old";

-- ----------------------------
-- Clear deprecated settings
-- ----------------------------
DELETE FROM "core"."settings" WHERE "key" = 'audit.enabled';
DELETE FROM "core"."domain_settings" WHERE "key" = 'audit.enabled';
