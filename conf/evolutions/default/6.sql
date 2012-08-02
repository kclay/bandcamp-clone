# --- !Ups
ALTER TABLE `queue`
  CHANGE `status` `status` ENUM('new','processing','error','completed','error_encode_preview','error_encode_full') CHARSET latin1 COLLATE latin1_swedish_ci NULL;
