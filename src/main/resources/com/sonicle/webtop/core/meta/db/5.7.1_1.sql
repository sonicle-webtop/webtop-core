@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_audit_log";
CREATE SEQUENCE "core"."seq_audit_log";

CREATE TABLE "core"."audit_log" (
"audit_log_id" int8 DEFAULT nextval('"core".seq_audit_log') NOT NULL,
"timestamp" timestamptz(6),
"domain_id" varchar(20),
"user_id" varchar(100),
"service_id" varchar(255),
"context" varchar(50),
"action" varchar(50),
"reference_id" varchar(255),
"ip_address" varchar(39),
"session_id" varchar(255),
"data" text
);
-- ----------------------------
-- Primary Key structure for table syslog
-- ----------------------------
ALTER TABLE "core"."audit_log" ADD PRIMARY KEY ("audit_log_id");

-- ----------------------------
-- Specific index for table syslog
-- ----------------------------
CREATE INDEX "audit_log_index_1" ON "core"."audit_log" ("domain_id", "service_id", "context", "action", "reference_id");

