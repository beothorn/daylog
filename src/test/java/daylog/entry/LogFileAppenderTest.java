package daylog.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import daylog.task.TaskName;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LogFileAppenderTest {

    private final LogEntry entry = new LogEntry(LocalDateTime.of(2026, 11, 19, 23, 55), new TaskName("T"), "text");
    private final String entryText = "2026_11_19_23_55 `T`\n```\ntext\n```\n";

    @Test
    void createsFileAndFolders(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("diary/new.md");
        LogFileAppender.append(file, entry);
        assertEquals(entryText, Files.readString(file));
    }

    @Test
    void keepsExistingContentEndingWithLineBreak(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("19.md");
        Files.writeString(file, "# Day\nnotes\n");
        LogFileAppender.append(file, entry);
        assertEquals("# Day\nnotes\n" + entryText, Files.readString(file));
    }

    @Test
    void addsLineBreakWhenExistingContentLacksOne(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("19.md");
        Files.writeString(file, "no trailing newline");
        LogFileAppender.append(file, entry);
        assertEquals("no trailing newline\n" + entryText, Files.readString(file));
    }

    @Test
    void appendsTwiceInOrder(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("19.md");
        LogFileAppender.append(file, entry);
        LogFileAppender.append(file, entry);
        assertEquals(entryText + entryText, Files.readString(file));
    }
}
