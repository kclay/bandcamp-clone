# --- !Ups
ALTER TABLE `tracks`
  CHANGE `donateMore` `donateMore` TINYINT(1) DEFAULT 1 NOT NULL,
  ADD COLUMN `genre_id` BIGINT DEFAULT 0 NULL AFTER `single`;
