/**
 * 
 */
package com.andune.minecraft.hsp.strategy;

import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.strategy.StrategyResult;

/** Factory for creating strategy results. The factory pattern is
 * used to allow simplicity in testing, since the Factory object
 * can easily be mocked to validate results.
 * 
 * @author morganm
 *
 */
public interface StrategyResultFactory {
    public StrategyResultImpl create(Home home);
    public StrategyResult create(Spawn spawn);
    public StrategyResult create(Location location);
    public StrategyResult create(boolean isSuccess, boolean explicitDefault);
}
