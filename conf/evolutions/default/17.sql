# --- !Ups
ALTER TABLE `sales`
  CHANGE `direct_payment` `percentage` DOUBLE NULL;
