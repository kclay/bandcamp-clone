# --- !Ups
ALTER TABLE `sales`
  ADD COLUMN `id` BIGINT(11) NOT NULL AUTO_INCREMENT FIRST,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`id`),
  ADD  INDEX `idx_metric` (`artist_id`, `item_id`);
