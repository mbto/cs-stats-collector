# cs-stats-collector
* Listening UDP port 8888
* Consuming game logs from counter-strike dedicated-servers
* Collecting & caching players statistics (kills, deaths, online at server in seconds)
* Merging players statistics to MySQL database on 'next map', 'shutdown server' events
* Provides REST-api for management

### **Install:**

server.cfg:
```
// logs
logaddress 127.0.0.1 8888
log on
mp_logfile 1		// Записывать логи сервера в файлы
mp_logecho 1		// Показывать информацию из логов сервера в консоль
mp_logmessages 1	// Писать в лог чат игроков
mp_logdetail 0		// Записывать в лог повреждения от: 0 - выключено; 1 - противников; 2 - своих; 3 - и от противников и от своих.
sv_log_onefile 1	// 0 - Записывать в разные файлы по порядку, 1 - записывать в один файл
sv_logbans 1		// Записывать в лог баны
sv_logblocks 1		// 1 - log warn Log_Printf("Traffic from %s was blocked for exceeding rate limits\n", NET_AdrToString(adr));
sv_logrelay 1		// 1 - логировать в консоль сообщение по команде log на уровне getchallenge
sv_logsecret 0		// 0 - отправлять логи как "log %s", 1 - "%c%s%s", S2A_LOGKEY, sv_logsecret.string, string
```

MySQL tables:
```
DROP TABLE IF EXISTS `csstats`;
CREATE TABLE `csstats` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_bin NOT NULL,
  `kills` bigint NOT NULL DEFAULT '0',
  `deaths` bigint NOT NULL DEFAULT '0',
  `time_secs` bigint unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `csstats_servers`;
CREATE TABLE `csstats_servers` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `ipport` varchar(21) COLLATE utf8_bin NOT NULL,
  `active` tinyint NOT NULL DEFAULT '0',
  `ffa` tinyint NOT NULL DEFAULT '0',
  `ignore_bots` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `ipport_UNIQUE` (`ipport`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

LOCK TABLES `csstats_servers` WRITE;
INSERT INTO `csstats_servers` VALUES (1,'127.0.0.1:27015',1,1,1);
UNLOCK TABLES;
```

Config location:
```
/src/main/resources/application.properties - copy config into /opt/csstats/
```

Structure:
```
root@user-desktop:~# ls /opt/csstats/
application.properties  cs-stats-collector.jar  logs/
```

Launch:
```
/usr/bin/java -jar /opt/csstats/cs-stats-collector.jar --spring.config.location=/opt/csstats/application.properties >> /opt/csstats/logs/collector.log
```

Management - use Postman or Curl:
```
GET http://localhost:8890/stats - prints in json format current players statistics & servers settings
POST http://localhost:8890/stats/flush - manually merge all players statistics to database
POST http://localhost:8890/stats/updateSettings - reload & apply servers settings from database, without restart JVM.
```

### **Compile:**
```
gradle assemble
```
