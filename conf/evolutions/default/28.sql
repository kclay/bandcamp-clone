# --- !Ups
DROP TABLE `genre`;
CREATE TABLE `genre` (
  `genre_name` varchar(128) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tag` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_genre` (`tag`)
) ENGINE=InnoDB;

insert into `genre` (`genre_name`, `id`, `tag`) values('Acoustic','1','acoustic');
insert into `genre` (`genre_name`, `id`, `tag`) values('Alternative','2','alternative');
insert into `genre` (`genre_name`, `id`, `tag`) values('Ambient','3','ambient');
insert into `genre` (`genre_name`, `id`, `tag`) values('Blues','4','blues');
insert into `genre` (`genre_name`, `id`, `tag`) values('Classical','5','classical');
insert into `genre` (`genre_name`, `id`, `tag`) values('Comedy','6','comedy');
insert into `genre` (`genre_name`, `id`, `tag`) values('Country','7','country');
insert into `genre` (`genre_name`, `id`, `tag`) values('Unknown','8','unknown');
insert into `genre` (`genre_name`, `id`, `tag`) values('Devotional','9','devotional');
insert into `genre` (`genre_name`, `id`, `tag`) values('Electronic','10','electronic');
insert into `genre` (`genre_name`, `id`, `tag`) values('Experimental','11','experimental');
insert into `genre` (`genre_name`, `id`, `tag`) values('Folk','12','folk');
insert into `genre` (`genre_name`, `id`, `tag`) values('Funk','13','funk');
insert into `genre` (`genre_name`, `id`, `tag`) values('Hip Hop/Rap','14','hip-hop');
insert into `genre` (`genre_name`, `id`, `tag`) values('Jazz','15','jazz');
insert into `genre` (`genre_name`, `id`, `tag`) values('Kids','16','kids');
insert into `genre` (`genre_name`, `id`, `tag`) values('Latin','17','latin');
insert into `genre` (`genre_name`, `id`, `tag`) values('Metal','18','metal');
insert into `genre` (`genre_name`, `id`, `tag`) values('Pop','19','pop');
insert into `genre` (`genre_name`, `id`, `tag`) values('Punk','20','punk');
insert into `genre` (`genre_name`, `id`, `tag`) values('R&B/Soul','21','r-b');
insert into `genre` (`genre_name`, `id`, `tag`) values('Reggae','22','reggae');
insert into `genre` (`genre_name`, `id`, `tag`) values('Rock','23','rock');
insert into `genre` (`genre_name`, `id`, `tag`) values('Soundtrack','24','soundtrack');
insert into `genre` (`genre_name`, `id`, `tag`) values('Spoken Word','25','spoken-word');

