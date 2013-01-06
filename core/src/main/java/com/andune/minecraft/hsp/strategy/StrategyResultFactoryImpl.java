/**
 * 
 */
package com.andune.minecraft.hsp.strategy;

import javax.inject.Singleton;

import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.strategy.StrategyResult;

/**
 * @author morganm
 *
 */
@Singleton
public class StrategyResultFactoryImpl implements StrategyResultFactory {
    @Override
    public StrategyResultImpl create(Home home) {
        return new StrategyResultImpl(home);
    }

    @Override
    public StrategyResult create(Spawn spawn) {
        return new StrategyResultImpl(spawn);
    }

    @Override
    public StrategyResult create(Location location) {
        return new StrategyResultImpl(location);
    }

    @Override
    public StrategyResult create(boolean isSuccess, boolean explicitDefault) {
        return new StrategyResultImpl(isSuccess, explicitDefault);
    }
}
