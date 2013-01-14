package org.deuce;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalMetadata {
	String metadataClass() default "org.deuce.transform.localmetadata.type.TxField";
}
