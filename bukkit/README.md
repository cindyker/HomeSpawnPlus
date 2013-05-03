PURPOSE
=======
This readme is intended as a quick primer to someone familiar with Bukkit development who wants to understand and/or contribute to HSP development. HSP v2.0 differs from most Bukkit plugins in two important ways:

1. It uses IoC (Inversion of Control) design principles, which leads to easier code to maintain and test and most critically is also a key enabler for item #2 below.

2. It uses a server abstraction layer so that 90% of the code is "Bukkit free" and Bukkit-specific code is just a very thin server implementation layer. This makes it easy to build versions of HSP for different MineCraft API's (such as Spout or MC-API) from mostly the same code base, with no changes necessary.

PROGRAM FLOW
============
Though you don't have to know how the program flows in order to contribute, it can be helpful just to have some familiarity with the environment. So if you're familiar with Bukkit, you know the normal program flow is that onEnable() gets called and then you setup your event listeners which are later called as Bukkit events happen and you also have a command API to respond to player commands.

HSP has the same goals, but of course accomplishes them slightly differently, using both IoC and a server abstraction layer. So what's the flow look like for HSP running in Bukkit?


onEnable
--------
HSP has a maven Bukkit module which specifically implements a thin layer to allow HSP to run on Bukkit. This includes an onEnable() method which you can find in [HSPBukkit.java](https://github.com/andune/HomeSpawnPlus/blob/Modules/bukkit/src/main/java/com/andune/minecraft/hsp/server/bukkit/HSPBukkit.java#L58). This does some basic setup and then passes the program flow off to the core [HomeSpawnPlus.java](https://github.com/andune/HomeSpawnPlus/blob/Modules/core/src/main/java/com/andune/minecraft/hsp/HomeSpawnPlus.java#L66). The core code is Bukkit-free and does the usual initialization and event registration.

events
------
So how are events invoked through this Bukkit abstraction layer? HSP core has a single [EventListener](https://github.com/andune/HomeSpawnPlus/blob/Modules/core/src/main/java/com/andune/minecraft/hsp/EventListener.java#L70) object that handles events passed to it by Bukkit.

For the Bukkit piece, while a generic abstract eventing system would be more flexible, because HSP's events are well-known (a small subset of what Bukkit offers) and because I didn't want to write/maintain/debug my own event system, the implementation is very simple and direct, which has the final benefit of being very efficient as well.

You can find it in [BukkitEventDispatcher](https://github.com/andune/HomeSpawnPlus/blob/Modules/bukkit/src/main/java/com/andune/minecraft/hsp/server/bukkit/BukkitEventDispatcher.java#L62). This should look pretty normal to any Bukkit developer, with perhaps the exception of the config-defined event priorities, whereby event priorities are determined run-time by config options instead of design-time by annotations. As you can see, the events are a very thin implementation that simply wrap the events into non-Bukkit objects and then pass it off to the core HSP event listener.

Inversion of Control
====================
I won't attempt to describe all of the core concepts of (IoC)[http://en.wikipedia.org/wiki/Inversion_of_control] and (Dependency Injection)[http://en.wikipedia.org/wiki/Dependency_injection] here, you can check out those links and any other excellent documentation available on the web for these topics.

However, I will describe briefly the primary benefits of these concepts as implemented by HSP as well as the specifics of the Guice IoC container that HSP uses.

To do that, I am going to use a relatively simple example, HSP's [SetHome](https://github.com/andune/HomeSpawnPlus/blob/Modules/core/src/main/java/com/andune/minecraft/hsp/commands/SetSpawn.java) command. Looking at this command, we can see it needs two dependencies satisfied (ignoring inherited objects for the moment), as declared by @Inject members: a config object and a spawnUtil object.

Without IoC, most commonly you'd see a reference to a Bukkit plugin object being passed all around the entire codebase, meaning every class is inextricably tied to that plugin object. This is not only bad design (a change to your plugin object could affect your entire codebase), but also now makes testing very difficult: in order to test, you have to load your plugin object, in order to load your plugin object, you have to load/stub large components of Bukkit. In this case with IoC, testing the SetHome command can easily be stubbed out without having to fight with trying not to stub the entire Bukkit library in the process. By comparison, here is the relatively simple test class for SetSpawn: [TestSetSpawn](https://github.com/andune/HomeSpawnPlus/blob/Modules/core/src/test/java/com/andune/minecraft/hsp/commands/TestSetSpawn.java).

The IoC container (Guice) automatically injects our config and spawnUtil dependencies when HSP starts up, so the SetSpawn command has them ready to go. Further, because these dependencies are fairly narrow in scope, it's very easy to mock them up and inject mock objects for testing. This also means if we later decide that we need a new dependency (perhaps another config object), we don't have to go add methods to an ever-growing uber plugin object to get it, we simply add an @Inject variable and IoC takes care of the rest.

While Guice can automatically determine dependencies for most objects, in some cases where there are interfaces and implementations, you will write a Guice module that tells Guice what objects to inject when an interface is requested. You can see this for HSP core in [HSPModule](https://github.com/andune/HomeSpawnPlus/blob/Modules/core/src/main/java/com/andune/minecraft/hsp/guice/HSPModule.java). There's also the Bukkit-specific [BukkitModule](https://github.com/andune/HomeSpawnPlus/blob/Modules/bukkit/src/main/java/com/andune/minecraft/hsp/guice/BukkitModule.java), which binds Bukkit-specific implementations to their generic interfaces. When writing a new server implementation (for MC-API, for example), you would simply write an McApiModule that binds to MC-API objects instead and none of the core HSP code would have to change at all, Guice would just inject MC-API objects instead of Bukkit ones.

Server abstraction
==================
Looking at the same [SetHome](https://github.com/andune/HomeSpawnPlus/blob/Modules/core/src/main/java/com/andune/minecraft/hsp/commands/SetSpawn.java) command, we can also see server abstraction at play. For example, execute() accepts a Player argument and Player has a sendMessage() method. While this (intentionally) looks like the Bukkit API, it is in fact an interface object: [Player](https://github.com/andune/HomeSpawnPlus/blob/Modules/api/src/main/java/com/andune/minecraft/hsp/server/api/Player.java). The sendMessage() method is inherited through [CommandSender](https://github.com/andune/HomeSpawnPlus/blob/Modules/api/src/main/java/com/andune/minecraft/hsp/server/api/CommandSender.java).

We can see our Bukkit implementation layer implements this as a simple pass-through call to the actual Bukkit CommandSender: [BukkitCommandSender](https://github.com/andune/HomeSpawnPlus/blob/Modules/bukkit/src/main/java/com/andune/minecraft/hsp/server/bukkit/BukkitCommandSender.java#L46).

But, lets say that MC-API comes along and decides to use writeMessage() instead of sendMessage() for the same functionality. No big deal, the MC-API server implementation for HSP will just call writeMessage() instead and HSP's setHome execute() doesn't have to change at all.
