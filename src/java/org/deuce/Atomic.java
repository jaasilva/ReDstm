package org.deuce;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Guy Korland
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Atomic
{
	int retries() default Defaults.MAX_RETRIES;

	String metainf() default Defaults.METAINF;
}
