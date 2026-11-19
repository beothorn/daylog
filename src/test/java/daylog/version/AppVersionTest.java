package daylog.version;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppVersionTest {

    @Test
    void versionComesFromTheBuildAndLooksLikeSemver() {
        assertTrue(AppVersion.current().matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"), AppVersion.current());
    }
}
