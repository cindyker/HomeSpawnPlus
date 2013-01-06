/**
 * 
 */
package org.morganm.homespawnplus.server.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation for abstracted server events.
 * 
 * @author morganm
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventMethod {
    EventPriority priority() default EventPriority.NORMAL;

    boolean ignoreCancelled() default false;
}
