/**
 * 
 */
package com.andune.minecraft.hsp.commands.uber;

import javax.inject.Inject;

import org.reflections.Reflections;

import com.andune.minecraft.hsp.server.api.Factory;

/**
 * @author andune
 *
 */
public class UberHome extends BaseUberCommand {
    @Inject
    public UberHome(Factory factory, Reflections reflections) {
        super(factory, reflections, "home");
    }
    
    @Override
    public String[] getCommandAliases() {
        return new String[] {"uh"};
    }
}
