@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Fix missing flag that can cause wrong initProfile calls
-- ----------------------------
INSERT INTO "core"."user_settings" ("domain_id", "user_id", "service_id", "key", "value")
(
SELECT "us"."domain_id", "us"."user_id", 'com.sonicle.webtop.core', 'initialized', 'true'
FROM "core"."user_settings" AS "us"
WHERE 
EXISTS (
SELECT 1
FROM "core"."user_settings" AS "us1"
WHERE "us1"."domain_id" = "us"."domain_id"
AND "us1"."user_id" = "us"."user_id"
AND "us1"."service_id" <> 'com.sonicle.webtop.core'
AND "us1"."key" = 'initialized'
)
AND NOT EXISTS (
SELECT 1
FROM "core"."user_settings" AS "us2"
WHERE "us2"."domain_id" = "us"."domain_id"
AND "us2"."user_id" = "us"."user_id"
AND "us2"."service_id" = 'com.sonicle.webtop.core'
AND "us2"."key" = 'initialized'
)
GROUP BY "us"."domain_id", "us"."user_id"
);
