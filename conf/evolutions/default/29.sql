# --- !Ups
CREATE TABLE `track_ratings`(
  `track_id` BIGINT NOT NULL,
  `votes` BIGINT,
  `points` DECIMAL(11,2),
  PRIMARY KEY (`track_id`)
);
ALTER TABLE `artists`
  ADD COLUMN `bio` TEXT NULL AFTER `genre_id`;

