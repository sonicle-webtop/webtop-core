@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Clean lost autosave data preserving for a while
-- ----------------------------
UPDATE "core"."autosave" SET "domain_id" = substr('!' || "domain_id", 1, 20);
