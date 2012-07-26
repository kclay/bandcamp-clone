# --- !Ups
ALTER TABLE `tracks`
  ADD COLUMN `file` VARCHAR(45) NULL AFTER `active`,
  ADD COLUMN `session` VARCHAR(45) NULL AFTER `file`;

