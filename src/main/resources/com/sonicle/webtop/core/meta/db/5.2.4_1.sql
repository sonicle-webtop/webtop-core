@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Fix layout setting values
-- ----------------------------

UPDATE "core"."user_settings"
SET "value" = 'default'
WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'layout' AND "value" IN ('stacked', 'queued');
