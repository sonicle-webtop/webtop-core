
-- ----------------------------
-- Table structure for customers
-- ----------------------------
DROP TABLE IF EXISTS "public"."customers";
CREATE TABLE "public"."customers" (
"customer_id" varchar(15),
"description" varchar(50),
"type" varchar(1),
"address" varchar(100),
"city" varchar(30),
"state" varchar(30),
"postalcode" varchar(20),
"country" varchar(30),
"telephone" varchar(50),
"email" varchar(80),
"from_drm" varchar(5),
"parent_id" varchar(15),
"external_id" varchar(100),
"status" varchar(1),
"domain_id" varchar(15),
"km" varchar(15),
"lock" varchar(20),
"note" varchar(2000)
)
WITH (OIDS=FALSE)

;
