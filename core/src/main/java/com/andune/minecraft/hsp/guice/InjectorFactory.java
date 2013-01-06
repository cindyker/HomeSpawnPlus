/**
 * 
 */
package com.andune.minecraft.hsp.guice;

import com.google.inject.Injector;

/** Class with logic for determining what sort of environment we
 * are running in and returning a dependency injector that will
 * build the appropriate object graph for that environment.
 *  
 * @author morganm
 *
 */
public interface InjectorFactory {
    /**
     * Factory method to create Guice Injector. 
     * 
     * @return
     */
    public Injector createInjector();
}
