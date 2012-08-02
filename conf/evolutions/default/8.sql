# --- !Ups
ALTER TABLE `queue`
  ADD COLUMN `duration` INT(11) NULL AFTER `ended`;
