@DataSource[default@com.sonicle.webtop.core]

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
  ROWS 1000;
