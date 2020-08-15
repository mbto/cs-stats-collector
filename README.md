#### **Features:**
* `Consuming game logs from counter-strike 1.6 dedicated-servers at UDP port 8888;`
* `Collecting & caching players statistics (kills, deaths, online at server in seconds) by player name;`
* `Merging players statistics to MySQL database on 'next map', 'shutdown server' events, or manually;`
* `Provides REST-api for management;`

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
