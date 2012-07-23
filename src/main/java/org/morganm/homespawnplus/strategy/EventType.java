package org.morganm.homespawnplus.strategy;

/** The HomeSpawnPlus event types. Strategy chains are defined and
 * based on these event types.
 * 
 * @author morganm
 *
 */
public enum EventType {
	ON_JOIN("onJoin"),
	ON_DEATH("onDeath"),
	HOME_COMMAND("onHomeCommand"),
	NAMED_HOME_COMMAND("onNamedHomeCommand"),
	NAMED_SPAWN_COMMAND("onNamedSpawnCommand"),
	SPAWN_COMMAND("onSpawnCommand"),
	GROUPSPAWN_COMMAND("onGroupSpawnCommand"),
	CROSS_WORLD_TELEPORT("crossWorldTeleport"),
	MULTIVERSE_TELEPORT_CROSSWORLD("multiverseCrossWorldTeleport"),
	MULTIVERSE_TELEPORT("multiverseTeleport");
	
	private String configOption;
	EventType(String configOption) {
		this.configOption = configOption;
	}
	public String getConfigOption() { return configOption; }
	public String toString() { return getConfigOption(); }
}
