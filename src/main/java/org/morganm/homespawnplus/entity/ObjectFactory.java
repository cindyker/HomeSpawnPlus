/**
 * 
 */
package org.morganm.homespawnplus.entity;

import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Location;

/** While IoC injection should be preferred over static Factory
 * methods (better decoupled code), I'm not using a heavy IoC tool
 * like Spring which would automatically inject my ORM entities.
 * 
 * So either ORM entities that create objects need to be injected
 * (a pain to implement generically, which is why frameworks like
 * Spring are so popular and work well), or I have a static factory
 * class that they can use for limited use case object creation.
 * 
 * Note the factory methods are package-private so this cannot
 * be used outside of the entities that use this pattern.
 * 
 * @author morganm
 *
 */
public class ObjectFactory {
    private static Factory factory;
    
    /**
     * To be invoked during initialization to setup a reference to
     * the Factory object to be used to actually create objects.
     * 
     * @param factory
     */
    public static void setFactory(Factory factory) {
        ObjectFactory.factory = factory;
    }
    
    /**
     * Factory method for creating a new Location object. Package private method.
     * 
     * @param worldName the name of the world the Location is on
     * @param x the x coordinates
     * @param y the y coordinates
     * @param z the z coordinates
     * @param yaw the yaw (360-degree horizontal view angle)
     * @param pitch the pitch (360-degree verticle view angle)
     * 
     * @return the new Location object
     */
    static Location newLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        return factory.newLocation(worldName, x, y, z, yaw, pitch);
    }
}
