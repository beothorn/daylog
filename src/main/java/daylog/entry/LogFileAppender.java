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
     * Entries are separated from what is already in the file by exactly one blank line (see separatorBefore).
     */
    public static void append(Path logFile, LogEntry entry) {
        log.trace("append(logFile, entry): appending to {}", logFile);
        try {
            Path parent = logFile.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            String block = separatorBefore(logFile) + EntryFormatter.format(entry);
            log.debug("Appending {} characters to {}", block.length(), logFile);
            Files.writeString(logFile, block, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot append to " + logFile, e);
        }
    }

    /**
     * This decides what must be written before the entry so there is one blank line between it and the previous content:
     * nothing for a missing/empty file or one already ending in a blank line, one line break if it ends in a single line break
     * and two if it does not end in one. We look at the last bytes ignoring carriage returns, so Windows files (CRLF) work too.
     */
    private static String separatorBefore(Path logFile) throws IOException {
        if (!Files.exists(logFile) || Files.size(logFile) == 0) {
            return "";
        }
        String tail = lastCharacters(logFile).replace("\r", "");
        if (tail.endsWith("\n\n")) {
            return "";
        }
        return tail.endsWith("\n") ? "\n" : "\n\n";
    }

    /** This reads at most the last four bytes of the file as text (enough to see a CRLF CRLF ending). */
    private static String lastCharacters(Path logFile) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(logFile.toFile(), "r")) {
            int length = (int) Math.min(4, file.length());
            byte[] tail = new byte[length];
            file.seek(file.length() - length);
            file.readFully(tail);
            return new String(tail, StandardCharsets.ISO_8859_1);
        }
    }
}
