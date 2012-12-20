/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import javax.inject.Singleton;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.server.api.Location;

/**
 * @author morganm
 *
 */
@Singleton
public class StrategyResultFactoryImpl implements StrategyResultFactory {
    @Override
    public StrategyResult create(Home home) {
        return new StrategyResult(home);
    }

    @Override
    public StrategyResult create(Spawn spawn) {
        return new StrategyResult(spawn);
    }

    @Override
    public StrategyResult create(Location location) {
        return new StrategyResult(location);
    }

    @Override
    public StrategyResult create(boolean isSuccess, boolean explicitDefault) {
        return new StrategyResult(isSuccess, explicitDefault);
    }
}
