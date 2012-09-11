# --- !Ups
ALTER TABLE `bulabowl`.`artists`
  ADD COLUMN `genre_id` BIGINT(11) NULL AFTER `permission`,
  ADD  INDEX `idx_genre` (`genre_id`);
