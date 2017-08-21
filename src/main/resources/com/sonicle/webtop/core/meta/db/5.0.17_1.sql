@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Support new deliveryTimestamp
-- ----------------------------
ALTER TABLE "core"."im_messages" RENAME "timestamp" TO "delivery_timestamp";
ALTER TABLE "core"."im_messages" ALTER COLUMN "date" TYPE timestamptz(6);
ALTER TABLE "core"."im_messages" RENAME "date" TO "timestamp";

-- ----------------------------
-- Data move: customers -> master_data
-- ----------------------------
UPDATE "core"."im_messages" SET "timestamp" = "delivery_timestamp";

-- ----------------------------
-- Support new deliveryTimestamp
-- ----------------------------
ALTER TABLE "core"."im_messages"
ALTER COLUMN "timestamp" SET NOT NULL,
ALTER COLUMN "delivery_timestamp" DROP NOT NULL;

-- ----------------------------
-- Indexes structure for table im_messages
-- ----------------------------
ALTER INDEX "core"."im_messages_ak2" RENAME TO "im_messages_ak3";
CREATE INDEX "im_messages_ak2" ON "core"."im_messages" ("domain_id", "user_id", "chat_jid", "delivery_timestamp");
