# --- !Ups
ALTER TABLE `transactions`
  ADD COLUMN `sig` VARCHAR(45) NULL AFTER `transaction_id`,
  ADD  UNIQUE INDEX `idx_sig` (`sig`);
