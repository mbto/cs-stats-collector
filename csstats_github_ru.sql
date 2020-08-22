-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: localhost    Database: csstats
-- ------------------------------------------------------
-- Server version	8.0.21

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `history` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int unsigned NOT NULL,
  `old_rank_id` int unsigned DEFAULT NULL,
  `new_rank_id` int unsigned DEFAULT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `history_player_id_idx` (`player_id`),
  KEY `history_old_rank_id_idx` (`old_rank_id`),
  KEY `history_new_rank_id_idx` (`new_rank_id`),
  CONSTRAINT `history_new_rank_id_fk` FOREIGN KEY (`new_rank_id`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `history_old_rank_id_fk` FOREIGN KEY (`old_rank_id`) REFERENCES `rank` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `history_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `player` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(31) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `kills` int unsigned NOT NULL DEFAULT '0',
  `deaths` int unsigned NOT NULL DEFAULT '0',
  `time_secs` int unsigned NOT NULL DEFAULT '0',
  `rank_id` int unsigned DEFAULT NULL,
  `lastseen_datetime` datetime DEFAULT NULL,
  `last_server_name` varchar(31) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `player_rank_id_idx` (`rank_id`),
  CONSTRAINT `player_rank_id_fk` FOREIGN KEY (`rank_id`) REFERENCES `rank` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player`
--

LOCK TABLES `player` WRITE;
/*!40000 ALTER TABLE `player` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `player` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_BEFORE_INSERT` BEFORE INSERT ON `player` FOR EACH ROW BEGIN
	set NEW.rank_id = calculate_rank_id(NEW.kills, NEW.deaths, NEW.time_secs);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_BEFORE_UPDATE` BEFORE UPDATE ON `player` FOR EACH ROW BEGIN
	IF (!(OLD.kills <=> NEW.kills)
     or !(OLD.deaths <=> NEW.deaths)
     or !(OLD.time_secs <=> NEW.time_secs)) THEN
		set NEW.rank_id = calculate_rank_id(NEW.kills, NEW.deaths, NEW.time_secs);
        
		if (!(OLD.rank_id <=> NEW.rank_id)) then
			insert into history (player_id, old_rank_id, new_rank_id)
			values (NEW.id, OLD.rank_id, NEW.rank_id);
		end if;
	END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `player_ip`
--

DROP TABLE IF EXISTS `player_ip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_ip` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int unsigned NOT NULL,
  `ip` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `player_ip_player_id_idx` (`player_id`),
  CONSTRAINT `player_ip_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player_ip`
--

LOCK TABLES `player_ip` WRITE;
/*!40000 ALTER TABLE `player_ip` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `player_ip` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_ip_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_ip_BEFORE_INSERT` BEFORE INSERT ON `player_ip` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(95);
	declare existed_id int unsigned;
    
    if( !is_valid_ip(NEW.ip, false, false)) then
		set error_msg = concat('Invalid ip=', ifnull(NEW.ip, ''));
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
    select id into existed_id from `player_ip` 
		where player_id = NEW.player_id 
        and ip = NEW.ip limit 1;
    
	if(existed_id is not null) then
		/* Unable to insert ip=255.255.255.255, due for player_id=4294967295 already existed id=4294967295 @ len=95 */
		set error_msg = concat('Unable to insert ip=', ifnull(NEW.ip, ''), ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_ip_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_ip_BEFORE_UPDATE` BEFORE UPDATE ON `player_ip` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(119);
	declare existed_id int unsigned;
    
    if( !is_valid_ip(NEW.ip, false, false)) then
		set error_msg = concat('Invalid ip=', ifnull(NEW.ip, ''));
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
    select id into existed_id from `player_ip` 
		where player_id = NEW.player_id 
        and ip = NEW.ip 
        and id != OLD.id limit 1;
    
	if(existed_id is not null) then
		/* Unable to update ip from 255.255.255.255 to 255.255.255.255, due for player_id=4294967295 already existed id=4294967295 @ len=119 */
		set error_msg = concat('Unable to update ip from ', ifnull(OLD.ip, ''), ' to ', ifnull(NEW.ip, ''), ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `player_steamid`
--

DROP TABLE IF EXISTS `player_steamid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_steamid` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int unsigned NOT NULL,
  `steamid` varchar(22) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `player_steamid_player_id_idx` (`player_id`),
  CONSTRAINT `player_steamid_player_id_fk` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player_steamid`
--

LOCK TABLES `player_steamid` WRITE;
/*!40000 ALTER TABLE `player_steamid` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `player_steamid` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_steamid_BEFORE_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_steamid_BEFORE_INSERT` BEFORE INSERT ON `player_steamid` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(107);
	declare existed_id int unsigned;
    
    if( !is_valid_steamid(NEW.steamid, false, true)) then
		set error_msg = concat('Invalid steamid=', ifnull(NEW.steamid, ''));
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
    select id into existed_id from `player_steamid` 
		where player_id = NEW.player_id 
        and steamid = NEW.steamid limit 1;
    
	if(existed_id is not null) then
		/* Unable to insert steamid=STEAM_0:0:123123123123, due for player_id=4294967295 already existed id=4294967295 @ len=107 */
		set error_msg = concat('Unable to insert steamid=', ifnull(NEW.steamid, ''), ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS player_steamid_BEFORE_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_steamid_BEFORE_UPDATE` BEFORE UPDATE ON `player_steamid` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(138);
	declare existed_id int unsigned;
    
    if( !is_valid_steamid(NEW.steamid, false, true)) then
		set error_msg = concat('Invalid steamid=', ifnull(NEW.steamid, ''));
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
    select id into existed_id from `player_steamid` 
		where player_id = NEW.player_id 
        and steamid = NEW.steamid 
        and id != OLD.id limit 1;
    
	if(existed_id is not null) then
		/* Unable to update steamid from STEAM_0:0:123123123123 to STEAM_0:0:123123123123, due for player_id=4294967295 already existed id=4294967295 @ len=138 */
		set error_msg = concat('Unable to update steamid from ', ifnull(OLD.steamid, ''), ' to ', ifnull(NEW.steamid, ''), ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rank`
--

DROP TABLE IF EXISTS `rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rank` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `level` int unsigned NOT NULL,
  `name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `level_UNIQUE` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rank`
--

LOCK TABLES `rank` WRITE;
/*!40000 ALTER TABLE `rank` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `rank` VALUES (1,1,'Сынок');
INSERT INTO `rank` VALUES (2,2,'Тюфяк');
INSERT INTO `rank` VALUES (3,3,'Овощ');
INSERT INTO `rank` VALUES (4,4,'Кабан');
INSERT INTO `rank` VALUES (5,5,'Силач');
INSERT INTO `rank` VALUES (6,6,'Шароеб');
INSERT INTO `rank` VALUES (7,7,'Пацан');
INSERT INTO `rank` VALUES (8,8,'Смертник');
INSERT INTO `rank` VALUES (9,9,'Везунчик');
INSERT INTO `rank` VALUES (10,10,'Жульбан');
INSERT INTO `rank` VALUES (11,11,'Гопник');
INSERT INTO `rank` VALUES (12,12,'Кэмпер');
INSERT INTO `rank` VALUES (13,13,'Помощник');
INSERT INTO `rank` VALUES (14,14,'Вуйко');
INSERT INTO `rank` VALUES (15,15,'Донышко');
INSERT INTO `rank` VALUES (16,16,'Профан');
INSERT INTO `rank` VALUES (17,17,'Титушка');
INSERT INTO `rank` VALUES (18,18,'Боцман');
INSERT INTO `rank` VALUES (19,19,'Школьник');
INSERT INTO `rank` VALUES (20,20,'Мусор');
INSERT INTO `rank` VALUES (21,21,'Отбой');
INSERT INTO `rank` VALUES (22,22,'ПТУ-шник');
INSERT INTO `rank` VALUES (23,23,'Зек');
INSERT INTO `rank` VALUES (24,24,'Бывалый');
INSERT INTO `rank` VALUES (25,25,'Прораб');
INSERT INTO `rank` VALUES (26,26,'Жестянщик');
INSERT INTO `rank` VALUES (27,27,'Пахан');
INSERT INTO `rank` VALUES (28,28,'Директор');
INSERT INTO `rank` VALUES (29,29,'Гастролер');
INSERT INTO `rank` VALUES (30,30,'Мордоворот');
INSERT INTO `rank` VALUES (31,31,'Геймер');
INSERT INTO `rank` VALUES (32,32,'Отважный');
INSERT INTO `rank` VALUES (33,33,'Убийца');
INSERT INTO `rank` VALUES (34,34,'Халявщик');
INSERT INTO `rank` VALUES (35,35,'Псих');
INSERT INTO `rank` VALUES (36,36,'Йовбак');
INSERT INTO `rank` VALUES (37,37,'Громила');
INSERT INTO `rank` VALUES (38,38,'Мужик');
INSERT INTO `rank` VALUES (39,39,'Дезертир');
INSERT INTO `rank` VALUES (40,40,'Боец');
INSERT INTO `rank` VALUES (41,41,'Софт');
INSERT INTO `rank` VALUES (42,42,'Громила-здоровяк');
INSERT INTO `rank` VALUES (43,43,'Партизан');
INSERT INTO `rank` VALUES (44,44,'Сенсей');
INSERT INTO `rank` VALUES (45,45,'Рыцарь');
INSERT INTO `rank` VALUES (46,46,'Спецназовец');
INSERT INTO `rank` VALUES (47,47,'Тащит всю команду');
INSERT INTO `rank` VALUES (48,48,'Олдфаг');
INSERT INTO `rank` VALUES (49,49,'Каратель');
INSERT INTO `rank` VALUES (50,50,'Здоровяк');
INSERT INTO `rank` VALUES (51,51,'Аим');
INSERT INTO `rank` VALUES (52,52,'Фраер');
INSERT INTO `rank` VALUES (53,53,'Штурмовой');
INSERT INTO `rank` VALUES (54,54,'Boss');
INSERT INTO `rank` VALUES (55,55,'Super Old School');
INSERT INTO `rank` VALUES (56,56,'Непобедимый');
/*!40000 ALTER TABLE `rank` ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_INSERT */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_INSERT` AFTER INSERT ON `rank` FOR EACH ROW BEGIN
	update player p set p.rank_id = calculate_rank_id(p.kills, p.deaths, p.time_secs);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_UPDATE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_UPDATE` AFTER UPDATE ON `rank` FOR EACH ROW BEGIN
	if(!(OLD.level <=> NEW.level)) then
		update player p set p.rank_id = calculate_rank_id(p.kills, p.deaths, p.time_secs);
    end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
/*!50032 DROP TRIGGER IF EXISTS rank_AFTER_DELETE */;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `rank_AFTER_DELETE` AFTER DELETE ON `rank` FOR EACH ROW BEGIN
	update player p set p.rank_id = calculate_rank_id(p.kills, p.deaths, p.time_secs);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Dumping routines for database 'csstats'
--
/*!50003 DROP FUNCTION IF EXISTS `build_human_time` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `build_human_time`(time_secs int unsigned) RETURNS varchar(31) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare y int unsigned default (time_secs DIV (60 * 60 * 24 * 30 * 12));
	declare mn int unsigned default (time_secs DIV (60 * 60 * 24 * 30)) % 12;
	declare d int unsigned default (time_secs DIV (60 * 60 * 24)) % 30;
	declare h int unsigned default (time_secs DIV (60 * 60)) % 24;
	declare m int unsigned default (time_secs DIV 60) % 60;
	declare s int unsigned default time_secs % 60;
    
    declare human_time varchar(31) default ''; -- 9999года 11мес 29дн 23ч 59м 59с @ len=31
    
    if(y > 0) then set human_time = concat(human_time,y,declension(y,'год','года','лет')); end if;
    if(mn > 0) then set human_time = concat(human_time,if(y > 0, ' ', ''),mn,'мес'); end if;
    if(d > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0, ' ', ''),d,'дн'); end if;
    if(h > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0, ' ', ''),h,'ч'); end if;
    if(m > 0) then set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0 or h > 0, ' ', ''),m,'м'); end if;
    if(!(y > 0 or mn > 0 or d > 0) and (((s > 0 and (h > 0 or m > 0))) or (h = 0 and m = 0))) then
		set human_time = concat(human_time,if(y > 0 or mn > 0 or d > 0 or h > 0 or m > 0, ' ', ''),s,'с'); 
    end if;
    
	RETURN human_time;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `build_stars` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `build_stars`(level int unsigned, ranks_total int unsigned) RETURNS varchar(6) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare black_stars int unsigned default greatest(1, truncate(level * 6 / ranks_total, 0));
	declare white_stars int unsigned default 6 - black_stars;
	
	return concat(repeat("★", black_stars), repeat("☆", white_stars));
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `calculate_rank_id` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `calculate_rank_id`(kills int unsigned, deaths int unsigned, time_secs int unsigned) RETURNS int unsigned
    READS SQL DATA
    DETERMINISTIC
BEGIN
    declare ranks_count int unsigned default (select count(*) from `rank`);
    
    declare hero_days int unsigned default 30;
    declare hero_kills int unsigned default 133/*frags per day*/ * hero_days;
    declare hero_skill int unsigned default calculate_skill(hero_kills, hero_kills * 0.4/* 40% */ );
    declare hero_time int unsigned default (hero_days/*days*/ * 24/*hours*/ * 60/*mins*/ * 60/*secs*/ );
    
    declare skill double default calculate_skill(kills, deaths);
    
    declare kills_pos int unsigned default greatest(1, least(ranks_count, ceil((skill * ranks_count) / hero_skill)));
    declare time_secs_pos int unsigned default greatest(1, least(ranks_count, floor((time_secs * ranks_count) / hero_time)));
	declare rank_num int unsigned default floor((kills_pos + time_secs_pos) / 2);
    
    declare new_rank_id int unsigned;
    
	if ranks_count > 0 then
		with cte as (select (row_number() over()) as num, id from `rank` order by level asc)
			select id from cte where num = rank_num into new_rank_id;
            
        return ifnull(new_rank_id, (select id from `rank` order by level asc limit 1));
	end if;
    
    return null;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `calculate_skill` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `calculate_skill`(kills bigint, deaths bigint) RETURNS int
    DETERMINISTIC
BEGIN
	declare weight double default ((kills - deaths) / (kills / (kills + deaths)));
	declare killsWeight double default weight / 100.0;
	return round(100 * ((kills / (kills + deaths)) * killsWeight), 0);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `declension` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `declension`(value int, opt1 varchar(32), opt2 varchar(32), opt3 varchar(32)) RETURNS varchar(32) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare n int unsigned default abs(value);
	if(n > 10 and n < 20) then return opt3; end if;
	if((n mod 10) > 1 and (n mod 10) < 5) then return opt2; end if;
	if((n mod 10) = 1) then return opt1; end if;
	return opt3;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `is_valid_ip` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `is_valid_ip`(ip varchar(21), nullable boolean, with_port boolean) RETURNS tinyint unsigned
    DETERMINISTIC
BEGIN
	if(nullable and ip is null) then
		return true;
	end if;
    
    if with_port then
		return (select ip regexp "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}$") is true;
	end if;
	
	return (select ip regexp "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$") is true;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `is_valid_steamid` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `is_valid_steamid`(steamid varchar(22), nullable boolean, only_legal boolean) RETURNS tinyint unsigned
    DETERMINISTIC
BEGIN
	if(nullable and steamid is null) then
		return true;
	end if;
    
	if only_legal then
		return (select steamid regexp "^STEAM_[0-1]:[0-1]:[0-9]+$") is true;
	end if;
    
    return (select steamid regexp "^STEAM_\\d+:\\d+:\\d+$") is true;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerDetail` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerDetail`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

select `player`.`id`,
       `player`.`name`,
       `player`.`kills`,
       `player`.`deaths`,
       `build_human_time`(`player`.`time_secs`) as `gaming_time`,
       `rank`.`name` as `rank_name`,
       `build_stars`(`rank`.`level`, (select count(*) from `rank`)) as `stars`
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` desc limit page, per_page
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerDetailJson` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerDetailJson`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select count(*) over() as count_total, 
	json_object('id', `player`.`id`,
		'name', `player`.`name`,
		'kills', `player`.`kills`,
		'deaths', `player`.`deaths`,
		'gaming_time', `build_human_time`(`player`.`time_secs`),
		'rank_name', `rank`.`name`,
		'stars', `build_stars`(`rank`.`level`, (select count(*) from `rank`))) as results
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` desc limit page, per_page)
select
 case when sub.count_total > 0
  then sub.results
-- else json_object() -- bug, not working
 end as results
from sub
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerDetailJsonAgg` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerDetailJsonAgg`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select count(*) over() as count_total, 
	json_object('id', `player`.`id`,
		'name', `player`.`name`,
		'kills', `player`.`kills`,
		'deaths', `player`.`deaths`,
		'gaming_time', `build_human_time`(`player`.`time_secs`),
		'rank_name', `rank`.`name`,
		'stars', `build_stars`(`rank`.`level`, (select count(*) from `rank`))) as results
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` desc limit page, per_page)
select
 case when sub.count_total > 0
  then json_object('count_total', sub.count_total, 'results', json_arrayagg(sub.results))
  else json_object('count_total', 0, 'results', cast('[]' as json))
 end as results
from sub
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerFull` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerFull`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

select `player`.`id`,
       `player`.`name`,
       `player`.`kills`,
       `player`.`deaths`,
       `build_human_time`(`player`.`time_secs`) as `gaming_time`,
       `rank`.`name` as `rank_name`,
       `build_stars`(`rank`.`level`, (select count(*) from `rank`)) as `stars`,
       `known_server`.`name` as `last_server_name`,
       `player`.`lastseen_datetime` as `lastseen_datetime`,
       `ips`.`grouped` as `ips`,
       `steamids`.`grouped` as `steamids`
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
		 left outer join `known_server` on `player`.`last_server_id` = `known_server`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`,
         lateral (select substring_index(group_concat(distinct `player_ip`.`ip` 
			order by `player_ip`.`reg_datetime` desc SEPARATOR ','), ',', 15) as `grouped`
				from `player_ip` where `player`.`id` = `player_ip`.`player_id`) as `ips`, 
         lateral (select substring_index(group_concat(distinct `player_steamid`.`steamid` 
			order by `player_steamid`.`reg_datetime` desc SEPARATOR ','), ',', 15) as `grouped`
				from `player_steamid` where `player`.`id` = `player_steamid`.`player_id`) as `steamids`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerFull2` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerFull2`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

select `player`.`id`,
       `player`.`name`,
       `player`.`kills`,
       `player`.`deaths`,
       `build_human_time`(`player`.`time_secs`) as `gaming_time`,
       `rank`.`name` as `rank_name`,
       `build_stars`(`rank`.`level`, (select count(*) from `rank`)) as `stars`,
       `known_server`.`name` as `last_server_name`,
       `player`.`lastseen_datetime` as `lastseen_datetime`,
       `ips`.`grouped` as `ips`,
       `steamids`.`grouped` as `steamids`
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
		 left outer join `known_server` on `player`.`last_server_id` = `known_server`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`,
         lateral (select cast(concat('[', substring_index(group_concat(distinct concat('"', `player_ip`.`ip`, '"')
			order by `player_ip`.`reg_datetime` desc SEPARATOR ','), ',', 15), ']') as json) as `grouped`
				from `player_ip` where `player`.`id` = `player_ip`.`player_id`) as `ips`,
         lateral (select cast(concat('[', substring_index(group_concat(distinct concat('"', `player_steamid`.`steamid`, '"')
			order by `player_steamid`.`reg_datetime` desc SEPARATOR ','), ',', 15), ']') as json) as `grouped`
				from `player_steamid` where `player`.`id` = `player_steamid`.`player_id`) as `steamids`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerFull2Json` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerFull2Json`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select count(*) over() as count_total, 
	json_object('id', `player`.`id`,
		'name', `player`.`name`,
		'kills', `player`.`kills`,
		'deaths', `player`.`deaths`,
		'gaming_time', `build_human_time`(`player`.`time_secs`),
		'rank_name', `rank`.`name`,
		'stars', `build_stars`(`rank`.`level`, (select count(*) from `rank`)),
		'last_server_name', `known_server`.`name`,
		'lastseen_datetime', DATE_FORMAT(`player`.`lastseen_datetime`, '%Y-%m-%d %H:%i:%s'),
		'ips', ips.grouped,
		'steamids', steamids.grouped) as results
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
		 left outer join `known_server` on `player`.`last_server_id` = `known_server`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`,
         lateral (select cast(concat('[', substring_index(group_concat(distinct concat('"', `player_ip`.`ip`, '"')
			order by `player_ip`.`reg_datetime` desc SEPARATOR ','), ',', 15), ']') as json) as `grouped`
				from `player_ip` where `player`.`id` = `player_ip`.`player_id`) as `ips`,
         lateral (select cast(concat('[', substring_index(group_concat(distinct concat('"', `player_steamid`.`steamid`, '"')
			order by `player_steamid`.`reg_datetime` desc SEPARATOR ','), ',', 15), ']') as json) as `grouped`
				from `player_steamid` where `player`.`id` = `player_steamid`.`player_id`) as `steamids`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page)
select
 case when sub.count_total > 0
  then sub.results
-- else json_object() -- bug, not working
 end as results
from sub
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerFull2JsonAgg` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerFull2JsonAgg`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select count(*) over() as count_total, 
	json_object('id', `player`.`id`,
		'name', `player`.`name`,
		'kills', `player`.`kills`,
		'deaths', `player`.`deaths`,
		'gaming_time', `build_human_time`(`player`.`time_secs`),
		'rank_name', `rank`.`name`,
		'stars', `build_stars`(`rank`.`level`, (select count(*) from `rank`)),
		'last_server_name', `known_server`.`name`,
		'lastseen_datetime', DATE_FORMAT(`player`.`lastseen_datetime`, '%Y-%m-%d %H:%i:%s'),
		'ips', ips.grouped,
		'steamids', steamids.grouped) as results
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
		 left outer join `known_server` on `player`.`last_server_id` = `known_server`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`,
         lateral (select cast(concat('[', substring_index(group_concat(distinct concat('"', `player_ip`.`ip`, '"')
			order by `player_ip`.`reg_datetime` desc SEPARATOR ','), ',', 15), ']') as json) as `grouped`
				from `player_ip` where `player`.`id` = `player_ip`.`player_id`) as `ips`,
         lateral (select cast(concat('[', substring_index(group_concat(distinct concat('"', `player_steamid`.`steamid`, '"')
			order by `player_steamid`.`reg_datetime` desc SEPARATOR ','), ',', 15), ']') as json) as `grouped`
				from `player_steamid` where `player`.`id` = `player_steamid`.`player_id`) as `steamids`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page)
select
 case when sub.count_total > 0
  then json_object('count_total', sub.count_total, 'results', json_arrayagg(sub.results))
  else json_object('count_total', 0, 'results', cast('[]' as json))
 end as results
from sub
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerFullJson` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerFullJson`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select count(*) over() as count_total, 
	json_object('id', `player`.`id`,
		'name', `player`.`name`,
		'kills', `player`.`kills`,
		'deaths', `player`.`deaths`,
		'gaming_time', `build_human_time`(`player`.`time_secs`),
		'rank_name', `rank`.`name`,
		'stars', `build_stars`(`rank`.`level`, (select count(*) from `rank`)),
		'last_server_name', `known_server`.`name`,
		'lastseen_datetime', DATE_FORMAT(`player`.`lastseen_datetime`, '%Y-%m-%d %H:%i:%s'),
		'ips', ips.grouped,
		'steamids', steamids.grouped) as results
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
		 left outer join `known_server` on `player`.`last_server_id` = `known_server`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`,
         lateral (select substring_index(group_concat(distinct `player_ip`.`ip` 
			order by `player_ip`.`reg_datetime` desc SEPARATOR ','), ',', 15) as `grouped`
				from `player_ip` where `player`.`id` = `player_ip`.`player_id`) as `ips`, 
         lateral (select substring_index(group_concat(distinct `player_steamid`.`steamid` 
			order by `player_steamid`.`reg_datetime` desc SEPARATOR ','), ',', 15) as `grouped`
				from `player_steamid` where `player`.`id` = `player_steamid`.`player_id`) as `steamids`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page)
