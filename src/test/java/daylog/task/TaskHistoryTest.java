package daylog.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaskHistoryTest {

    @Test
    void emptyWhenFileMissing(@TempDir Path temp) {
        TaskHistory history = new TaskHistory(temp.resolve("tasks.txt"));
        assertTrue(history.last().isEmpty());
        assertTrue(history.all().isEmpty());
    }

    @Test
    void remembersAndPersists(@TempDir Path temp) {
        Path file = temp.resolve("tasks.txt");
        TaskHistory history = new TaskHistory(file);
        history.remember(new TaskName("A"));
        history.remember(new TaskName("B"));
        TaskHistory reloaded = new TaskHistory(file);
        assertEquals(List.of(new TaskName("A"), new TaskName("B")), reloaded.all());
        assertEquals(new TaskName("B"), reloaded.last().orElseThrow());
    }

    @Test
    void rememberingKnownTaskMovesItToTheEnd(@TempDir Path temp) {
        TaskHistory history = new TaskHistory(temp.resolve("tasks.txt"));
        history.remember(new TaskName("A"));
        history.remember(new TaskName("B"));
        history.remember(new TaskName("A"));
        assertEquals(List.of(new TaskName("B"), new TaskName("A")), history.all());
    }

    @Test
    void skipsBlankLinesAndDuplicatesWhenLoading(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("tasks.txt");
        Files.writeString(file, "A\n\nB\nA\n");
        assertEquals(List.of(new TaskName("B"), new TaskName("A")), new TaskHistory(file).all());
    }
}
