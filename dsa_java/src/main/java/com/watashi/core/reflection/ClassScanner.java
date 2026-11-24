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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassScanner {
    private static final Logger logger =
        Logger.getLogger(ClassScanner.class.getName());

    private URLClassLoader loader;

    private final Set<URL> urls = new LinkedHashSet<>();
    private final Set<Class<?>> classes = new LinkedHashSet<>();

    static {
        logger.setLevel(Level.INFO);
    }

    public void findClass(String pkg) {
        logger.info("Starting class discovery from: " + pkg);

        scan(new File(pkg), true);
        loadClasses();
    }

    public void findClass(String pkg, String baseDir) {
        String location = baseDir != null ? baseDir : pkg;

        logger.info("Starting class discovery from: " + location);

        addURL(new File(location));
        loadClasses();
    }

    public void findClass(Class<?> mainClass) {
        logger.info("Starting class discovery using: " + mainClass.getName());

        try {
            var codeSource = mainClass.getProtectionDomain().getCodeSource();

            if (codeSource == null) {
                logger.warning("Unable to determine class source: " + mainClass.getName());
                return;
            }

            File location = new File(codeSource.getLocation().toURI());

            logger.info("Resolved class source: " + location.getAbsolutePath());

            if (location.isDirectory()) {
                scan(location, true);
            } else {
                addURL(location);
            }

            loadClasses();

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to locate class source", ex);
        }
    }

    private void findClass(URLClassLoader loader) {
        logger.info("Scanning " + loader.getURLs().length + " classpath location(s)");

        for (URL url : loader.getURLs()) {
            try {
                File file = new File(url.toURI());

                logger.info("Inspecting: " + file.getAbsolutePath());

                if (file.isDirectory()) {
                    try (var paths = Files.walk(file.toPath())) {
                        paths.filter(path -> path.toString().endsWith(".class"))
                            .forEach(path -> {
                                String className = file.toPath()
                                    .relativize(path)
                                    .toString()
                                    .replace("\\", "/")
                                    .replace("/", ".");

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
                } else {
                    logger.warning("Unsupported classpath entry: " + file);
                }

            } catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to scan: " + url, ex);
            }
        }
    }

    private void addURL(File file) {
        try {
            URL url = file.toURI().toURL();

            if (urls.add(url)) {
                logger.info("Added classpath location: " + url);
            } else {
                logger.fine("Classpath location already registered: " + url);
            }

        } catch (MalformedURLException ex) {
            logger.log(Level.WARNING, "Invalid classpath location: " + file, ex);
        }
    }

    private void scan(File dir, boolean addDirectory) {
        if (!dir.isDirectory()) {
            logger.warning("Directory does not exist or is not accessible: " + dir);
            return;
        }

        if (addDirectory) {
            addURL(dir);
        }

        File[] files = dir.listFiles();

        if (files == null) {
            logger.warning("Unable to list directory contents: " + dir);
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
        if (className.equals("module-info")
                || className.endsWith(".module-info")
                || className.equals("package-info")
                || className.endsWith(".package-info")) {
            return;
        }

        try {
            Class<?> clazz = loader.loadClass(className);

            if (classes.add(clazz)) {
                logger.fine("Loaded class: " + clazz.getName());
            }

        } catch (ClassNotFoundException | LinkageError ex) {
            logger.log(Level.WARNING, "Failed to load class: " + className, ex);
        }
    }

    private void loadClasses() {
        if (urls.isEmpty()) {
            logger.warning("No classpath locations available for scanning.");
            return;
        }

        logger.info("Initializing URLClassLoader...");

        loader = new URLClassLoader(
            urls.toArray(new URL[0]),
            getClass().getClassLoader()
        );

        int previousCount = classes.size();

        findClass(loader);

        int loadedCount = classes.size() - previousCount;

        logger.info("Class discovery completed.");
        logger.info("New classes loaded: " + loadedCount);
        logger.info("Total registered classes: " + classes.size());
    }

    public Set<Class<?>> getClasses() {
        return Set.copyOf(classes);
    }
}
