@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Add svg as well-known extension
-- ----------------------------
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('svg', 'image', '') ON CONFLICT DO NOTHING;

-- ----------------------------
-- Fix user_settings for nethesis theme
-- ----------------------------
UPDATE "core"."user_settings" 
SET "value" = 'nethesis-light'
WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'theme' AND "value" = 'nethesis';

UPDATE "core"."user_settings" 
SET "value" = 'nethesis@light'
WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'laf' AND "value" = 'nethesis';
