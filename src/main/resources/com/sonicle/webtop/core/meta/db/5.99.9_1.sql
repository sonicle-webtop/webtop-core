@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Add auth state fields:
-- - 'UN' = Unknown
-- - 'OK' = Auth OK
-- - 'ER' = Auth Error
-- ----------------------------
ALTER TABLE config.pecbridge_fetchers ADD auth_state varchar(2) NOT NULL DEFAULT 'UN';
ALTER TABLE config.pecbridge_relays ADD auth_state varchar(2) NOT NULL DEFAULT 'UN';
