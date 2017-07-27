@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Sequence structure for seq_im_chats
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_im_chats";
CREATE SEQUENCE "core"."seq_im_chats";

-- ----------------------------
-- Sequence structure for seq_im_messages
-- ----------------------------
DROP SEQUENCE IF EXISTS "core"."seq_im_messages";
CREATE SEQUENCE "core"."seq_im_messages";

-- ----------------------------
-- Table structure for im_chats
-- ----------------------------
DROP TABLE IF EXISTS "core"."im_chats";
CREATE TABLE "core"."im_chats" (
"id" int4 DEFAULT nextval('"core".seq_im_chats'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"chat_jid" varchar(255) NOT NULL,
"owner_jid" varchar(255) NOT NULL,
"name" varchar(255) NOT NULL,
"is_group_chat" bool NOT NULL,
"last_seen_activity" timestamptz(6),
"with_jid" varchar(255)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for im_messages
-- ----------------------------
DROP TABLE IF EXISTS "core"."im_messages";
CREATE TABLE "core"."im_messages" (
"id" int4 DEFAULT nextval('"core".seq_im_messages'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"chat_jid" varchar(255) NOT NULL,
"sender_jid" varchar(255) NOT NULL,
"sender_resource" varchar(255),
"date" date,
"timestamp" timestamptz(6) NOT NULL,
"action" varchar(10) NOT NULL,
"text" varchar(255),
"message_uid" varchar(255) NOT NULL,
"stanza_id" varchar(255)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Indexes structure for table im_chats
-- ----------------------------
CREATE INDEX "im_chats_ak1" ON "core"."im_chats" USING btree ("domain_id", "user_id", "revision_status");
CREATE INDEX "im_chats_ak2" ON "core"."im_chats" USING btree ("domain_id", "user_id", "chat_jid");

-- ----------------------------
-- Primary Key structure for table im_chats
-- ----------------------------
ALTER TABLE "core"."im_chats" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table im_messages
-- ----------------------------
CREATE INDEX "im_history_ak1" ON "core"."im_messages" USING btree ("domain_id", "user_id", "chat_jid");

-- ----------------------------
-- Primary Key structure for table im_messages
-- ----------------------------
ALTER TABLE "core"."im_messages" ADD PRIMARY KEY ("id");
