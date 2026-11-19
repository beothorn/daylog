package daylog.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsTest {

    @Test
    void noLogFileWhenFileMissing(@TempDir Path temp) {
        assertTrue(new Settings(temp.resolve("s.properties")).logFile().isEmpty());
    }

    @Test
    void remembersLogFileAcrossInstances(@TempDir Path temp) {
        Path settingsFile = temp.resolve("s.properties");
        Path logFile = temp.resolve("diary/19.md");
        new Settings(settingsFile).saveLogFile(logFile);
        assertEquals(Optional.of(logFile.toAbsolutePath()), new Settings(settingsFile).logFile());
    }

    @Test
    void blankValueMeansNoLogFile(@TempDir Path temp) throws Exception {
        Path settingsFile = temp.resolve("s.properties");
        Files.writeString(settingsFile, "logFile=   \n");
        assertTrue(new Settings(settingsFile).logFile().isEmpty());
    }

    @Test
    void windowsStylePathWithBackslashesSurvivesRoundTrip(@TempDir Path temp) {
        Path settingsFile = temp.resolve("s.properties");
        Path logFile = temp.resolve("with space/ünï.md");
        new Settings(settingsFile).saveLogFile(logFile);
        assertEquals(logFile.toAbsolutePath(), new Settings(settingsFile).logFile().orElseThrow());
    }
}
