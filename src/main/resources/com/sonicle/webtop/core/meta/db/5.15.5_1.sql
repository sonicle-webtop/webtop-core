@DataSource[default@com.sonicle.webtop.core]

ALTER TABLE config.pecbridge_fetchers ADD enabled bool NOT NULL DEFAULT true;
ALTER TABLE config.pecbridge_relays ADD enabled bool NOT NULL DEFAULT true;
