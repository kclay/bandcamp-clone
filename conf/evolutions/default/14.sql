# --- !Ups
ALTER TABLE `tracks`
  ADD COLUMN `artURL` VARCHAR(255) NULL AFTER `fileName`;
