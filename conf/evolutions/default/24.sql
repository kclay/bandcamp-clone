# --- !Ups
CREATE TABLE `track_tags`(
  `track_id` INT(11) NOT NULL,
  `tag_id` INT(11) NOT NULL,
  PRIMARY KEY (`track_id`, `tag_id`)
);
CREATE TABLE `album_tags`(
  `album_id` INT(11) NOT NULL,
  `tag_id` INT(11) NOT NULL,
  PRIMARY KEY (`album_id`, `tag_id`)
);

