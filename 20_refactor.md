= 2.0 refactor notes

== Goals

* Eliminate or break up util class into smaller, more maintainable code. Lots of legacy code and bad design
in the original large util class.
* Utilize IoC container. Good design, facilitate simple addition of test cases.
* Add test cases: test new code with unit tests, increase test coverage from 0% to something more than 0%.
* Abstract Bukkit. Eliminate direct Bukkit dependencies, so that future Bukkit changes affect only a small,
controlled portion of the codebase, and so that future migration to MC-API will likewise mean minimal changes.
* Integrate standard highly efficient SLF4J logger and eliminate custom logging methods
