@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Fix table configuration
-- ----------------------------

UPDATE "core"."shares" SET "instance" = '' WHERE ("instance" IS NULL);
ALTER TABLE "core"."shares"
ALTER COLUMN "user_uid" SET NOT NULL,
ALTER COLUMN "service_id" SET NOT NULL,
ALTER COLUMN "key" SET NOT NULL,
ALTER COLUMN "instance" SET NOT NULL;
