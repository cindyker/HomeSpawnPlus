/**
 * 
 */
package com.andune.minecraft.hsp.strategies.home;

import java.util.Collection;

import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.strategy.HomeStrategy;
import com.andune.minecraft.hsp.strategy.NoArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.strategy.StrategyResultImpl;

/**
 * This strategy is conceptually similar to HomeLocalWorld except that it will
 * also consider associated worlds. For example, if this strategy were run with
 * a player on world "world" that had a home on "world_nether" but not one on
 * "world", this strategy would find the home on their associated home_nether
 * world and send them there.
 * 
 * @author andune
 * 
 */
@NoArgStrategy
public class HomeAssociatedWorld extends HomeStrategy {
    @Override
    public StrategyResult evaluate(StrategyContext context) {
        Home result = null;
        
        World world = context.getEventLocation().getWorld();
        if( world != null ) {
            // first we check the world the strategy is based on.
            result = super.getModeHome(context, world.getName());
            
            // failing that, parent world is next
            if( result == null ) {
                World parent = world.getParentWorld();
                if( parent != null ) {
                    result = super.getModeHome(context, parent.getName());
                }
                
                // failing that, children are next
                if( result == null ) {
                    Collection<World> children = world.getChildWorlds();
                    for(World child : children) {
                        result = super.getModeHome(context, child.getName());
                        if( result != null )
                            break;
                    }
                }
            }
        }
        
        return new StrategyResultImpl(result);
    }
}
