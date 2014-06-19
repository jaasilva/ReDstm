package org.deuce.distribution.replication;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.deuce.transform.ExcludeTM;

@Target(FIELD)
@Retention(CLASS)
@ExcludeTM
public @interface Bootstrap
{
	int id();
}
