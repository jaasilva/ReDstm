package org.deuce;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to mark a method as Irrevocable, in this case a transaction context will
 * be informed on accessing an Irrevocable method.
 * 
 * @author Guy Korland
 * @since 2.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Irrevocable
{
}
