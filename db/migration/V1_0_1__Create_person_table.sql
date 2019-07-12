CREATE TABLE `test`.`people` (
  `id_people` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(60) NOT NULL,
  `surname` VARCHAR(80) NOT NULL,
  PRIMARY KEY (`id_people`),
  UNIQUE INDEX `id_people_UNIQUE` (`id_people` ASC));
