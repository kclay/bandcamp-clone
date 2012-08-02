# --- !Ups
ALTER TABLE `tracks`
  CHANGE `duration` `duration` INT(11) DEFAULT 0 NOT NULL;
