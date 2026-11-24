package com.watashi.core.reflection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

import com.watashi.Main;

public class ClassScanner {
    private static Logger logger;

    static {
        if(Main.debug && logger == null) {
            logger = Logger.getLogger(ClassScanner.class.getName());
        }
    }

    public void findClass(String pkg) {
        File dir = new File(pkg);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                findClass(file.getAbsolutePath());
            } else if (file.getName().endsWith(".jar")) {
                findClass(file.getAbsolutePath(), null);
            } else if (file.getName().endsWith(".class")) {
                findClass(file.getAbsolutePath(), dir.getAbsolutePath());
            }
        }
    }

    public void findClass(String pkg, String baseDir) {
        try {
            File file = new File(pkg);

            URL[] urls = new URL[] {
                    file.toURI().toURL()
            };

            URLClassLoader loader = new URLClassLoader(urls);

            findClass(loader);
            
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    private void findClass(URLClassLoader loader) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findClass'");
    }

}
