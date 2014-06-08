package org.deuce;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.deuce.distribution.Defaults;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalMetadata
{
	String metadataClass() default Defaults.LOCAL_METADATA;
}
