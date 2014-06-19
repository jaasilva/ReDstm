package org.deuce.distribution.replication.partial;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@Target(FIELD)
@Retention(CLASS)
@ExcludeTM
public @interface Partial
{
}
