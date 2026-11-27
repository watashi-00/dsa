# ClassScanner

`ClassScanner` is a reflection utility responsible for discovering and loading Java classes from filesystem directories and JAR files.

It provides a centralized mechanism for inspecting classpath locations, creating a dedicated `URLClassLoader`, and collecting discovered classes into a registry.

## Overview

The scanner operates through three main stages:

1. **Classpath registration:** identifies and registers directories and JAR files as classpath locations.
2. **Class discovery:** recursively inspects registered locations and identifies `.class` files.
3. **Class loading:** uses `URLClassLoader` to load discovered classes and registers them in an internal collection.

The resulting classes can be retrieved through `getClasses()`.

## Architecture

### Internal components

| Component | Responsibility |
|---|---|
| `URLClassLoader loader` | Loads classes from registered classpath locations. |
| `Set<URL> urls` | Stores registered classpath locations, preventing duplicate URLs. |
| `Set<Class<?>> classes` | Stores discovered and loaded classes, avoiding duplicate class references. |
| `Logger logger` | Reports discovery, loading, and error events. |

The implementation uses `LinkedHashSet` to preserve insertion order during registration and collection.

## Discovery mechanisms

`ClassScanner` exposes three public entry points for class discovery.

### 1. Package directory

```java
scanner.findClass("target/classes");
```

Scans the specified filesystem directory recursively.

The directory is registered as a classpath location, and any JAR files found within its directory hierarchy are also registered.

**Note:** Despite the parameter name `pkg`, this overload expects a filesystem path, not a Java package name.

### 2. Custom base directory

```java
scanner.findClass("com.example", "target/classes");
```

Uses the provided base directory as the classpath location.

If `baseDir` is `null`, the value of `pkg` is used instead.

### 3. Main class inspection

```java
scanner.findClass(Main.class);
```

Uses the main class's `ProtectionDomain` and `CodeSource` to determine its original location.

Depending on the resolved location, the scanner either:

- Recursively scans a directory.
- Registers a JAR file for inspection.

This approach allows discovery to begin from a known class without manually specifying its classpath location.

## Class discovery process

Once classpath locations are registered, the scanner inspects each URL.

### Directory inspection

For directories, `Files.walk()` recursively traverses the filesystem and identifies files ending in `.class`.

The relative filesystem path is converted into a fully qualified class name.

For example:

```text
com/example/service/UserService.class
```

Becomes:

```text
com.example.service.UserService
```

### JAR inspection

For JAR files, `JarFile` enumerates their entries.

Each non-directory entry ending in `.class` is converted into a fully qualified class name and passed to the class loader.

### Class filtering

The scanner explicitly skips:

- `module-info.class`
- `package-info.class`

These entries do not represent regular application classes and are excluded from the loading process.

## Class loading

Discovered class names are passed to `URLClassLoader.loadClass()`.

```java
Class<?> clazz = loader.loadClass(className);
```

Classes that are successfully loaded are registered in the internal `classes` collection.

The implementation handles:

- `ClassNotFoundException`
- `LinkageError`

This prevents an individual class-loading failure from immediately terminating the entire discovery process.

**Important:** `loadClass()` does not necessarily initialize the class. Static initializers are not executed merely because a class has been loaded.

## Retrieving discovered classes

The `getClasses()` method exposes the registered classes:

```java
Set<Class<?>> discovered = scanner.getClasses();
```

The returned collection is immutable through `Set.copyOf()`.

This allows consumers to inspect the discovered classes without directly modifying the scanner's internal registry.

## Logging

The scanner uses `java.util.logging` to report its execution.

| Level | Usage |
|---|---|
| `INFO` | Discovery initialization, registered locations, and completion statistics. |
| `WARNING` | Invalid locations, unsupported entries, and class-loading failures. |
| `FINE` | Duplicate registrations and individual class-loading events. |

The final report includes the number of newly loaded classes and the total number of registered classes.

## Implementation considerations

A few implementation details are worth considering when extending this utility:

- **Class identity:** Classes loaded by different class loaders are distinct JVM types, even when they share the same fully qualified name.
- **Class initialization:** Loading a class does not automatically execute its static initializer.
- **Nested classes:** The scanner does not currently exclude anonymous, synthetic, or nested classes.
- **Class loader lifecycle:** The created `URLClassLoader` is not explicitly closed.
- **Classpath boundaries:** The directory registered as the classpath root must preserve the expected package hierarchy.
- **Module support:** The current implementation focuses on conventional directories and JAR files rather than full JPMS module-path discovery.

## Design purpose

`ClassScanner` provides the discovery foundation for reflection-based components.

It can be used by higher-level utilities that need to inspect available classes, identify annotations, discover implementations, or build registries dynamically.

Rather than coupling class discovery to a specific application feature, the scanner provides a reusable mechanism that other components can build upon.
