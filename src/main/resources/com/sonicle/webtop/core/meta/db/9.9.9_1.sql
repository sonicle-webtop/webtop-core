@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Table structure for tags
-- ----------------------------
CREATE TABLE "core"."tags" (
"tag_id" varchar(22) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"built_in" bool NOT NULL,
"name" varchar(50) NOT NULL,
"color" varchar(20) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Indexes structure for table tags
-- ----------------------------
CREATE INDEX "tags_ak1" ON "core"."tags" USING btree ("domain_id");
CREATE INDEX "tags_ak2" ON "core"."tags" USING btree ("built_in", "name");
CREATE UNIQUE INDEX "tags_ak3" ON "core"."tags" USING btree ("domain_id", "name");

-- ----------------------------
-- Primary Key structure for table tags
-- ----------------------------
ALTER TABLE "core"."tags" ADD PRIMARY KEY ("tag_id");

-- ----------------------------
-- Table structure for licenses
-- ----------------------------
CREATE TABLE "core"."licenses" (
"internet_domain" varchar(255) NOT NULL,
"product_id" varchar(255) NOT NULL,
"license" text,
PRIMARY KEY ("internet_domain", "product_id")
);
