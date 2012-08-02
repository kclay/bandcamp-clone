# --- !Ups
ALTER TABLE `tracks`
  ADD COLUMN `duration` INT(11) NULL AFTER `slug`;
