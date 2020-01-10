@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Table structure for languages
-- ----------------------------
DROP TABLE IF EXISTS "core"."languages";
CREATE TABLE "core"."languages" (
"language_tag" varchar(5) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Primary Key structure for table languages
-- ----------------------------
ALTER TABLE "core"."languages" ADD PRIMARY KEY ("language_tag");

-- ----------------------------
-- Records of languages
-- ----------------------------
INSERT INTO "core"."languages" ("language_tag") VALUES ('en_EN');
INSERT INTO "core"."languages" ("language_tag") VALUES ('it_IT');
INSERT INTO "core"."languages" ("language_tag") VALUES ('es_ES');
INSERT INTO "core"."languages" ("language_tag") VALUES ('de_DE');
INSERT INTO "core"."languages" ("language_tag") VALUES ('hr_HR');
