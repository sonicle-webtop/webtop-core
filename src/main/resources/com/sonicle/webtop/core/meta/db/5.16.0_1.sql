@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Re-enable groups pkey
-- ----------------------------
ALTER TABLE "core"."users" DROP CONSTRAINT "users_pkey";

-- ----------------------------
-- Sanitize group names replacing spaces with '-' and translating accented chars to un-accented version
-- In other languages accented letters needs to be fixed manually in database!
-- ----------------------------
UPDATE "core"."users"
SET "user_id" = translate(trim(regexp_replace("user_id", '\s+', '-', 'g')), 'àèéìòù', 'aeeiou')
WHERE "type" = 'G';

-- ----------------------------
-- Rename duplicated group names by adding a numbered suffix (done across all domains)
-- ----------------------------
CREATE SEQUENCE "core"."seq_groupnamesuffix";

WITH dupl AS (
SELECT t1."domain_id", t1."user_id"
FROM "core"."users" AS t1
WHERE t1."type" = 'G'
GROUP BY t1."domain_id", t1."user_id"
HAVING COUNT(*) > 1
)
UPDATE "core"."users" AS t2
SET "user_id" = t2."user_id" || '-' || nextval('core.seq_groupnamesuffix')
FROM dupl
WHERE (dupl."domain_id" = t2."domain_id") AND (dupl."user_id" = t2."user_id");

DROP SEQUENCE IF EXISTS "core"."seq_groupnamesuffix";

-- ----------------------------
-- Re-enable groups pkey and add unique index on domain/user_id (users/groups with same name are not allowed anymore)
-- ----------------------------
ALTER TABLE "core"."users" ADD PRIMARY KEY ("domain_id", "user_id", "type");
@IgnoreErrors
CREATE UNIQUE INDEX "users_ak2" ON "core"."users" USING btree ("domain_id", "user_id");

-- ----------------------------
-- Sanitize role names replacing spaces with '-'
-- ----------------------------
UPDATE "core"."roles"
SET "name" = translate(trim(regexp_replace("name", '\s+', '-', 'g')), 'àèéìòù', 'aeeiou');

-- ----------------------------
-- Rename duplicated role names by adding a numbered suffix (done across all domains)
-- ----------------------------
CREATE SEQUENCE "core"."seq_rolenamesuffix";

WITH dupl AS (
SELECT t1."domain_id", t1."name"
FROM "core"."roles" AS t1
GROUP BY t1."domain_id", t1."name"
HAVING COUNT(*) > 1
)
UPDATE "core"."roles" AS t2
SET "name" = t2."name" || '-' || nextval('core.seq_rolenamesuffix')
FROM dupl
WHERE (dupl."domain_id" = t2."domain_id") AND (dupl."name" = t2."name");

DROP SEQUENCE IF EXISTS "core"."seq_rolenamesuffix";

-- ----------------------------
-- Make ak1 index unique on domain_id/name
-- ----------------------------
DROP INDEX "core"."roles_ak1";
CREATE UNIQUE INDEX "roles_ak1" ON "core"."roles" ("domain_id", "name");
