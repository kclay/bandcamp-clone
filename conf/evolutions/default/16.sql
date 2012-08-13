# --- !Ups
ALTER TABLE `tracks`
  ADD COLUMN `single` BOOLEAN DEFAULT 0 NULL AFTER `artURL`,
  ADD  INDEX `idx_single` (`single`);
