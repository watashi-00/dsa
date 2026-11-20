package com.watashi.core.reflection.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface Challenge  {
    String   name();
    String   platform()   default "CUSTOM";
    String   difficulty() default "UNSPECIFIED";
    String[] topics()     default {};
}
