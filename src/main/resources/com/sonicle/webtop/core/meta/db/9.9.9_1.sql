@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Historicize OLD audit_log (and related)
-- ----------------------------
ALTER TABLE IF EXISTS "core"."audit_log_bck" RENAME TO "audit_log_bck.old";
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
-- Function fn_access_log_aggr
-- TODO: later add support to software_name
-- ----------------------------
CREATE OR REPLACE FUNCTION "core"."fn_access_log_aggr"("domain_id" varchar, "from_date" timestamptz, "to_date" timestamptz)
  RETURNS TABLE("user_id" varchar, "session_id" varchar, "timestamp" timestamptz, "duration" int4, "authenticated" bool, "failure" bool, "login_errors" int4, "data" varchar) AS $BODY$

SELECT
	subq.user_id,
	subq.session_id,
	COALESCE(subq."LOGIN_SUCCESS", subq."LOGIN_FAILURE") AS timestamp,
	floor(date_part('epoch'::text, COALESCE(subq."LOGOUT", subq."SESSION_EXPIRED") - subq."LOGIN_SUCCESS") / 60::double precision)::int AS duration, 
	CASE
		WHEN subq."AUTHENTICATED" IS NULL THEN false
    ELSE true
  END AS authenticated, 
  CASE
    WHEN subq."LOGIN_FAILURE" IS NULL AND subq."OTP_FAILURE" IS NULL THEN false
    ELSE true
  END AS failure,
	((
		SELECT count(*) AS count
		FROM core.access_log
		WHERE access_log.domain_id::text = subq.domain_id::text AND access_log.user_id::text = subq.user_id::text AND access_log.session_id::text = subq.session_id::text AND access_log.service_id::text = 'com.sonicle.webtop.core'::text AND access_log.context::text = 'AUTH'::text AND access_log.action::text = 'LOGIN_FAILURE'::text
	))::integer AS login_errors,
	subq.data
  FROM (
		SELECT
			crosstab.row_details [ 1 ] AS session_id,
			crosstab.row_details [ 2 ] AS domain_id,
			crosstab.row_details [ 3 ] AS user_id,
			crosstab.data,
			crosstab."LOGIN_SUCCESS",
			crosstab."OTP_SUCCESS",
			crosstab."OTP_FAILURE",
			crosstab."OTP_ABANDONED",
			crosstab."AUTHENTICATED",
			crosstab."LOGOUT",
			crosstab."SESSION_EXPIRED",
			crosstab."LOGIN_FAILURE" 
		FROM
			crosstab ('SELECT ARRAY[session_id, domain_id, user_id]::text[] AS row_details, session_id, domain_id, user_id, "data", "action", "timestamp" FROM core.access_log WHERE domain_id='''||$1||''' AND service_id=''com.sonicle.webtop.core'' AND context=''AUTH'' AND timestamp >= '''||$2||'''::timestamptz AND timestamp < '''||$3||'''::timestamptz ORDER BY session_id, user_id, action, "timestamp" ' :: TEXT, 'SELECT * FROM (values (''LOGIN_SUCCESS''),(''OTP_SUCCESS''),(''OTP_FAILURE''),(''OTP_ABANDONED''),(''AUTHENTICATED''),(''LOGOUT''),(''SESSION_EXPIRED''),(''LOGIN_FAILURE'')) AS actions' :: TEXT )
			crosstab (
				row_details CHARACTER VARYING [],
				session_id CHARACTER VARYING,
				domain_id CHARACTER VARYING,
				user_id CHARACTER VARYING,
				data CHARACTER VARYING,
				"LOGIN_SUCCESS" TIMESTAMP WITH TIME ZONE,
				"OTP_SUCCESS" TIMESTAMP WITH TIME ZONE,
				"OTP_FAILURE" TIMESTAMP WITH TIME ZONE,
				"OTP_ABANDONED" TIMESTAMP WITH TIME ZONE,
				"AUTHENTICATED" TIMESTAMP WITH TIME ZONE,
				"LOGOUT" TIMESTAMP WITH TIME ZONE,
				"SESSION_EXPIRED" TIMESTAMP WITH TIME ZONE,
				"LOGIN_FAILURE" TIMESTAMP WITH TIME ZONE
			)
   ) as subq 
   WHERE (subq."LOGIN_SUCCESS" IS NOT NULL OR subq."LOGIN_FAILURE" IS NOT null)
   ORDER BY COALESCE(subq."LOGIN_SUCCESS", subq."LOGIN_FAILURE");
	 
$BODY$
  LANGUAGE sql VOLATILE
  COST 100
  ROWS 1000

-- ----------------------------
-- Clear OLD audit_log
-- ----------------------------
DROP TABLE IF EXISTS "core"."audit_log.old";
