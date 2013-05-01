package org.deuce.transform;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotated class won't suffer ANY instrumentation.
 * 
 * @author Tiago Vale
 */
@Target(TYPE)
@Retention(CLASS)
public @interface ExcludeTM
{

}
