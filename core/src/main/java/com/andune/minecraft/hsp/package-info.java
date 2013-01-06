/**
 * High level map of HSP package structure:
 * {hsp} is used as an alias for org.morganm.homespawnplus
 * 
 * {hsp} - This is the top-level package with the main plugin class and a few
 * other top-level classes like the initializer and permissions.
 * 
 * {hsp}.command - This is where the support classes for commands are
 * located, such as the command interface and abstract base command class
 * 
 * {hsp}.commands - This package contains all of HSP's commands, one per
 * class
 * 
 * {hsp}.config - Configuration classes are located here
 * 
 * {hsp}.convert - HSP's converters from other plugins are in this package
 * 
 * {hsp}.entity - This package contains the entity objects, part of the
 * ORM (object-relational mapping) pattern HSP uses. These entities contain
 * long-term data HSP keeps track of and stores in the database.
 * 
 * {hsp}.guice - This package contains the Guice modules that are used by
 * the Guice injector for IoC (Inversion of Control).
 * 
 * {hsp}.integration - This package and subpackages contain HSP's integration
 * and abstraction layer to other plugins
 * 
 * {hsp}.listener - This package contains HSP's event listeners
 * 
 * {hsp}.manager - HSP's manager classes are located here, responsible for
 * managing things such as cooldowns and warmups
 * 
 * {hsp}.server.api - This package and sub-packages are HSP's server
 * abstraction layer. This allows HSP to be written and managed as a server
 * agnostic plugin, easily ported to new server backends (such as MCAPI
 * or Spout).
 * 
 * {hsp}.server.bukkit - This package and sub-packages are the implementation
 * of HSP's server API specific to Bukkit.
 * 
 * {hsp}.storage - This package contains the parent interface and factory
 * of HSP's storage subsystem
 * 
 * {hsp}.storage.dao - HSP's DAO (Data Access Object) interfaces are located
 * here. The DAO's provide a way for the rest of the plugin to access data
 * without caring how it is actually implemented; in a database, in YAML
 * files, or something else.
 * 
 * {hsp}.storage.ebean - EBean implementation of HSP DAOs. The ebean server
 * is capable of storing in many dialects of SQL, although HSP primarily
 * targets and tests with MySQL and Sqlite3.
 * 
 * {hsp}.storage.yaml - A YAML-based DAO implementation for HSP's data.
 * 
 * {hsp}.strategy - This package contains the Strategy Engine, the heart of
 * HSP's flexible strategy system. It also contains the various interfaces
 * and base classes that strategies use.
 * 
 * {hsp}.strategies - This package contains the actual strategies that HSP
 * uses, one strategy per file. This format makes them easy to test and
 * maintain as they are small and purpose-built for the task they perform.
 * 
 * {hsp}.util - Various utility and common routines exist here for use by
 * other classes. While HSP tries to minimize the use of large centralized
 * utility classes as this breaks loose coupling and makes testing much
 * harder, the use of the IoC pattern offsets this concern somewhat and
 * the utilities are kept small and focused on a specific domain so they
 * are easy to test as well.
 */

package com.andune.minecraft.hsp;