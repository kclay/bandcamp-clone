# --- !Ups
DROP TABLE IF EXISTS `queue`;
CREATE TABLE `queue`(
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `file` VARCHAR(45),
  `status` ENUM('new','processing','error','completed'),
  `started` INT(11),
  `ended` INT(11),
  KEY `idx_status` (`status`),
  PRIMARY KEY (`id`)
);
