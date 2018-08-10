@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Fix file types
-- ----------------------------
DELETE FROM "core"."file_types" WHERE ("extension"='doc') AND ("type"='document') AND ("subtype"='word');
DELETE FROM "core"."file_types" WHERE ("extension"='docx') AND ("type"='document') AND ("subtype"='word');
DELETE FROM "core"."file_types" WHERE ("extension"='xls') AND ("type"='document') AND ("subtype"='excel');
DELETE FROM "core"."file_types" WHERE ("extension"='xlsx') AND ("type"='document') AND ("subtype"='excel');
DELETE FROM "core"."file_types" WHERE ("extension"='xml') AND ("type"='text') AND ("subtype"='');
DELETE FROM "core"."file_types" WHERE ("extension"='rtf') AND ("type"='text') AND ("subtype"='');
DELETE FROM "core"."file_types" WHERE ("extension"='waw') AND ("type"='audio') AND ("subtype"='');

INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('wav', 'audio', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('m4a', 'audio', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('flac', 'audio', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('amr', 'audio', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('ical', 'calendar', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('ifb', 'calendar', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('icalendar', 'calendar', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('vcf', 'card', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('vcard', 'card', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('epub', 'ebook', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('rtf', 'document', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('xps', 'document', '');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('doc', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('docm', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('docx', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('dot', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('dotm', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('dotx', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('fodt', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('odt', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('ott', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('djvu', 'document', 'text');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('csv', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('fods', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('ods', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('ots', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('xls', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('xlsm', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('xlsx', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('xlt', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('xltm', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('xltx', 'document', 'spreadsheet');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('fodp', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('odp', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('otp', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('pot', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('potm', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('potx', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('pps', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('ppsm', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('ppsx', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('ppt', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('pptm', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('pptx', 'document', 'presentation');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('mht', 'text', 'html');
INSERT INTO "core"."file_types" ("extension", "type", "subtype") VALUES ('xml', 'text', 'xml');
