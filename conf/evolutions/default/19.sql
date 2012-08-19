# --- !Ups
ALTER TABLE `sales`
  ADD COLUMN `item_id` BIGINT(11) NOT NULL AFTER `artist_id`,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`transaction_id`, `artist_id`, `item_id`);
