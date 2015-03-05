HomeSpawnPlus is a home/spawn management tool for Bukkit/Minecraft.

Please read http://dev.bukkit.org/server-mods/homespawnplus for detailed description and support.

Latest official dev build is available on [Jenkins](http://andune.com/jenkins/job/HomeSpawnPlus/)

A big thank you to YourKit for their amazing support of open source projects. Their performance and memory tracking tool has been used during the development and testing of HomeSpawnPlus to keep HSP tuned and performing well. You can visit their website at http://www.yourkit.com

---
Building
---
Building a release version of HSP 2.0 is very simple, all dependencies are available via maven:

```
  git clone https://github.com/andune/HomeSpawnPlus
  cd HomeSpawnPlus
  git checkout 2.0-beta2
  mvn package
````

Building the latest dev build requires a little more work since you must build the SNAPSHOT dependencies yourself:

```
  git clone https://github.com/andune/anduneCommonLib
  cd anduneCommonLib
  mvn install

  git clone https://github.com/andune/anduneCommonBukkitLib
  cd anduneCommonBukkitLib
  mvn install

  git clone https://github.com/andune/HomeSpawnPlus
  cd HomeSpawnPlus
  mvn package
```

In both cases, the resulting JAR for Bukkit can be found in bukkit/target/HomeSpawnPlus.jar