select
 case when sub.count_total > 0
  then sub.results
-- else json_object() -- bug, not working
 end as results
from sub
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerFullJsonAgg` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerFullJsonAgg`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select count(*) over() as count_total, 
	json_object('id', `player`.`id`,
		'name', `player`.`name`,
		'kills', `player`.`kills`,
		'deaths', `player`.`deaths`,
		'gaming_time', `build_human_time`(`player`.`time_secs`),
		'rank_name', `rank`.`name`,
		'stars', `build_stars`(`rank`.`level`, (select count(*) from `rank`)),
		'last_server_name', `known_server`.`name`,
		'lastseen_datetime', DATE_FORMAT(`player`.`lastseen_datetime`, '%Y-%m-%d %H:%i:%s'),
		'ips', ips.grouped,
		'steamids', steamids.grouped) as results
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
		 left outer join `known_server` on `player`.`last_server_id` = `known_server`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`,
         lateral (select substring_index(group_concat(distinct `player_ip`.`ip` 
			order by `player_ip`.`reg_datetime` desc SEPARATOR ','), ',', 15) as `grouped`
				from `player_ip` where `player`.`id` = `player_ip`.`player_id`) as `ips`, 
         lateral (select substring_index(group_concat(distinct `player_steamid`.`steamid` 
			order by `player_steamid`.`reg_datetime` desc SEPARATOR ','), ',', 15) as `grouped`
				from `player_steamid` where `player`.`id` = `player_steamid`.`player_id`) as `steamids`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page)
select
 case when sub.count_total > 0
  then json_object('count_total', sub.count_total, 'results', json_arrayagg(sub.results))
  else json_object('count_total', 0, 'results', cast('[]' as json))
 end as results
from sub
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerHistoryJsonAgg` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerHistoryJsonAgg`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned, history_limit int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select
	count(*) over() as count_total,
	h.player_name,
	h.player_level,
	json_object('player_id', h.player_id,
				'history', json_arrayagg(json_object('history_id', h.id,
					'reg_datetime', DATE_FORMAT(h.reg_datetime, '%Y-%m-%d %H:%i:%s'),
					'old_level', case when r1.id is not null then json_object(r1.`level`, r1.`name`) end,
					'new_level', case when r2.id is not null then json_object(r2.`level`, r2.`name`) end))) as player
from (select * from
		(select h.*, 
				p.name as player_name, 
                r.`level` as player_level, 
                row_number() over(partition by h.player_id 
					order by r.`level` desc, h.reg_datetime desc, h.id desc) rownum -- #ranking
			from history h join player p on h.player_id = p.id
				left outer join `rank` r on p.rank_id = r.id
                left outer join `player_ip` pip on p.id = pip.player_id
				left outer join `player_steamid` psid on p.id = psid.player_id
			where (id is null or p.id = id) -- #filtering players
				and ((name is null or p.`name` = name)
				and (ip is null or pip.ip = ip)
				and (steamid is null or psid.steamid = steamid))
			group by h.id -- #grouping history
			order by r.`level` desc, h.reg_datetime desc, h.id desc -- #sorting
		) h
    where h.rownum <= history_limit) h -- #limitation per group
		left outer join `rank` r1 on h.old_rank_id = r1.id
		left outer join `rank` r2 on h.new_rank_id = r2.id
 group by h.player_id -- #grouping players
limit page, per_page -- #limitation per players
) select
 case when sub.count_total is not null
  then json_object('count_total', sub.count_total, 'results', json_arrayagg(json_object(sub.player_name, sub.player)) over(order by sub.player_level desc))
--  else json_object('count_total', 0, 'results', cast('[]' as json)) -- not working
 end as results
from sub order by sub.player_level asc limit 1 #trick for sorting in json_arrayagg with over() =/
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerSummary` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerSummary`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

select `player`.`id`,
       `player`.`name`,
       `rank`.`name` as `rank_name`,
       `build_stars`(`rank`.`level`, (select count(*) from `rank`)) as `stars`
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerSummaryJson` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerSummaryJson`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select count(*) over() as count_total, 
	json_object('id', `player`.`id`,
		'name', `player`.`name`,
		'rank_name', `rank`.`name`,
		'stars', `build_stars`(`rank`.`level`, (select count(*) from `rank`))) as results
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page)
select
 case when sub.count_total > 0
  then sub.results
-- else json_object() -- bug, not working
 end as results
from sub
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `PlayerSummaryJsonAgg` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `PlayerSummaryJsonAgg`(id int unsigned, name varchar(31), 
ip varchar(15), steamid varchar(22), page int unsigned, per_page int unsigned)
BEGIN
set page = ((page - 1) * per_page);

with sub as (select count(*) over() as count_total, 
	json_object('id', `player`.`id`,
		'name', `player`.`name`,
		'rank_name', `rank`.`name`,
		'stars', `build_stars`(`rank`.`level`, (select count(*) from `rank`))) as results
from `player`
         left outer join `rank` on `player`.`rank_id` = `rank`.`id`
         left outer join `player_ip` on `player`.`id` = `player_ip`.`player_id`
         left outer join `player_steamid` on `player`.`id` = `player_steamid`.`player_id`
where (id is null or `player`.`id` = id)
and ((name is null or `player`.`name` = name)
and (ip is null or `player_ip`.`ip` = ip)
and (steamid is null or `player_steamid`.`steamid` = steamid))
group by `player`.`id` order by `rank`.`level` desc, `player`.`time_secs` limit page, per_page)
select
 case when sub.count_total > 0
  then json_object('count_total', sub.count_total, 'results', json_arrayagg(sub.results))
  else json_object('count_total', 0, 'results', cast('[]' as json))
 end as results
from sub
;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-08-22  4:20:39
