package daylog.appdir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppDirectoryTest {

    private final Path home = Path.of("/home/someone");

    @Test
    void linuxUsesDotDaylogInHome() {
        assertEquals(home.resolve(".daylog"), AppDirectory.resolve("Linux", null, home).path());
    }

    @Test
    void windowsUsesAppData() {
        assertEquals(Path.of("C:/Users/x/AppData/Roaming/daylog"),
            AppDirectory.resolve("Windows 11", "C:/Users/x/AppData/Roaming", home).path());
    }

    @Test
    void windowsWithoutAppDataFallsBackToHome() {
        assertEquals(home.resolve(".daylog"), AppDirectory.resolve("Windows 11", " ", home).path());
    }

    @Test
    void createsMissingDirectories(@TempDir Path temp) {
        Path created = new AppDirectory(temp.resolve("a/b")).createdIfMissing();
        assertTrue(Files.isDirectory(created));
    }
}
