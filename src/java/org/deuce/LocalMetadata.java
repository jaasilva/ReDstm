package org.deuce;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.deuce.distribution.Defaults;

@Target(TYPE)
@Retention(RUNTIME)
public @interface LocalMetadata
{
	String metadataClass() default Defaults._LOCAL_METADATA;
}
