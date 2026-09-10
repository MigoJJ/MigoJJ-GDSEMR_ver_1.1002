package com.emr.gds.core.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Canonical path resolution for the app's {@code app/db/*.db} SQLite files.
 * Walks up from the working directory to the project root (identified by
 * {@code gradlew} or {@code .git}) so resolution is independent of the
 * directory the app happens to be launched from, then falls back to a
 * legacy {@code db/} directory for files that predate the {@code app/db/}
 * convention.
 * <p>
 * Not used by features whose database is a bundled classpath resource
 * (e.g. KCD's {@code src/main/resources/database/kcd_database.db}), which
 * intentionally resolves relative to the working directory instead.
 */
public final class DbPaths {

    private DbPaths() {
    }

    public static Path resolveDbPath(String fileName) {
        Path root = findProjectRoot();
        Path appDb = root.resolve("app").resolve("db").resolve(fileName);
        if (Files.exists(appDb)) {
            return appDb;
        }
        Path legacyDb = root.resolve("db").resolve(fileName);
        if (Files.exists(legacyDb)) {
            return legacyDb;
        }
        return appDb;
    }

    public static String jdbcUrl(String fileName) {
        return "jdbc:sqlite:" + resolveDbPath(fileName).toAbsolutePath();
    }

    private static Path findProjectRoot() {
        Path p = Paths.get("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("gradlew")) && !Files.exists(p.resolve(".git"))) {
            p = p.getParent();
        }
        return (p != null) ? p : Paths.get("").toAbsolutePath();
    }
}
