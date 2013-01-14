package org.deuce.transform;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Instrumented class won't suffer transactional instrumentation, but arrays
 * will still be modified.
 * @author Tiago Vale
 */
@Target(TYPE)
@Retention(CLASS)
public @interface Exclude {

}
