@DataSource[default@com.sonicle.webtop.core]

UPDATE "core"."user_settings" 
SET "value" = 'nethesis-light'
WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'theme' AND "value" = 'nethesis';

UPDATE "core"."user_settings" 
SET "value" = 'nethesis@light'
WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'laf' AND "value" = 'nethesis';
