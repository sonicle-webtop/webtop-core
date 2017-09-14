@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Fix im_messages
-- ----------------------------

ALTER TABLE "core"."im_messages" ALTER COLUMN "text" TYPE text;
