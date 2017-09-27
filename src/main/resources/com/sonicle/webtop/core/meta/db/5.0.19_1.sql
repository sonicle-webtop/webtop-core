@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Fix structure for master_data
-- ----------------------------

ALTER TABLE "core"."master_data"
ALTER COLUMN "lock_status" DROP DEFAULT, ALTER COLUMN "lock_status" DROP NOT NULL;

@IgnoreErrors
ALTER TABLE "core"."master_data" ADD COLUMN "distance" int4;

-- ----------------------------
-- Fix structure for im_messages
-- ----------------------------

ALTER TABLE "core"."im_messages" ALTER COLUMN "text" TYPE text;
