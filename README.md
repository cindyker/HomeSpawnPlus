HomeSpawnPlus is a home/spawn management tool for Bukkit/Minecraft.

Please read http://dev.bukkit.org/server-mods/homespawnplus for detailed description and support.

**Update Oct 14, 2016:**

Jenkins server is now offline and a [static web page](http://andune.com/) is now hosting the dev builds for those still using this plugin. MD5 and SHA1 hashes are provided here for authenticity.

HomeSpawnPlus.jar (build 638, built Aug 18, 2016):

MD5: aceb3f50af1bfb432c794003c504c3e7

SHA1: 3573581ed3a1aa4794bbec291e731cb390979c6e


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
