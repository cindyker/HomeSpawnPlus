/**
 * 
 */
package org.morganm.homespawnplus.strategy;

import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.server.api.Location;

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
