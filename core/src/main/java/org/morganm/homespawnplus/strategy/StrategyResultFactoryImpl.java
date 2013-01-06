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
