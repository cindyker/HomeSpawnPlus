While I considered lots of options for HSP's UUID implementation, I ultimately chose what I considered to be the simplest implementation with minimal disruption to the code base and existing installations.

This amounts to not changing the majority of entity objects and therefore not changing all the code that uses them or the database objects behind them. We still use player_name as the unique key for HSP entities. But if player names can change, how do we make sure a players homes and data don't get lost when they change their name?

Simple, we track the UUID->player relationship. Every time a player logs in, we compare their UUID to their known name. If it's changed, we modify their data to their new name. 

The end result is that if admins crack open their .yml data file or their SQL database, they can work with familiar player names instead of ugly UUIDs. Additionally, HSP code doesn't have to change all over the place to do name->UUID lookups. All the commands that use usernames don't require any changes at all.