CREATE SCHEMA "config";

-- ----------------------------
-- Sequence structure for seq_pecbridge_fetchers
-- ----------------------------
DROP SEQUENCE IF EXISTS "config"."seq_pecbridge_fetchers";
CREATE SEQUENCE "config"."seq_pecbridge_fetchers";

-- ----------------------------
-- Sequence structure for seq_pecbridge_relays
-- ----------------------------
DROP SEQUENCE IF EXISTS "config"."seq_pecbridge_relays";
CREATE SEQUENCE "config"."seq_pecbridge_relays";


-- ----------------------------
-- Table structure for pecbridge_fetchers
-- ----------------------------
DROP TABLE IF EXISTS "config"."pecbridge_fetchers";
CREATE TABLE "config"."pecbridge_fetchers" (
"fetcher_id" int4 DEFAULT nextval('"config".seq_pecbridge_fetchers'::regclass) NOT NULL,
"context" varchar(100) NOT NULL,
"forward_address" varchar(320),
"delete_on_forward" bool,
"host" varchar(255),
"port" int2,
"protocol" varchar(10),
"username" varchar(255),
"password" varchar(255),
"webtop_profile_id" varchar(150)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for pecbridge_relays
-- ----------------------------
DROP TABLE IF EXISTS "config"."pecbridge_relays";
CREATE TABLE "config"."pecbridge_relays" (
"relay_id" int4 DEFAULT nextval('"config".seq_pecbridge_relays'::regclass) NOT NULL,
"context" varchar(100) NOT NULL,
"matcher" varchar(320),
"host" varchar(255),
"port" int2,
"protocol" varchar(10),
"username" varchar(255),
"password" varchar(255),
"debug" bool,
"webtop_profile_id" varchar(150)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Alter Sequences Owned By 
-- ----------------------------

-- ----------------------------
-- Indexes structure for table pecbridge_fetchers
-- ----------------------------
CREATE INDEX "pecbridge_fetchers_ak1" ON "config"."pecbridge_fetchers" USING btree ("context");
CREATE UNIQUE INDEX "pecbridge_fetchers_ak2" ON "config"."pecbridge_fetchers" USING btree ("context", "forward_address");

-- ----------------------------
-- Primary Key structure for table pecbridge_fetchers
-- ----------------------------
ALTER TABLE "config"."pecbridge_fetchers" ADD PRIMARY KEY ("fetcher_id");

-- ----------------------------
-- Indexes structure for table pecbridge_relays
-- ----------------------------
CREATE INDEX "pecbridge_relays_ak1" ON "config"."pecbridge_relays" USING btree ("context");
CREATE UNIQUE INDEX "pecbridge_relays_ak2" ON "config"."pecbridge_relays" USING btree ("context", "matcher");

-- ----------------------------
-- Primary Key structure for table pecbridge_relays
-- ----------------------------
ALTER TABLE "config"."pecbridge_relays" ADD PRIMARY KEY ("relay_id");
