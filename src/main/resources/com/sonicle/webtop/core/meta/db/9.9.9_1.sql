@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Add new policy fields
-- ----------------------------
ALTER TABLE "core"."domains"
ADD COLUMN "dir_pwd_policy_min_length" int2,
ADD COLUMN "dir_pwd_policy_complexity" bool DEFAULT false NOT NULL,
ADD COLUMN "dir_pwd_policy_avoid_consecutive_chars" bool DEFAULT false NOT NULL,
ADD COLUMN "dir_pwd_policy_avoid_old_similarity" bool DEFAULT false NOT NULL,
ADD COLUMN "dir_pwd_policy_avoid_username_similarity" bool DEFAULT false NOT NULL,
ADD COLUMN "dir_pwd_policy_expiration" int2,
ADD COLUMN "dir_pwd_policy_verify_at_login" bool DEFAULT false NOT NULL;

ALTER TABLE "core"."domains"
ALTER COLUMN "dir_password_policy" DROP NOT NULL;

-- ----------------------------
-- Populate data according to column "dir_password_policy"
-- ----------------------------
UPDATE "core"."domains" SET "dir_pwd_policy_complexity" = "dir_password_policy";
UPDATE "core"."domains" SET "dir_pwd_policy_min_length" = 8 WHERE "dir_password_policy" IS TRUE;

-- ----------------------------
-- Clear deprecated settings
-- ----------------------------
DELETE FROM "core"."settings" WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'password.forcechangeifpolicyunmet';
DELETE FROM "core"."domain_settings" WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'password.forcechangeifpolicyunmet';

-- ----------------------------
-- Create views for auth logs pages
-- ----------------------------
CREATE VIEW "core"."vw_access_log" AS 
SELECT 
  crosstab.row_details [ 1 ] AS session_id, 
  crosstab.row_details [ 2 ] AS domain_id, 
  crosstab.row_details [ 3 ] AS user_id, 
  crosstab.DATA, 
  crosstab."LOGIN_SUCCESS", 
  crosstab."OTP_SUCCESS", 
  crosstab."OTP_FAILURE", 
  crosstab."OTP_ABANDONED", 
  crosstab."AUTHENTICATED", 
  crosstab."LOGOUT", 
  crosstab."SESSION_EXPIRED", 
  crosstab."LOGIN_FAILURE" 
FROM 
  crosstab (
    'select ARRAY[session_id, domain_id, user_id]::text[] as row_details, session_id, domain_id, user_id, "data", "action", "timestamp" from core.audit_log where 
    service_id=''com.sonicle.webtop.core'' and context=''AUTH'' order by session_id, user_id, action, "timestamp"' :: TEXT, 
    'select * from (values (''LOGIN_SUCCESS''),(''OTP_SUCCESS''),(''OTP_FAILURE''),(''OTP_ABANDONED''),(''AUTHENTICATED''),(''LOGOUT''),(''SESSION_EXPIRED''),(''LOGIN_FAILURE'')) as actions' :: TEXT
  ) crosstab (
    row_details CHARACTER VARYING [], session_id CHARACTER VARYING, 
    domain_id CHARACTER VARYING, user_id CHARACTER VARYING, 
    DATA CHARACTER VARYING, "LOGIN_SUCCESS" TIMESTAMP WITH TIME ZONE, 
    "OTP_SUCCESS" TIMESTAMP WITH TIME ZONE, 
    "OTP_FAILURE" TIMESTAMP WITH TIME ZONE, 
    "OTP_ABANDONED" TIMESTAMP WITH TIME ZONE, 
    "AUTHENTICATED" TIMESTAMP WITH TIME ZONE, 
    "LOGOUT" TIMESTAMP WITH TIME ZONE, 
    "SESSION_EXPIRED" TIMESTAMP WITH TIME ZONE, 
    "LOGIN_FAILURE" TIMESTAMP WITH TIME ZONE
  );

CREATE VIEW "core"."vw_auth_details" AS 
SELECT 
  vw_access_log.session_id, 
  vw_access_log.domain_id, 
  vw_access_log.user_id, 
  COALESCE (
    vw_access_log."LOGIN_SUCCESS", vw_access_log."LOGIN_FAILURE"
  ) AS DATE, 
  date_part(
    'minute' :: TEXT, 
    (
      COALESCE (
        vw_access_log."LOGOUT", vw_access_log."SESSION_EXPIRED"
      ) - vw_access_log."LOGIN_SUCCESS"
    )
  ) AS minutes, 
  CASE WHEN (
    vw_access_log."AUTHENTICATED" IS NULL
  ) THEN FALSE ELSE TRUE END AS authenticated, 
  CASE WHEN (
    vw_access_log."LOGIN_FAILURE" IS NULL 
    AND vw_access_log."OTP_FAILURE" IS NULL
  ) THEN FALSE ELSE TRUE END AS failure, 
  (
    SELECT 
      COUNT (*) AS COUNT 
    FROM 
      core.audit_log 
    WHERE 
      (audit_log.session_id) :: TEXT = (vw_access_log.session_id) :: TEXT 
      AND (audit_log.domain_id) :: TEXT = (vw_access_log.domain_id) :: TEXT 
      AND (audit_log.user_id) :: TEXT = (vw_access_log.user_id) :: TEXT 
      AND (audit_log.ACTION) :: TEXT = 'LOGIN_FAILURE' :: TEXT
  ) :: INTEGER AS login_errors, 
  vw_access_log.DATA 
FROM 
  core.vw_access_log 
WHERE 
  vw_access_log."LOGIN_SUCCESS" IS NOT NULL 
  OR vw_access_log."LOGIN_FAILURE" IS NOT NULL 
ORDER BY 
  COALESCE (
    vw_access_log."LOGIN_SUCCESS", vw_access_log."LOGIN_FAILURE"
  );

-- ----------------------------
-- Clear audit_log table
-- ----------------------------
DELETE FROM core.audit_log WHERE context = 'AUTH';
