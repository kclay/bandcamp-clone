# --- !Ups
/*
SQLyog Ultimate v9.62 
MySQL - 5.5.20-log : Database - bulabowl
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`bulabowl` /*!40100 DEFAULT CHARACTER SET latin1 */;

/*Table structure for table `album_tracks` */

DROP TABLE IF EXISTS `album_tracks`;

CREATE TABLE `album_tracks` (
  `track_id` bigint(20) NOT NULL,
  `track_order` int(11) NOT NULL DEFAULT '0',
  `album_id` bigint(20) NOT NULL,
  PRIMARY KEY (`album_id`,`track_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `album_tracks` */

/*Table structure for table `albums` */

DROP TABLE IF EXISTS `albums`;

CREATE TABLE `albums` (
  `art` varchar(45) DEFAULT NULL,
  `releaseDate` date DEFAULT NULL,
  `album_name` varchar(128) NOT NULL,
  `artURL` varchar(128) NOT NULL,
  `donateMore` tinyint(1) NOT NULL,
  `price` double NOT NULL,
  `credits` text,
  `slug` varchar(128) NOT NULL,
  `artistName` varchar(45) DEFAULT NULL,
  `upc` varchar(20) DEFAULT NULL,
  `artist_id` bigint(20) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `about` text,
  `download` tinyint(1) NOT NULL,
  `active` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx368d0675` (`artist_id`),
  KEY `idx240b052e` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `albums` */

/*Table structure for table `artists` */

DROP TABLE IF EXISTS `artists`;

CREATE TABLE `artists` (
  `artist_name` varchar(128) NOT NULL,
  `activated` tinyint(1) NOT NULL,
  `email` varchar(128) NOT NULL,
  `username` varchar(128) NOT NULL,
  `domain` varchar(128) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pass` varchar(128) NOT NULL,
  `permission` varchar(10) DEFAULT 'normal',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `artists` */

/*Table structure for table `genre` */

DROP TABLE IF EXISTS `genre`;

CREATE TABLE `genre` (
  `genre_name` varchar(128) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1;

/*Data for the table `genre` */

insert  into `genre`(`genre_name`,`id`) values ('Acoustic',1),('Alternative',2),('Ambient',3),('Blues',4),('Classical',5),('Comedy',6),('Country',7),('Unknown',8),('Devotional',9),('Electronic',10),('Experimental',11),('Folk',12),('Funk',13),('Hip Hop/Rap',14),('Jazz',15),('Kids',16),('Latin',17),('Metal',18),('Pop',19),('Punk',20),('R&B/Soul',21),('Reggae',22),('Rock',23),('Soundtrack',24),('Spoken Word',25),('World',26);

/*Table structure for table `tags` */

DROP TABLE IF EXISTS `tags`;

CREATE TABLE `tags` (
  `tag_name` varchar(128) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `idx23a30519` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `tags` */

/*Table structure for table `tracks` */

DROP TABLE IF EXISTS `tracks`;

CREATE TABLE `tracks` (
  `art` varchar(45) DEFAULT NULL,
  `releaseDate` date DEFAULT NULL,
  `track_name` varchar(128) NOT NULL,
  `donateMore` tinyint(1) NOT NULL,
  `price` double NOT NULL,
  `lyrics` text,
  `credits` text,
  `artistName` varchar(45) DEFAULT NULL,
  `license` varchar(128) NOT NULL,
  `artist_id` bigint(20) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `about` text,
  `download` tinyint(1) NOT NULL,
  `active` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx37070679` (`artist_id`),
  KEY `idx24790532` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `tracks` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
