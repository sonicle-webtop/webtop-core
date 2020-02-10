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
