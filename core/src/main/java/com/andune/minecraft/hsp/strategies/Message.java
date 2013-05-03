/**
 * 
 */
package com.andune.minecraft.hsp.strategies;

import com.andune.minecraft.hsp.strategy.BaseStrategy;
import com.andune.minecraft.hsp.strategy.OneArgStrategy;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyResult;

/**
 * Strategy primarily intended for testing, just echos the given message to
 * the player when it is fired in the strategy chain. It never returns a
 * result so it will always fall through to the next strategy in the chain.
 * 
 * @author andune
 *
 */
@OneArgStrategy
public class Message extends BaseStrategy {
    private final String message;

    public Message(String message) {
        this.message = message;
    }

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        context.getPlayer().sendMessage(message);
        return null;
    }
}
