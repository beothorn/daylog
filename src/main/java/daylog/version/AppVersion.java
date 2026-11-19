package daylog.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tells which version of daylog is running. The number is not written in code: Maven copies the pom version into
 * version.properties when building, so the pom is the single place where the version is defined.
 */
public final class AppVersion {

    private static final Logger log = LoggerFactory.getLogger(AppVersion.class);

    private AppVersion() {
    }

    /** Returns the version such as 0.0.1-SNAPSHOT, or "unknown" if the resource is missing (for example when run from an IDE without Maven). */
    public static String current() {
        log.trace("current(): reading version.properties");
        try (InputStream stream = AppVersion.class.getResourceAsStream("/version.properties")) {
            if (stream == null) {
                log.warn("version.properties not found, the version is unknown");
                return "unknown";
            }
            Properties properties = new Properties();
            properties.load(stream);
            return properties.getProperty("version", "unknown");
        } catch (IOException e) {
            log.warn("Could not read version.properties: {}", e.toString());
            return "unknown";
        }
    }
}
