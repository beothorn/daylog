package daylog.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for the user's persisted settings, today only the log file where entries are appended.
 * It is stored as a java properties file so a human can read and edit it.
 */
public class Settings {

    private static final Logger log = LoggerFactory.getLogger(Settings.class);
    private static final String LOG_FILE_KEY = "logFile";

    private final Path settingsFile;

    /** The settings are read from and written to the given file, which may not exist yet. */
    public Settings(Path settingsFile) {
        this.settingsFile = settingsFile;
    }

    /** This returns the configured log file, or empty when the user never chose one. */
    public Optional<Path> logFile() {
        log.trace("logFile(): reading {} from {}", LOG_FILE_KEY, settingsFile);
        String value = load().getProperty(LOG_FILE_KEY);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Path.of(value));
    }

    /** This stores the chosen log file, keeping any other property already in the file. */
    public void saveLogFile(Path logFile) {
        log.trace("saveLogFile(logFile): storing {}", logFile);
        Properties properties = load();
        properties.setProperty(LOG_FILE_KEY, logFile.toAbsolutePath().toString());
        try (Writer writer = Files.newBufferedWriter(settingsFile, StandardCharsets.UTF_8)) {
            properties.store(writer, "daylog settings");
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + settingsFile, e);
        }
        log.info("Log file set to {}", logFile);
    }

    /** This loads the properties; a missing file simply means no settings yet. */
    private Properties load() {
        Properties properties = new Properties();
        if (!Files.exists(settingsFile)) {
            log.debug("Settings file {} does not exist yet", settingsFile);
            return properties;
        }
        try (Reader reader = Files.newBufferedReader(settingsFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + settingsFile, e);
        }
        return properties;
    }
}
