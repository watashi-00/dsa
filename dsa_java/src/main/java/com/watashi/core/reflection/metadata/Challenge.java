package com.watashi.core.reflection.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking classes as challenges.
 * Contains metadata about the challenge including name, platform, difficulty, and topics.
 * 
 * @author watashi00
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface Challenge {
    /** Name of the challenge. */
    String name();

    /** Platform where the challenge is available. Defaults to "CUSTOM". */
    String platform() default "CUSTOM";

    /** Difficulty level of the challenge. Defaults to "UNSPECIFIED". */
    String difficulty() default "UNSPECIFIED";

    /** Array of topics related to the challenge. */
    String[] topics() default {};
}
