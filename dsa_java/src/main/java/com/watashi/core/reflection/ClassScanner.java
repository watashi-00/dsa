package com.watashi.core.reflection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.watashi.Main;

public class ClassScanner {
    private static Logger logger;
    private final Set<URL> urls = new LinkedHashSet<>();

    static {
        if(Main.debug && logger == null) {
            logger = Logger.getLogger(ClassScanner.class.getName());
        }
    }

    public void findClass(String pkg) {
        scan(new File(pkg), true);
        loadClasses();
    }

    public void findClass(String pkg, String baseDir) {
        addURL(new File(baseDir != null ? baseDir : pkg));
        loadClasses();
    }

    private void findClass(URLClassLoader loader) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findClass'");
    }

    private void addURL(File file) {
        try {
            urls.add(file.toURI().toURL());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    private void scan(File dir, boolean addDirectory) {
        if (!dir.isDirectory()) {
            return;
        }

        if (addDirectory) {
            addURL(dir);
        }

        File[] files = dir.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scan(file, false);
            } else if (file.getName().endsWith(".jar")) {
                addURL(file);
            }
        }
    }

    private void loadClasses() {
        URLClassLoader loader = new URLClassLoader(
            urls.toArray(new URL[0]),
            getClass().getClassLoader()
        );

        findClass(loader);
    }
}
