# --- !Ups
ALTER TABLE `tracks`
  CHANGE `license` `license` VARCHAR(128) DEFAULT 'all-rights' NULL;
