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

/**
 * Class scanner utility for discovering and loading classes from classpath locations.
 * 
 * @author watashi00
 */
public class ClassScanner {
    private static final Logger logger =
        Logger.getLogger(ClassScanner.class.getName());

    /** URLClassLoader instance used for loading discovered classes. */
    private URLClassLoader loader;

    /** Set of URLs representing classpath locations. */
    private final Set<URL> urls = new LinkedHashSet<>();

    /** Set of loaded classes. */
    private final Set<Class<?>> classes = new LinkedHashSet<>();

    static {
        logger.setLevel(Level.INFO);
    }

    /**
     * Finds and loads classes from a package directory.
     * 
     * @param pkg the fully qualified name of the package to scan
     */
    public void findClass(String pkg) {
        logger.info("Starting class discovery from: " + pkg);

        scan(new File(pkg), true);
        loadClasses();
    }

    /**
     * Finds and loads classes from a given location.
     * 
     * @param pkg the fully qualified name of the package
     * @param baseDir optional base directory for scanning; uses pkg if null
     */
    public void findClass(String pkg, String baseDir) {
        String location = baseDir != null ? baseDir : pkg;

        logger.info("Starting class discovery from: " + location);

        addURL(new File(location));
        loadClasses();
    }

    /**
     * Finds and loads classes using the specified main class.
     * Determines classpath locations from the main class's code source.
     * 
     * @param mainClass the main class to analyze
     */
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

    /**
     * Scans a URLClassLoader for classes.
     * 
     * @param loader the URLClassLoader to scan
     */
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

    /**
     * Adds a URL representing a classpath location.
     * 
     * @param file the File object representing the classpath entry
     */
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

    /**
     * Recursively scans a directory for class files and jars.
     * 
     * @param dir the directory to scan
     * @param addDirectory whether to add the directory itself as a URL
     */
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

    /**
     * Loads a single class using the provided URLClassLoader.
     * Skips module-info and package-info classes.
     * 
     * @param loader the URLClassLoader to use for loading
     * @param className fully qualified name of the class to load
     */
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

    /**
     * Initializes the URLClassLoader with registered URLs and discovers classes.
     */
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

    /**
     * Returns the set of all classes that have been discovered and loaded.
     * 
     * @return immutable set of loaded classes
     */
    public Set<Class<?>> getClasses() {
        return Set.copyOf(classes);
    }
}
