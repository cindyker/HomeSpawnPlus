These notes are intended to document the architecture of HomeSpawnPlus, both for my own future
reference and also for the reference of anyone else looking to contribute to the project
in the future.

History

A brief history is relevant to why the application is designed as it is.

This was my first Bukkit plugin. I didn't want to write a plugin, I figured it was another
timesink I didn't need. However, after searching, I was unable to find a home/spawn plugin
that worked like I wanted. So I picked the closest plugin I could find to my needs, the
venerable SpawnControl, and figured I'd just tweak it a bit and call it a day.

A year and a half later HomeSpawnPlus has evolved far beyond it's humble beginnings as a
minor fork. Along the way I've cut my teeth on Bukkit plugins, which has meant experiments
here or there when I wanted something done a certain way and Bukkit didn't offer what I
wanted.

Today, the experimentation continues. HomeSpawnPlus incorporates a few modern development
concepts that often don't make their way into most plugin development:

* ORM and DAO pattern
* Bukkit abstraction
* IoC
* Test framework
* Internationalization (i18n)
* Dynamic Commands
* SLF4J logging
* Maven

Each of these is a design feature of HSP that aids in simpler maintenance and support of
a fairly complex plugin.

## ORM and DAO pattern

HSP leverages Object Relational Mapping (ORM) concepts to avoid hard-coding SQL to store
data to a backing database. In addition, HSP uses the DAO (data access object) pattern to
abstract the data back-end such that new back-ends can be added without having to change
any other code - in this way a YAML back-end was added to HSP.

## Bukkit abstraction

Most Bukkit plugins are heavily tied to the Bukkit API. This seems to make sense since
they are "Bukkit plugins" after all, but this has a few major downsides.

* Small changes in Bukkit can break your plugin or require massive recoding on your part.
For example, Bukkit changed it's configuration API. While the new API was "semi-compatible",
any plugins which dependend heavily on the previous config API had a large refactor and
testing effort ahead of them. 
* It's difficult to write tests. The Bukkit API weaves a complex web of dependencies,
many of them with unique challenges to test frameworks (such as being final classes). This
makes it very hard to write simple unit tests for your plugins.
* Bukkit isn't the only game in town. Eventually it will be replaced by MC-API, which may
or may not be backward-compatible. There's also Spout and several other implementations
available. Tightly binding to Bukkit limits your plugin and guarantees a future refactor
when Bukkit is superceded by MC-API.

A simple abstraction over Bukkit nullfies all of these problems. WorldEdit paved the way
with this thinking, and as a result, as Bukkit has evolved and changed over the years,
sometimes breaking lots (or in some cases, all) plugins out there, WorldEdit has been
able to adapt quickly, often having new versions ready within *minutes* of Bukkit
publishing breaking API changes. That's a well-designed plugin.

== IoC

Inversion of Control is a design pattern which promotes good design and facilitates easy
test writing by minimizing hard-wired dependencies between components. With IoC, and
particularly a good container, it becomes very easy to add dependencies where you need
them and easily "wire" them in, all without having uber objects that become core
dependencies throughout your entire plugin. This leads to more maintanable code and
also facilitates good unit tests.

HSP leverages Google's Guice IoC container, because of it's relatively small footprint
and straightforward design, particularly it's natural binding to JSR-330 javax.inject
annotations.

== Test Framework

With a complex plugin that offers so much flexibility to end users, it becomes very hard
to keep track of all the use cases and test for them with each release, so regressions
become common as the code base grows and becomes harder and harder to test.

Unit tests help alleviate this contraint by providing consistent, automatic regression
tests and a developer can move forward with confidence adding or making changes, knowing
that if s/he accidentally breaks something, tests will fail. And if they don't but the
code breaks anyway, it's easy to add another test to be sure that regression doesn't
happen again.

HSP uses Junit and PowerMock (based on Mockito) to write test cases. HSP further tries
to follow good design practices of OOP (encapsulation and abstraction) and modular
design in order to have small classes with minimal dependencies to facilitate simple
addition of test cases, encouraging increased test coverage.

== Internationalization (i18n)

Not everybody speaks English. It's nice for a plugin to offer the ability for other
langauges to be used in different locales. Further this facility can be used to allow
for customization of any message by the end user.

== Dynamic Commands

The lack of any useful API for manipulating commands (beyond the static plugin.yml)
is a major shortcoming of the Bukkit API. With a dynamic command system, it becomes
possible to allow admins to fully customize commands to their preference; turning
off commands in plugins to avoid conflicts or possibly even creating entirely new
commands. HSP implements a dynamic command system to allow exactly these behaviors.

== SLF4J Logging

Bukkit uses JDK14 logging, whose only real advantage is that it comes built into
the JDK. Minecraft, being 99% single-threaded, is heavily dependent upon maximizing
performance of all code since every single line of code that is not made async is
blocking performance of MC servers, which is the limiting factor in maximum player
count for large servers.

SLF4J offers performance improvement up to 50 times more than what comes built into
JDK14. Further, SLF4J offers a fantastic plugin system so that admins can decide
what logging facility they want to use at runtime (such as log4j or logback). It
would be great if Bukkit used this mature logging framework instead, perhaps MC-API
will change this. In the meantime, HSP chooses to bind itself to a highly
performant future-proof logging framework rather than a slow performing one simply
for convenience.

== Maven

When a plugin has more than a one or two dependencies, it becomes a pain for new devs
to join the effort because the first thing they have to do is spend a few hours tracking
down all the dependencies. Maven solves this problem by recording the dependencies
declartively in the project pom.xml. Thus it becomes a very simple task for new devs
to join the project and execute the build process to produce the final JAR file the
same way I do.

= Structure

== Program Flow

The basic flow of of HSP can be divided into two phases. The startup phase and the
event processing phase.

=== Startup Phase

In the startup phase, HSP initializes itself and gets ready to process events. This
process primarily involves these modules:

* IoC container
* Event hooks
* Configuration setup

=== Event Processing

At runtime, HSP processes events from the underlying server (such as Bukkit) and
responds to those events. This primarily involves these modules:

* Event processor
* Command system
* Runtime managers (warmups, cooldowns, etc)
* Strategy Engine
* Storage system
* Plugin integrations (WorldGuard, Multiverse, Dynmap, etc)

== HSP Modules

=== Event Processor

HSP processes incoming events to provide it's event hook capabilities that allows admins
to define what happens on various events, such as a player joining the server or a player
dying and respawning.

The event system is in the package org.morganm.homespawnplus.listener.

Primarily the event system gathers meta information about the event and then runs the
event through the Strategy Engine to determine what to do and then take action on it.
The primary action is setting the target location for the player for that event.

== Strategy Engine

This is the heart of the HSP strategy system. It takes in a context and processes an
event rule chain. The strategy engine generically implements the per-permisson,
per-world and default strategy logic for all event types. The strategy engine returns
a StrategyResult object which can be used to determine what action is supposed to be
taken based on the strategy processing that occured.

The strategy system is located in the package org.morganm.homespawnplus.strategy and
the actual stratgies are defined in org.morganm.homespawnplus.strategies.
