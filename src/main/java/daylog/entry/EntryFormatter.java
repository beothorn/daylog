package daylog.entry;

import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is responsible for turning a LogEntry into the markdown text that is appended to the log file.
 * It does not touch files, that is the job of LogFileAppender.
 */
public final class EntryFormatter {

    private static final Logger log = LoggerFactory.getLogger(EntryFormatter.class);
    private static final DateTimeFormatter MOMENT_FORMAT = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");

    private EntryFormatter() {
    }

    /**
     * The first line is the moment and the task in backticks. If there is text it follows in a fenced block.
     * The fence is always longer than any run of backticks inside the text, otherwise the text could close it early.
     * The result always ends with a line break.
     */
    public static String format(LogEntry entry) {
        log.trace("format(entry): formatting entry for task {}", entry.task());
        String header = MOMENT_FORMAT.format(entry.moment()) + " `" + entry.task().value() + "`\n";
        if (entry.text().isEmpty()) {
            return header;
        }
        String fence = "`".repeat(Math.max(3, longestBacktickRun(entry.text()) + 1));
        return header + fence + "\n" + entry.text() + "\n" + fence + "\n";
    }

    /** This counts the longest sequence of consecutive backticks in the text. */
    private static int longestBacktickRun(String text) {
        int longest = 0;
        int current = 0;
        for (char character : text.toCharArray()) {
            current = character == '`' ? current + 1 : 0;
            longest = Math.max(longest, current);
        }
        return longest;
    }
}
