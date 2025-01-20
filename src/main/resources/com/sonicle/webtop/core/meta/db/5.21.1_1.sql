@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Consolidate data for new 'service.version' setting
-- ----------------------------
INSERT INTO "core"."user_settings" (SELECT "domain_id", "user_id", "service_id", 'service.version', "value" FROM "core"."user_settings" WHERE "key" = 'whatsnew.version');
