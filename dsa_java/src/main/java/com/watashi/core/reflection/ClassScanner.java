package com.watashi.core.reflection;

public class ClassScanner {
    
    private ClassLoader classLoader;

    public void run() {
        if(this.classLoader == null) {    
            this.classLoader = Thread.currentThread()
                .getContextClassLoader();
        }
    }

    Class<?>[] scanPkg(String pkg) {
        return new Class<?>[0];
    }

}
