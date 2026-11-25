package com.watashi.core.reflection.inspectors;

/**
 * Interface for class inspection functionality.
 * Defines the contract for analyzing and validating metadata from classes.
 * 
 * @author watashi00
 */
public interface Inspector {
    /**
     * Inspects a class for relevant annotations and properties.
     * 
     * @param clazz the class to inspect
     */
    void inspect(Class<?> clazz);
}
