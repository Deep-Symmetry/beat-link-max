package beatlink;

import com.cycling74.max.MaxObject;
import org.deepsymmetry.beatlink.data.SearchableItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;

/**
 * Created by James Elliott on 11/16/24.
 */
public class Util {

    private static final String baseConfig = "handlers = java.util.logging.FileHandler\n" +
            ".level = INFO\n" +
            "java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n" +
            "java.util.logging.FileHandler.level = INFO\n" +
            "java.util.logging.FileHandler.limit = 256000\n" +
            "java.util.logging.FileHandler.append = true\n" +
            "java.util.logging.FileHandler.count = 5\n";

    /**
     * Ensures initialization happens only once.
     */
    private static final AtomicBoolean isLoggingInitialized = new AtomicBoolean(false);

    /**
     * Make sure that the logging environment has been properly established. Does nothing
     * if another caller has already requested this.
     */
    public static synchronized void initializeLogging() {
        if (!isLoggingInitialized.get()) {
            // Configure logging here:
            // Find path to our jar; write a temporary config file to configure Java logging to write a rollover Info-level log there.
            try {
                final File ourJar = new File(Util.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                final File ourPackage = ourJar.getParentFile().getParentFile().getParentFile();
                // Make sure the logs folder exists
                final File logFolder = new File(ourPackage, "logs");
                if (!logFolder.exists()) {
                    if (!logFolder.mkdir()) {
                        throw new IOException("Failed to create logs folder " + logFolder.getCanonicalPath());
                    }
                }

                // Make sure we can create files in the log directory
                final File probeFile = new File(logFolder, "some.log");
                if (probeFile.createNewFile()) {
                    if (!probeFile.delete()) {
                        throw new IOException("Failed to delete trial log file " + probeFile.getCanonicalPath());
                    }
                } else  {
                    throw new IOException("Failed to create log file in folder " + logFolder.getCanonicalPath());
                }

                // Set the logging configuration
                MaxObject.post("beat-link-max logs found in " + logFolder.getCanonicalPath());
                final String config = baseConfig + "java.util.logging.FileHandler.pattern = " + logFolder.getCanonicalPath() +
                        "/beat-link-max-%g.log";
                LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                MaxObject.error("Unable to enable logging in beat-link-max: " + e);
            }
            isLoggingInitialized.set(true);
        }
    }

    /**
     * Gets the label of a {@link SearchableItem}, safely returning an empty string if the item was null.
     *
     * @param item the searchable item whose label is desired
     * @return the label of the item, or an empty string if the item was null
     */
    public static String labelIfNotNull(SearchableItem item) {
        if (item == null) {
            return "";
        }
        return item.label;
    }

    /**
     * Returns a string unchanged unless it is {@code null}, in which case an empty string is returned instead.
     *
     * @param s the possibly-null string
     * @return a non-null string
     */
    public static String stringIfNotNull(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }
}
