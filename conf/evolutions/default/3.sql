# --- !Ups
ALTER TABLE `queue`
  ADD COLUMN `session` VARCHAR(45) NOT NULL AFTER `file`;
ALTER TABLE `albums`
  ADD COLUMN `session` VARCHAR(45) NOT NULL;

