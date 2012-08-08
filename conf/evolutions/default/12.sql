# --- !Ups
CREATE TABLE `sales`(
  `transaction_id` bigint(20) NOT NULL,
  `num_of_downloads` INT(11),
  `amount` DOUBLE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `direct_payment` BOOL DEFAULT 1,
  PRIMARY KEY (`transaction_id`)
);

CREATE TABLE `stats`(
  `artist_id` bigint(11) NOT NULL,
  `metric` VARCHAR(20) NOT NULL,
  `tracked_at` DATE NOT NULL,
  `object_id` bigint(11) NOT NULL,
  `total` INT(11),
  PRIMARY KEY (`artist_id`, `metric`, `tracked_at`, `object_id`)
);

