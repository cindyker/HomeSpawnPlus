package org.morganm.homespawnplus.strategy;

public interface ModeStrategy extends Strategy {

	public abstract StrategyMode getMode();

	public abstract boolean isAdditive();

}