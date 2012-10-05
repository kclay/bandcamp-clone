# --- !Ups
CREATE
/*[ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = { user | CURRENT_USER }]
    [SQL SECURITY { DEFINER | INVOKER }]*/
VIEW `bulabowl`.`tracks_with_tags` AS
(SELECT
  tracks.*,
  GROUP_CONCAT(tags.`tag_name`) AS tags
FROM
  tracks
  LEFT JOIN track_tags
    ON track_tags.track_id = tracks.id
  LEFT JOIN tags
    ON tags.id = track_tags.`tag_id`
GROUP BY tracks.`id`) ;

