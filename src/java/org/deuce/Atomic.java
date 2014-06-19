package org.deuce;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.deuce.distribution.Defaults;

/**
 * @author Guy Korland
 * @since 1.0
 */
@Target(METHOD)
@Retention(CLASS)
public @interface Atomic
{
	int retries() default Defaults.MAX_RETRIES;

	String metainf() default Defaults.METAINF;
}
