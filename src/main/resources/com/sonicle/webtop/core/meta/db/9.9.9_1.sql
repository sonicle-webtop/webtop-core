@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Add new fields to datasources table
-- ----------------------------
ALTER TABLE "core"."data_sources" ADD COLUMN "friendly_id" varchar(255);
CREATE UNIQUE INDEX "data_sources_ak2" ON "core"."data_sources" ("domain_id", "friendly_id");
