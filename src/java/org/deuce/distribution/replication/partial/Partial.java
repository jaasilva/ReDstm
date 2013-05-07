package org.deuce.distribution.replication.partial;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
@ExcludeTM
public @interface Partial
{
}
