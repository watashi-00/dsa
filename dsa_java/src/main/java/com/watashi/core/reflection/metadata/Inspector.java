package com.watashi.core.reflection.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking classes that provide inspection functionality.
 * Currently a placeholder annotation with no fields.
 * 
 * @author watashi00
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface Inspector {

}
