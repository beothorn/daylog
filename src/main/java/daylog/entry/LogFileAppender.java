package daylog.entry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is responsible for appending formatted entries to the user's log file, never changing what is already there.
 */
public final class LogFileAppender {

    private static final Logger log = LoggerFactory.getLogger(LogFileAppender.class);

    private LogFileAppender() {
    }

    /**
     * Appends the entry at the end of the file, creating the file and its folders if missing.
     * If the file has content that does not end with a line break we add one first, so the entry starts on its own line.
     */
    public static void append(Path logFile, LogEntry entry) {
        log.trace("append(logFile, entry): appending to {}", logFile);
        try {
            Path parent = logFile.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            String prefix = endsWithoutLineBreak(logFile) ? "\n" : "";
            String block = prefix + EntryFormatter.format(entry);
            log.debug("Appending {} characters to {}", block.length(), logFile);
            Files.writeString(logFile, block, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot append to " + logFile, e);
        }
    }

    /** True when the file exists, is not empty and its last byte is not a line break. */
    private static boolean endsWithoutLineBreak(Path logFile) throws IOException {
        if (!Files.exists(logFile) || Files.size(logFile) == 0) {
            return false;
        }
        try (RandomAccessFile file = new RandomAccessFile(logFile.toFile(), "r")) {
            file.seek(file.length() - 1);
            return file.read() != '\n';
        }
    }
}
