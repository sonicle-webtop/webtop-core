@DataSource[default@com.sonicle.webtop.core]

-- -------------------------------
-- Remove old vw_auth_details view
-- -------------------------------
drop VIEW "core"."vw_auth_details";

-- -------------------------------
-- Create new vw_auth_details view
-- -------------------------------

CREATE VIEW "core"."vw_auth_details" AS 
SELECT 
  vw_access_log.session_id, 
  vw_access_log.domain_id, 
  vw_access_log.user_id, 
  COALESCE (
    vw_access_log."LOGIN_SUCCESS", vw_access_log."LOGIN_FAILURE"
  ) AS DATE, 
	FLOOR ((
		date_part(
			'epoch' :: TEXT,
		( COALESCE ( vw_access_log."LOGOUT", vw_access_log."SESSION_EXPIRED" ) - vw_access_log."LOGIN_SUCCESS" )) / ( 60 ) :: DOUBLE PRECISION 
	)) AS minutes, 
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
