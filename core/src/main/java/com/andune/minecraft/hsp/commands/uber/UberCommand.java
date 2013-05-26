/**
 * 
 */
package com.andune.minecraft.hsp.commands.uber;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author andune
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UberCommand {
    String subCommand();
    String[] aliases() default {};
    String uberCommand();
}
