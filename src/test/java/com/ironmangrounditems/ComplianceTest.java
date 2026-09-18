package com.ironmangrounditems;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The README says what this plugin does not do. These assert it against the source, so a later
 * change that quietly breaks one of those claims fails the build rather than the review.
 */
public class ComplianceTest
{
    private static final List<String> sources = new ArrayList<>();

    @BeforeClass public static void readSource() throws IOException
    {
        Path root = Paths.get("src", "main", "java");
        try (DirectoryStream<Path> files = Files.newDirectoryStream(root.resolve("com/ironmangrounditems"), "*.java"))
        {
            for (Path file : files) sources.add(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
        }
        assertTrue("no sources found", sources.size() >= 4);
    }

    private static void absent(String... fragments)
    {
        for (String source : sources)
            for (String fragment : fragments)
                assertTrue(fragment + " appears in the plugin source", !source.contains(fragment));
    }

    @Test public void makesNoNetworkRequests()
    { absent("java.net", "HttpURLConnection", "Socket", "OkHttp", "openConnection"); }

    @Test public void readsAndWritesNoFiles()
    { absent("java.io.File", "java.nio.file", "FileOutputStream", "FileInputStream", "RUNELITE_DIR"); }

    @Test public void usesNoReflection()
    { absent("java.lang.reflect", "setAccessible", "Class.forName"); }

    @Test public void startsNoThreads()
    { absent("new Thread", "Executors.", "ScheduledExecutorService", "ForkJoin"); }

    @Test public void neverActsForThePlayer()
    { absent("invokeMenuAction", "menuAction(", "createMenuEntry", "java.awt.Robot", "dispatchEvent"); }
}
