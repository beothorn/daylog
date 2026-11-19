package daylog.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import daylog.task.TaskName;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EntryFormatterTest {

    private final LocalDateTime moment = LocalDateTime.of(2026, 11, 19, 23, 55);

    private String format(String text) {
        return EntryFormatter.format(new LogEntry(moment, new TaskName("Offhour work"), text));
    }

    @Test
    void formatsMultilineText() {
        assertEquals("2026_11_19_23_55 `Offhour work`\n```\nline one\nline two\n```\n", format("line one\nline two"));
    }

    @Test
    void emptyTextOnlyHasHeader() {
        assertEquals("2026_11_19_23_55 `Offhour work`\n", format("  \n "));
        assertEquals("2026_11_19_23_55 `Offhour work`\n", format(null));
    }

    @Test
    void padsSingleDigitFields() {
        String formatted = EntryFormatter.format(new LogEntry(LocalDateTime.of(2026, 1, 2, 3, 4), new TaskName("t"), ""));
        assertEquals("2026_01_02_03_04 `t`\n", formatted);
    }

    @Test
    void fenceGrowsWhenTextContainsBackticks() {
        assertEquals("2026_11_19_23_55 `Offhour work`\n````\nuse ``` here\n````\n", format("use ``` here"));
    }
}
