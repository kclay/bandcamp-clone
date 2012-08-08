# --- !Ups
CREATE TABLE `password_resets`(
  `id` BIGINT(20) NOT NULL,
  `token` VARCHAR(40),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_reset_token` (`token`)
);
