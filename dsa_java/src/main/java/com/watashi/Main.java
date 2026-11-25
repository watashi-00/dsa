package com.watashi;

import com.watashi.core.reflection.ClassScanner;

/**
 * Main application entry point.
 * 
 * @author watashi00
 */
public class Main {
    /**
     * Application main method.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        ClassScanner scn = new ClassScanner();
        scn.findClass(Main.class);
    }
}