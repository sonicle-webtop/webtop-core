@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Data move: customers -> master_data
-- ----------------------------

INSERT INTO "core"."master_data"
("domain_id", "master_data_id", "parent_master_data_id", "external_id", "type", "revision_status", "revision_timestamp", "revision_sequence", "lock_status", "description", "address", "city", "postal_code", "state", "country", "telephone", "fax", "mobile", "email", "notes", "distance")
SELECT "domain_id", "customer_id", "parent_id", "external_id", "type",
'M' AS "revision_status",
NOW() AS "revision_timestamp",
0 AS "revision_sequence",
CASE WHEN ("lock" IS NULL) THEN 'N' ELSE 'L' END AS "lock_status",
"description", "address", "city", "postalcode", "state", "country", "telephone",
NULL AS "fax",
NULL AS "mobile",
"email", "note", "km"
FROM "core"."customers";
