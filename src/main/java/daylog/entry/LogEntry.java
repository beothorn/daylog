package daylog.entry;

import daylog.task.TaskName;
import java.time.LocalDateTime;

/** This represents one thing the user logged: when, which task and the free text (which may be empty). */
public record LogEntry(LocalDateTime moment, TaskName task, String text) {

    /** The text is never null, an absent text becomes an empty one. */
    public LogEntry {
        text = text == null ? "" : text.strip();
    }
}
