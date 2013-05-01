package org.deuce.distribution.replication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.deuce.transform.ExcludeTM;

// This means that the annotation can only be applied to class fields.
@Target(ElementType.FIELD)
// This means that the annotation is retained in .class file, but not
// necessarily by the JVM runtime.
@Retention(RetentionPolicy.CLASS)
@ExcludeTM
public @interface Bootstrap
{
	int id();
}
