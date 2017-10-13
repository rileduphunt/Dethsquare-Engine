package com.ezardlabs.dethsquare.networking.markers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field for networking. Note that the field must be public and must not be static, and that the enclosing
 * class must implement {@link Networked}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NetVar {
}
