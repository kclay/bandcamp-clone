# --- !Ups
CREATE TABLE `transactions`(
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `item_id` INT(11) NOT NULL,
  `amount` DOUBLE NOT NULL,
  `kind` ENUM('album','track') NOT NULL,
  `paypal_token` VARCHAR(25) NOT NULL,
  `status` ENUM('pending','callback','error','void','completed','checkout'),
  `correlation_id` VARCHAR(20),
  `transaction_id` VARCHAR(20),
  `payer_id` VARCHAR(50),
  `ack` VARCHAR(45),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_token` (`paypal_token`)
);
