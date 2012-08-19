# --- !Ups
ALTER TABLE `sales`
  DROP COLUMN `num_of_downloads`,
  ADD COLUMN `artist_id` BIGINT(11) NOT NULL AFTER `percentage`,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`transaction_id`, `artist_id`);
