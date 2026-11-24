package com.watashi;

import com.watashi.core.reflection.ClassScanner;

public class Main {
    public static boolean debug = true;
    public static void main(String[] args) {
        ClassScanner scn = new ClassScanner();
        scn.findClass(Main.class);
    }
}