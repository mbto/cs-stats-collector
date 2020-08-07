-- MySQL dump 10.13  Distrib 8.0.20, for Win64 (x86_64)
--
-- Host: localhost    Database: csstats_github
-- ------------------------------------------------------
-- Server version	8.0.20

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
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `known_server`
--

DROP TABLE IF EXISTS `known_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `known_server` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `ipport` varchar(21) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ip:port of the server from which the logs will be expected',
  `active` tinyint unsigned NOT NULL DEFAULT '0' COMMENT 'Are ip:port allowed?: 1-allowed; 0-not allowed (logs/stats from this ip:port will be ignored)',
  `ffa` tinyint unsigned NOT NULL DEFAULT '0' COMMENT 'game server is FREE-FOR-ALL mode (Example: CS-DeathMatch): 1-true; 0-false',
  `ignore_bots` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '1-ignore statistics, when killer or victim is BOT; 0-don''t ignore (include all player''s)',
  `start_session_on_action` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '1-start player''s session on event "... killed ... with ..." (not for kreedz servers); 0-start player''s session on event "... connected, address ..." or "... entered the game"',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `ipport_UNIQUE` (`ipport`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `known_server`
--

LOCK TABLES `known_server` WRITE;
/*!40000 ALTER TABLE `known_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `known_server` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `known_server_BEFORE_INSERT` BEFORE INSERT ON `known_server` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(36);
    
	if( !is_valid_ip(NEW.ipport, false, true)) then
		/* Invalid ipport=255.255.255.255:12345 @ len=36*/
		set error_msg = concat('Invalid ipport=', NEW.ipport);
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
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `known_server_BEFORE_UPDATE` BEFORE UPDATE ON `known_server` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(36);
    
    if( !is_valid_ip(NEW.ipport, false, true)) then
		/* Invalid ipport=255.255.255.255:12345 @ len=36*/
		set error_msg = concat('Invalid ipport=', NEW.ipport);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `manager`
--

DROP TABLE IF EXISTS `manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manager` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `active` tinyint unsigned NOT NULL DEFAULT '1',
  `username` varchar(31) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'https://www.browserling.com/tools/bcrypt',
  `reg_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manager`
--

