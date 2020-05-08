#### **cs-stats-collector**
* `Consuming game logs from counter-strike 1.6 dedicated-servers at UDP port 8888;`
* `Collecting & caching players statistics (kills, deaths, online at server in seconds) by player name;`
* `Merging players statistics to MySQL database on 'next map', 'shutdown server' events, or manually;`
* `Provides REST-api for management;`

#### **Requirements:**
* `Java 8+`
* `MySQL 8.0.20+`
---
#### **Install:**

**Add settings into server.cfg file:**
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

**Create MySQL database (optional):**
```
CREATE SCHEMA `amx` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;
```
**Create MySQL tables:**
```
USE `amx`; -- optional

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
  `ipport` varchar(21) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'ip:port of the server from which the logs will be expected',
  `active` tinyint NOT NULL DEFAULT '0' COMMENT 'Are ip:port allowed?: 1-allowed; 0-not allowed (logs/stats from this ip:port will be ignored)',
  `ffa` tinyint NOT NULL DEFAULT '0' COMMENT 'game server is FREE-FOR-ALL mode (Example: CS-DeathMatch): 1-on; 0-off',
  `ignore_bots` tinyint NOT NULL DEFAULT '0' COMMENT '1-ignore statistics, when killer or victim is BOT; 0-don''t ignore (include all players)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `ipport_UNIQUE` (`ipport`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
```

**Determine server's ip:port:**
```
Execute `net_address` at server console or rcon:
Result can be
    "net_address" is "0.0.0.0:27015"`
or
    "net_address" is "127.0.0.1:27015"`
or
    "net_address" is "192.168.1.111:27015"`
```

**Add ip:port and settings to table of allowed servers:**
```
-- for 0.0.0.0:27015 or 127.0.0.1:27015
INSERT INTO `csstats_servers` (`ipport`,`active`,`ffa`,`ignore_bots`) VALUES ('127.0.0.1:27015',1,0,0);
```
or
```
-- for 192.168.1.111:27015
INSERT INTO `csstats_servers` (`ipport`,`active`,`ffa`,`ignore_bots`) VALUES ('192.168.1.111:27015',1,0,0);
```

**Add MySQL user:**
* `stats` (password `stats`):
```
INSERT INTO `mysql`.`user` VALUES ('%', 'stats', 'Y', 'Y', 'Y', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'Y', 'N', 'N', 'Y', 'N', 'N', 'N', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'N', '', '', '', '', 0, 0, 0, 0, 'mysql_native_password', '*2707D57595F6CBAD52FA17AD4B08C85FA7185BAC', 'N', '2020-05-01 00:00:00', NULL, 'N', 'Y', 'Y', NULL, NULL, NULL, NULL);
FLUSH PRIVILEGES;
```

**Config location:**
* `/src/main/resources/application.properties` - copy config into /opt/csstats/ 

**Configure config values `/opt/csstats/application.properties` as you want:**
* `stats.datasource.jdbcUrl = jdbc:mysql://127.0.0.1:3306/amx?user=stats&password=stats`
* `stats.session.startOnAction=true`
* `stats.listener.port=8888`
* `server.servlet.context-path=/`
* `server.port=8890`
* `etc...`

**Directory structure overview:**
```
root@user-desktop:~# ls /opt/csstats/
application.properties  cs-stats-collector.jar  logs/
```

**Unix launch:**
```
/usr/bin/java -jar /opt/csstats/cs-stats-collector.jar --spring.config.location=/opt/csstats/application.properties >> /opt/csstats/logs/collector.log 2>&1
or
/usr/bin/java -jar /opt/csstats/cs-stats-collector.jar --spring.config.location=/opt/csstats/application.properties --logging.file=/opt/csstats/logs/collector.log
```
**Windows launch:**
```
java -jar C:\csstats\cs-stats-collector.jar --spring.config.location=C:\csstats\application.properties --logging.file=C:\csstats\logs\collector.log
```
**Launch via start.bat file for Windows (example):**
* Create **start.bat** with payload:
```
@ECHO OFF
START "cs-stats-collector" "C:\Program Files\Java\jre1.8.0_231\bin\java.exe" -jar C:\csstats\cs-stats-collector.jar --spring.config.location=C:\csstats\application.properties --logging.file=C:\csstats\logs\collector.log
exit
```

**Stop:**
* **Ctrl+C** or **kill [process pid]**

**Management:**
* **Curl** or **Postman plugin for Chrome** or **RESTED plugin for FireFox**
```
GET http://localhost:8890/stats - prints in json format current players statistics & servers settings
POST http://localhost:8890/stats/flush - manually merge all players statistics to database
POST http://localhost:8890/stats/updateSettings - reload & apply servers settings from database, without restart JVM.
```
---
**Example screenshots:**
* Logs:
![Screenshot_1](https://user-images.githubusercontent.com/8545291/81408357-c27d8b80-9145-11ea-9631-8be1044f42b7.png)
---
* Example result from `/stats` endpoint _(RESTED plugin for FireFox)_:
![Screenshot_3](https://user-images.githubusercontent.com/8545291/81405183-d6be8a00-913f-11ea-93ca-07b2ea5a8d05.png)
---
* Example result from `/stats/flush` endpoint _(RESTED plugin for FireFox)_:
![Screenshot_5](https://user-images.githubusercontent.com/8545291/81405185-d6be8a00-913f-11ea-8844-af75aab6c840.png)
---
* Example SQL query in MySQL Workbench:
![Screenshot_7](https://user-images.githubusercontent.com/8545291/81405670-ca86fc80-9140-11ea-9136-4ac0ab1f8b58.png)
---
#### **Compile:**
#### **Requirements:**
* `Gradle 5.4+`
```
gradle assemble
```
---
#### **Downloads:**
https://github.com/mbto/cs-stats-collector/releases

#### **Questions:**
https://github.com/mbto/cs-stats-collector/wiki/Questions
