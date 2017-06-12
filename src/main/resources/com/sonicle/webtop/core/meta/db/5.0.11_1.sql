@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Fix values
-- ----------------------------

UPDATE "core"."activities"
SET "revision_status" = 'M'
WHERE "revision_status" IS NULL;

UPDATE "core"."causals"
SET "revision_status" = 'M'
WHERE "revision_status" IS NULL;

-- ----------------------------
-- Update default
-- ----------------------------

ALTER TABLE "core"."activities"
ALTER COLUMN "activity_id" SET DEFAULT nextval('"core".seq_activities'::regclass),
ALTER COLUMN "revision_status" SET NOT NULL,
ALTER COLUMN "read_only" SET DEFAULT false;

ALTER TABLE "core"."causals"
ALTER COLUMN "causal_id" SET DEFAULT nextval('"core".seq_causals'::regclass),
ALTER COLUMN "revision_status" SET NOT NULL,
ALTER COLUMN "read_only" SET DEFAULT false;

ALTER TABLE "core"."messages_queue"
ALTER COLUMN "queue_id" SET DEFAULT nextval('"core".seq_messages_queue'::regclass);

ALTER TABLE "core"."snoozed_reminders"
ALTER COLUMN "snoozed_reminder_id" SET DEFAULT nextval('"core".seq_snoozed_reminders'::regclass);

ALTER TABLE "core"."syslog"
ALTER COLUMN "syslog_id" SET DEFAULT nextval('"core".seq_syslog'::regclass);

-- ----------------------------
-- Support new MasterData table
-- ----------------------------

ALTER TABLE "core"."causals"
ALTER COLUMN "customer_id" TYPE varchar(36);
ALTER TABLE "core"."causals" RENAME "customer_id" TO "master_data_id";

-- ----------------------------
-- Table structure for master_data
-- ----------------------------
DROP TABLE IF EXISTS "core"."master_data";
CREATE TABLE "core"."master_data" (
"domain_id" varchar(20) NOT NULL,
"master_data_id" varchar(36) NOT NULL,
"parent_master_data_id" varchar(36),
"external_id" varchar(36),
"type" varchar(3) NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int4 DEFAULT 0 NOT NULL,
"lock_status" varchar(1) DEFAULT 'N'::character varying NOT NULL,
"description" varchar(50),
"address" varchar(100),
"city" varchar(50),
"postal_code" varchar(20),
"state" varchar(30),
"country" varchar(50),
"telephone" varchar(50),
"fax" varchar(50),
"mobile" varchar(50),
"email" varchar(320),
"notes" varchar(2000)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Indexes structure for table master_data
-- ----------------------------
CREATE INDEX "master_data_ak1" ON "core"."master_data" USING btree ("domain_id", "parent_master_data_id", "type", "revision_status", "description");
CREATE INDEX "master_data_ak2" ON "core"."master_data" USING btree ("domain_id", "type", "revision_status", "description");
CREATE INDEX "master_data_ak3" ON "core"."master_data" USING btree ("external_id", "domain_id");

-- ----------------------------
-- Primary Key structure for table master_data
-- ----------------------------
ALTER TABLE "core"."master_data" ADD PRIMARY KEY ("domain_id", "master_data_id");
