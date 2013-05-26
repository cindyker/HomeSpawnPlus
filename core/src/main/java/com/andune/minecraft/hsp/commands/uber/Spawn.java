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
public class Spawn extends BaseUberCommand {
    @Inject
    public Spawn(Factory factory, Reflections reflections) {
        super(factory, reflections);
    }
}
