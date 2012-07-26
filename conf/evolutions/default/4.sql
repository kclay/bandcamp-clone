# --- !Ups
ALTER TABLE `tracks`
  ADD COLUMN `slug` VARCHAR(50) NULL AFTER `active`,
  DROP INDEX `idx37070679`,
  ADD  UNIQUE INDEX `idx_artist_slug` (`artist_id`, `slug`);
