package daylog.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import daylog.settings.Settings;
import daylog.task.TaskHistory;
import daylog.task.TaskName;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EntryRecorderTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-19T23:55:00Z"), ZoneOffset.UTC);

    @Test
    void recordsToConfiguredFileAndRemembersTask(@TempDir Path temp) throws Exception {
        Settings settings = new Settings(temp.resolve("s.properties"));
        Path logFile = temp.resolve("19.md");
        settings.saveLogFile(logFile);
        TaskHistory history = new TaskHistory(temp.resolve("tasks.txt"));
        EntryRecorder recorder = new EntryRecorder(settings, history, clock, () -> Optional.empty());

        assertTrue(recorder.record(new TaskName("Work"), "hello"));

        assertEquals("2026_11_19_23_55 `Work`\n```\nhello\n```\n", Files.readString(logFile));
        assertEquals(new TaskName("Work"), history.last().orElseThrow());
    }

    @Test
    void asksOnlyOnceWhenNoLogFileConfigured(@TempDir Path temp) throws Exception {
        Settings settings = new Settings(temp.resolve("s.properties"));
        Path chosen = temp.resolve("chosen.md");
        AtomicInteger questions = new AtomicInteger();
        EntryRecorder recorder = new EntryRecorder(settings, new TaskHistory(temp.resolve("tasks.txt")), clock,
            () -> { questions.incrementAndGet(); return Optional.of(chosen); });

        recorder.record(new TaskName("A"), "");
        recorder.record(new TaskName("B"), "");

        assertEquals(1, questions.get());
        assertEquals(3, Files.readAllLines(chosen).size());
    }

    @Test
    void nothingHappensWhenUserDoesNotChooseAFile(@TempDir Path temp) {
        TaskHistory history = new TaskHistory(temp.resolve("tasks.txt"));
        EntryRecorder recorder = new EntryRecorder(new Settings(temp.resolve("s.properties")), history, clock, Optional::empty);

        assertFalse(recorder.record(new TaskName("A"), "x"));
        assertTrue(history.all().isEmpty());
    }
}
