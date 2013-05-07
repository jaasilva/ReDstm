package org.deuce;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to mark a method as unsafe, in this case a transaction context will be
 * suspended until this method returns.
 * 
 * @author Guy Korland
 * @since 1.4
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Unsafe
{
}
