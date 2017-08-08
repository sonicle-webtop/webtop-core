@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Indexes structure for table im_messages
-- ----------------------------
DROP INDEX "core"."im_history_ak1";
CREATE INDEX "im_messages_ak1" ON "core"."im_messages" USING btree ("domain_id", "user_id", "chat_jid", "date");
CREATE INDEX "im_messages_ak2" ON "core"."im_messages" USING btree ("domain_id", "user_id", "chat_jid", "text");

-- ----------------------------
-- Support new file action
-- ----------------------------
ALTER TABLE "core"."im_messages" ADD COLUMN "data" text;
