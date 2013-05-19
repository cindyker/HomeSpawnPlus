/**
 * 
 */
package com.andune.minecraft.hsp.strategies.mode;

import com.andune.minecraft.commonlib.server.api.Effect;
import com.andune.minecraft.hsp.strategy.ModeStrategyImpl;
import com.andune.minecraft.hsp.strategy.OneArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyException;
import com.andune.minecraft.hsp.strategy.StrategyMode;

/**
 * @author andune
 *
 */
@OneArgStrategy
public class ModeEffect extends ModeStrategyImpl {
    private String arg;
    private Effect effect;

    public ModeEffect(String arg) {
        this.arg = arg;
    }

    public Effect getEffect() { return effect; }
    
    @Override
    public void validate() throws StrategyException {
        if( arg == null )
            throw new StrategyException("required argument is null for strategy "+getStrategyConfigName());
        
        for(Effect e : Effect.values()) {
            if( e.getName().equalsIgnoreCase(arg) ) {
                this.effect = e;
                break;
            }
        }
        
        if( this.effect == null )
            throw new StrategyException(getStrategyConfigName()+" argument \""+arg+"\" doesn't match any known effect");
    }

    @Override
    public StrategyMode getMode() {
        return StrategyMode.MODE_EFFECT;
    }

    @Override
    public boolean isAdditive() {
        return true;
    }
}
