@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Clear deprecated settings
-- ----------------------------
DELETE FROM "core"."settings" WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'test.newhtmleditor';
DELETE FROM "core"."domain_settings" WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'test.newhtmleditor';
DELETE FROM "core"."user_settings" WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'test.newhtmleditor';
