====
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Copyright (c) 2013 Andune (andune.alleria@gmail.com)
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:
    1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer
    in the documentation and/or other materials provided with the
    distribution.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
====

HomeSpawnPlus is a home/spawn management tool for Bukkit/Minecraft.
Please read http://dev.bukkit.org/server-mods/homespawnplus for detailed description and support.

== Distribution
HomeSpawnPlus is distributed in one of 3 formats:

=== HomeSpawnPlus.jar
This is just HomeSpawnPlus code, no dependencies. If you use HomeSpawnPlus-with-dependencies
as your first download, you can update HomeSpawnPlus just by downloading and updating your
this JAR file (dependencies don't change often and are noted if they do).

=== HomeSpawnPlus-uber.jar
This is a JAR file that uses a technique in maven known as shading to shade in all
dependencies, so that it is in one single JAR file. While having the advantage of just one
file to manage, the downside is that if you have other plugins with similar dependencies,
you end up with two copies in memory instead of just a single shared dependency.

=== HomeSpawnPlus-with-dependencies.zip
This is the HomeSpawnPlus normal jar bundled with all dependencies required to run it, with
the directory format setup such that you just unzip it into the root of your Bukkit server
and everything ends up in the right place.

== Dependencies
What are these dependencies that HomeSpawnPlus uses and do they impact performance or
memory footprint significantly?

* Guice - This is Google's lightweight IoC framework. The use of IoC results in cleaner,
better code that is easier to manage and test. While perhaps not necessary with tiny
plugins with only a few class files, plugins of any decent size or complexity can
benefit greatly from using IoC best practices to manage their code.

* SLF4J - This is the fatest and most portable Java logging framework available. It's
primary benefit is speed - it is up to 50x faster than comparable logging frameworks,
especially with respect to doing minimal processing for debug statements that aren't
in use.

* mBukkitLib - This is my own small library of common routines I use between multiple
plugin projects.

* Reflections - This is a lightweight reflections API which allows HSP to offer
dynamic command features using a robust, mature reflections framework, that has the
additional benefit of performance since the reflections are precalculated at compile
time instead of runtime.

All other dependencies in the lib directory are transitive dependencies of one of these
four. HSP uses a very small subset of these libraries and so the resulting footprint
in memory is extremely low (~1MB). There is no runtime performance impact and in fact
several of these libraries result in faster runtime code, both in terms of direct
performance as well as a result of better designed code.