LOCK TABLES `manager` WRITE;
/*!40000 ALTER TABLE `manager` DISABLE KEYS */;
INSERT INTO `manager` VALUES (1,1,'stats','$2a$10$sE7hgQ8crpIxQk33J4hQAudOrnwDu0DG9n3T.c4x85YDzWwLHsKde','2020-01-01 00:00:00');
/*!40000 ALTER TABLE `manager` ENABLE KEYS */;
UNLOCK TABLES;

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
  `last_server_id` int unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `player_rank_id_idx` (`rank_id`),
  KEY `player_last_server_id_idx` (`last_server_id`),
  CONSTRAINT `player_last_server_id_fk` FOREIGN KEY (`last_server_id`) REFERENCES `known_server` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `player_rank_id_fk` FOREIGN KEY (`rank_id`) REFERENCES `rank` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player`
--

LOCK TABLES `player` WRITE;
/*!40000 ALTER TABLE `player` DISABLE KEYS */;
/*!40000 ALTER TABLE `player` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
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
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_BEFORE_UPDATE` BEFORE UPDATE ON `player` FOR EACH ROW BEGIN
	IF (!(OLD.kills <=> NEW.kills)
     or !(OLD.deaths <=> NEW.deaths)
     or !(OLD.time_secs <=> NEW.time_secs)) THEN
		set NEW.rank_id = calculate_rank_id(NEW.kills, NEW.deaths, NEW.time_secs);
        
		if (!(OLD.rank_id <=> NEW.rank_id)) then
			insert into history (player_id, old_rank_id, new_rank_id, reg_datetime)
			values (NEW.id, OLD.rank_id, NEW.rank_id, current_timestamp());
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
  `reg_datetime` datetime NOT NULL,
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
/*!40000 ALTER TABLE `player_ip` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_ip_BEFORE_INSERT` BEFORE INSERT ON `player_ip` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(95);
	declare existed_id int unsigned;
	
    if( !is_valid_ip(NEW.ip, false, false)) then
		set error_msg = concat('Invalid ip=', NEW.ip);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
    select id into existed_id from `player_ip` 
		where player_id = NEW.player_id 
        and ip = NEW.ip limit 1;
    
	if(existed_id is not null) then
		/* Unable to insert ip=255.255.255.255, due for player_id=4294967295 already existed id=4294967295 @ len=95 */
		set error_msg = concat('Unable to insert ip=', NEW.ip, ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
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
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_ip_BEFORE_UPDATE` BEFORE UPDATE ON `player_ip` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(119);
	declare existed_id int unsigned;
    
    if( !is_valid_ip(NEW.ip, false, false)) then
		set error_msg = concat('Invalid ip=', NEW.ip);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
    select id into existed_id from `player_ip` 
		where player_id = NEW.player_id 
        and ip = NEW.ip 
        and id != OLD.id limit 1;
    
	if(existed_id is not null) then
		/* Unable to update ip from 255.255.255.255 to 255.255.255.255, due for player_id=4294967295 already existed id=4294967295 @ len=119 */
		set error_msg = concat('Unable to update ip from ', OLD.ip, ' to ', NEW.ip, ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
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
  `reg_datetime` datetime NOT NULL,
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
/*!40000 ALTER TABLE `player_steamid` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_steamid_BEFORE_INSERT` BEFORE INSERT ON `player_steamid` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(107);
	declare existed_id int unsigned;
    
    if( !is_valid_steamid(NEW.steamid, false, true)) then
		set error_msg = concat('Invalid steamid=', NEW.steamid);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
    select id into existed_id from `player_steamid` 
		where player_id = NEW.player_id 
        and steamid = NEW.steamid limit 1;
    
	if(existed_id is not null) then
		/* Unable to insert steamid=STEAM_0:0:123123123123, due for player_id=4294967295 already existed id=4294967295 @ len=107 */
		set error_msg = concat('Unable to insert steamid=', NEW.steamid, ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
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
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`%`*/ /*!50003 TRIGGER `player_steamid_BEFORE_UPDATE` BEFORE UPDATE ON `player_steamid` FOR EACH ROW BEGIN
	declare error_msg VARCHAR(138);
	declare existed_id int unsigned;
    
    if( !is_valid_steamid(NEW.steamid, false, true)) then
		set error_msg = concat('Invalid steamid=', NEW.steamid);
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    end if;
    
    select id into existed_id from `player_steamid` 
		where player_id = NEW.player_id 
        and steamid = NEW.steamid 
        and id != OLD.id limit 1;
    
	if(existed_id is not null) then
		/* Unable to update steamid from STEAM_0:0:123123123123 to STEAM_0:0:123123123123, due for player_id=4294967295 already existed id=4294967295 @ len=138 */
		set error_msg = concat('Unable to update steamid from ', OLD.steamid, ' to ', NEW.steamid, ', due for player_id=', NEW.player_id, ' already existed id=', existed_id);
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
INSERT INTO `rank` VALUES (1,1,'Сынок'),(2,2,'Тюфяк'),(3,3,'Овощ'),(4,4,'Кабан'),(5,5,'Силач'),(6,6,'Шароеб'),(7,7,'Пацан'),(8,8,'Смертник'),(9,9,'Везунчик'),(10,10,'Жульбан'),(11,11,'Гопник'),(12,12,'Кэмпер'),(13,13,'Помощник'),(14,14,'Вуйко'),(15,15,'Донышко'),(16,16,'Профан'),(17,17,'Титушка'),(18,18,'Боцман'),(19,19,'Школьник'),(20,20,'Мусор'),(21,21,'Отбой'),(22,22,'ПТУ-шник'),(23,23,'Зек'),(24,24,'Бывалый'),(25,25,'Прораб'),(26,26,'Жестянщик'),(27,27,'Пахан'),(28,28,'Директор'),(29,29,'Гастролер'),(30,30,'Мордоворот'),(31,31,'Геймер'),(32,32,'Отважный'),(33,33,'Убийца'),(34,34,'Халявщик'),(35,35,'Псих'),(36,36,'Йовбак'),(37,37,'Громила'),(38,38,'Мужик'),(39,39,'Дезертир'),(40,40,'Боец'),(41,41,'Софт'),(42,42,'Громила-здоровяк'),(43,43,'Партизан'),(44,44,'Сенсей'),(45,45,'Рыцарь'),(46,46,'Спецназовец'),(47,47,'Тащит всю команду'),(48,48,'Олдфаг'),(49,49,'Каратель'),(50,50,'Здоровяк'),(51,51,'Аим'),(52,52,'Фраер'),(53,53,'Штурмовой'),(54,54,'Boss'),(55,55,'Super Old School'),(56,56,'Непобедимый');
/*!40000 ALTER TABLE `rank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'csstats_github'
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
CREATE DEFINER=`root`@`%` FUNCTION `build_human_time`(time_secs int) RETURNS varchar(31) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    DETERMINISTIC
BEGIN
	declare y int unsigned default (time_secs DIV (60 * 60 * 24 * 30 * 12));
	declare mn int unsigned default (time_secs DIV (60 * 60 * 24 * 30)) % 12;
	declare d int unsigned default (time_secs DIV (60 * 60 * 24)) % 30;
	declare h int unsigned default (time_secs DIV (60 * 60)) % 24;
	declare m int unsigned default (time_secs DIV 60) % 60;
	declare s int unsigned default time_secs % 60;
    
    declare human_time varchar(31) default ''; -- 9999года 11мес 29дн 23ч 59м 59с = 31 len
    
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
    declare hero_skill int unsigned default calculate_skill(hero_kills, hero_kills * 0.4/* 40% */);
    declare hero_time int unsigned default (hero_days/*days*/ * 24/*hours*/ * 60/*mins*/ * 60/*secs*/);
    
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
	if(nullable = true and ip is null) then
		return true;
	end if;
    
    if(with_port = true) then
		return (select ip regexp "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}$");
	end if;
	
	return (select ip regexp "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
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
	if(nullable = true and steamid is null) then
		return true;
	end if;
    
	if only_legal then
		return (select steamid regexp "^STEAM_[0-1]:[0-1]:[0-9]+$");
	end if;
    
    return (select steamid regexp "^STEAM_\\d+:\\d+:\\d+$");
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

-- Dump completed on 2020-08-08  1:31:44
