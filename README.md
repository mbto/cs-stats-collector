#### **Features:**
* `Consuming game logs from HLDS (CS 1.6) servers at UDP port 8888;`
* `Collecting & caching players statistics (kills, deaths, online at server, IPs, Steam IDs) by player name;`
* `Merging players statistics to MySQL on 'next map', 'shutdown server' events, or manually;`
* `Automatic calculation players activity and assignment one of 56 ENG/RUS ranks;`
* `Provides SQL-routines and REST api for view players statistics & module management;`

#### **Requirements:**
* `Java 8+`
* `MySQL server 8.0.21`

#### **FAQ:**
* https://github.com/mbto/cs-stats-collector/wiki/Questions

#### **Downloads:**
* https://github.com/mbto/cs-stats-collector/releases

#### **Examples:**
* https://github.com/mbto/cs-stats-collector/wiki/Examples

#### **Install & launch:**
* https://github.com/mbto/cs-stats-collector/wiki/Install-&-launch

#### **Compile & Build:**
* **Requirements:**
    * `Gradle 5.4+`
* **With tests:**
    * `gradle build`
* **Without tests:**
    * `gradle build -x test`
