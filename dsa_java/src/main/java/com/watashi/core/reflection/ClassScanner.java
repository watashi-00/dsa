package com.watashi.core.reflection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
        for (URL url : loader.getURLs()) {
            try { 
                File file = new File(url.toURI());

                if (file.isDirectory()) {
                    try (var paths = Files.walk(file.toPath())) {
                        paths.filter(path -> path.toString().endsWith(".class"))
                            .forEach(path -> {
                                String className = file.toPath()
                                    .relativize(path)
                                    .toString()
                                    .replace(File.separator, ".");
                                
                                className = className.substring(
                                    0, className.length() - 6
                                );

                                loadClass(loader, className);
                            });
                    }
                } else if (file.getName().endsWith(".jar")) {
                    try (JarFile jar = new JarFile(file)) {
                        Enumeration<JarEntry> entries = jar.entries();

                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();

                            if (!entry.isDirectory() 
                                && entry.getName().endsWith(".class")) {
                                
                                String className = entry.getName()
                                    .substring(0, entry.getName().length() - 6)
                                    .replace("/", ".");

                                loadClass(loader, className);
                            }

                        }

                    }
                }

            } catch (Exception ex) {
                if (logger != null) {
                    logger.warning("Failed to scan " + url + ": " + ex.getMessage());
                }
            }
        }
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

    private void loadClass(URLClassLoader loader, String className) {
        try {
            loader.loadClass(className);
        } catch (ClassNotFoundException | LinkageError ex) {
            if (logger != null) {
                logger.warning("Failed to load " + className + ": " + ex.getMessage());
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
