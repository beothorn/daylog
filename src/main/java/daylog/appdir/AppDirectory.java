package daylog.appdir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for knowing where daylog keeps its own data (settings and task history).
 * It does not read or write those files, that is the job of Settings and TaskHistory.
 */
public record AppDirectory(Path path) {

    private static final Logger log = LoggerFactory.getLogger(AppDirectory.class);

    /**
     * Here we pick the folder for the current system: %APPDATA%\daylog on Windows
     * (when APPDATA is set) and ~/.daylog everywhere else. The values come as parameters
     * so tests do not depend on the real machine.
     */
    public static AppDirectory resolve(String operatingSystemName, String appDataValue, Path homeDirectory) {
        log.trace("resolve(os, appData, home): choosing the app directory for {}", operatingSystemName);
        boolean isWindows = operatingSystemName.toLowerCase().contains("win");
        if (isWindows && appDataValue != null && !appDataValue.isBlank()) {
            return new AppDirectory(Path.of(appDataValue).resolve("daylog"));
        }
        return new AppDirectory(homeDirectory.resolve(".daylog"));
    }

    /** This resolves the directory for the machine we are running on right now. */
    public static AppDirectory forThisMachine() {
        return resolve(System.getProperty("os.name"), System.getenv("APPDATA"), Path.of(System.getProperty("user.home")));
    }

    /** This makes sure the directory exists and returns it, so callers can just write files into it. */
    public Path createdIfMissing() {
        log.trace("createdIfMissing(): ensuring {} exists", path);
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create app directory " + path, e);
        }
    }

    /** This gives the path of a file that lives inside the app directory. */
    public Path file(String name) {
        return path.resolve(name);
    }
}
