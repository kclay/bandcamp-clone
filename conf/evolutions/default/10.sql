# --- !Ups
ALTER TABLE `tracks`
  ADD COLUMN `fileName` VARCHAR(45) NULL AFTER `duration`;
