package org.deuce;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalMetadata
{
	String metadataClass() default "org.deuce.transform.localmetadata.type.TxField";
}
