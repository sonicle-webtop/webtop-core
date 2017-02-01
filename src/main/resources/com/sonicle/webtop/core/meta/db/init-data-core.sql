
-- ----------------------------
-- Records of activities
-- ----------------------------
INSERT INTO "core"."activities" VALUES ('1', '*', '*', NULL, 'Permesso', 'f', NULL);
INSERT INTO "core"."activities" VALUES ('2', '*', '*', NULL, 'Ferie', 'f', NULL);
INSERT INTO "core"."activities" VALUES ('3', '*', '*', NULL, 'Malattia', 'f', NULL);

-- ----------------------------
-- Records of file_types
-- ----------------------------
INSERT INTO "core"."file_types" VALUES ('7z', 'archive', '');
INSERT INTO "core"."file_types" VALUES ('aiff', 'audio', '');
INSERT INTO "core"."file_types" VALUES ('asf', 'video', '');
INSERT INTO "core"."file_types" VALUES ('au', 'audio', '');
INSERT INTO "core"."file_types" VALUES ('avi', 'video', '');
INSERT INTO "core"."file_types" VALUES ('bin', 'file', '');
INSERT INTO "core"."file_types" VALUES ('bmp', 'image', '');
INSERT INTO "core"."file_types" VALUES ('cab', 'archive', '');
INSERT INTO "core"."file_types" VALUES ('css', 'text', '');
INSERT INTO "core"."file_types" VALUES ('csv', 'text', '');
INSERT INTO "core"."file_types" VALUES ('divx', 'video', '');
INSERT INTO "core"."file_types" VALUES ('doc', 'document', 'word');
INSERT INTO "core"."file_types" VALUES ('docx', 'document', 'word');
INSERT INTO "core"."file_types" VALUES ('eml', 'message', '');
INSERT INTO "core"."file_types" VALUES ('exe', 'application', '');
INSERT INTO "core"."file_types" VALUES ('gif', 'image', '');
INSERT INTO "core"."file_types" VALUES ('gz', 'archive', '');
INSERT INTO "core"."file_types" VALUES ('htm', 'text', 'html');
INSERT INTO "core"."file_types" VALUES ('html', 'text', 'html');
INSERT INTO "core"."file_types" VALUES ('ics', 'calendar', '');
INSERT INTO "core"."file_types" VALUES ('ical', 'calendar', '');
INSERT INTO "core"."file_types" VALUES ('icalendar', 'calendar', '');
INSERT INTO "core"."file_types" VALUES ('jpeg', 'image', '');
INSERT INTO "core"."file_types" VALUES ('jpg', 'image', '');
INSERT INTO "core"."file_types" VALUES ('js', 'script', '');
INSERT INTO "core"."file_types" VALUES ('mkv', 'video', '');
INSERT INTO "core"."file_types" VALUES ('mov', 'video', '');
INSERT INTO "core"."file_types" VALUES ('mp3', 'audio', '');
INSERT INTO "core"."file_types" VALUES ('mp4', 'video', '');
INSERT INTO "core"."file_types" VALUES ('mpeg', 'video', '');
INSERT INTO "core"."file_types" VALUES ('ogg', 'audio', '');
INSERT INTO "core"."file_types" VALUES ('pdf', 'document', 'pdf');
INSERT INTO "core"."file_types" VALUES ('png', 'image', '');
INSERT INTO "core"."file_types" VALUES ('py', 'script', '');
INSERT INTO "core"."file_types" VALUES ('rar', 'archive', '');
INSERT INTO "core"."file_types" VALUES ('rtf', 'text', '');
INSERT INTO "core"."file_types" VALUES ('svf', 'image', '');
INSERT INTO "core"."file_types" VALUES ('tar', 'archive', '');
INSERT INTO "core"."file_types" VALUES ('tiff', 'image', '');
INSERT INTO "core"."file_types" VALUES ('txt', 'text', '');
INSERT INTO "core"."file_types" VALUES ('waw', 'audio', '');
INSERT INTO "core"."file_types" VALUES ('wma', 'audio', '');
INSERT INTO "core"."file_types" VALUES ('wmv', 'video', '');
INSERT INTO "core"."file_types" VALUES ('xls', 'document', 'excel');
INSERT INTO "core"."file_types" VALUES ('xlsx', 'document', 'excel');
INSERT INTO "core"."file_types" VALUES ('xml', 'text', '');
INSERT INTO "core"."file_types" VALUES ('z', 'archive', '');
INSERT INTO "core"."file_types" VALUES ('zip', 'archive', '');

-- ----------------------------
-- Records of local_vault
-- ----------------------------
INSERT INTO "core"."local_vault" VALUES ('*', 'admin', 'PLAIN', '1234');

-- ----------------------------
-- Records of media_types
-- ----------------------------
INSERT INTO "core"."media_types" VALUES ('eml', 'message/rfc822');

-- ----------------------------
-- Records of media_types
-- ----------------------------
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'devices.sync.shell.uri', 'sh://localhost');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'dropbox.appkey', '5jzeukj99hzcawj');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'dropbox.appsecret', 'aud3y9v7mu4l4go');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'googledrive.clientid', '542484747748-iorv72fth410dbr9p185emqjs8ejmekl.apps.googleusercontent.com');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'googledrive.clientsecret', 'mAOLSBaSNZ8Z87eLBwYgDIpG');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'otp.enabled', 'true');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'php.path', '/sonicle/bin/');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'smtp.host', 'localhost');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'smtp.port', '25');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'syslog.enabled', 'false');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'zpush.path', '/sonicle/z-push/');
INSERT INTO "core"."settings" VALUES ('com.sonicle.webtop.core', 'home.path', '/sonicle/sonicle/webtop5/');

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO "core"."users" VALUES ('*', 'admin', 'U', 't', '991f72dc-2b96-4340-b88f-53506b160519', 'Administrator', 'K2U4CN57AEQ73QB5');

-- ----------------------------
-- Records of users_info
-- ----------------------------
INSERT INTO "core"."users_info" VALUES ('*', 'admin', NULL, NULL, 'System', 'Admin', NULL, NULL, 'admin@locahlost', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
