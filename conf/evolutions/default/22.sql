# --- !Ups
ALTER TABLE `sales`
  ADD COLUMN `item_type` ENUM('album','track') NULL AFTER `item_id`,
  DROP INDEX `idx_metric`,
  ADD  INDEX `idx_metric` (`artist_id`, `item_id`, `item_type`);
