@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Add primary keys defaults
-- ----------------------------

ALTER TABLE "core"."activities"
ALTER COLUMN "activity_id" SET DEFAULT nextval('"core".seq_activities'::regclass);

ALTER TABLE "core"."causals"
ALTER COLUMN "causal_id" SET DEFAULT nextval('"core".seq_causals'::regclass);

ALTER TABLE "core"."messages_queue"
ALTER COLUMN "queue_id" SET DEFAULT nextval('"core".seq_messages_queue'::regclass);

ALTER TABLE "core"."snoozed_reminders"
ALTER COLUMN "snoozed_reminder_id" SET DEFAULT nextval('"core".seq_snoozed_reminders'::regclass);

ALTER TABLE "core"."syslog"
ALTER COLUMN "syslog_id" SET DEFAULT nextval('"core".seq_syslog'::regclass);
