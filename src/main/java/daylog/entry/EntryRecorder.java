package daylog.entry;

import daylog.settings.Settings;
import daylog.task.TaskHistory;
import daylog.task.TaskName;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the whole "save an entry" use case, without any UI: it finds the log file (asking the user the first time),
 * stamps the entry with the current time, appends it and remembers the task for autocomplete.
 * The question to the user is injected as a supplier so this class does not depend on Swing and can be tested.
 */
public class EntryRecorder {

    private static final Logger log = LoggerFactory.getLogger(EntryRecorder.class);

    private final Settings settings;
    private final TaskHistory taskHistory;
    private final Clock clock;
    private final Supplier<Optional<Path>> askUserForLogFile;

    /** Everything this class needs is passed in, so tests can use a fixed clock and a fake question. */
    public EntryRecorder(Settings settings, TaskHistory taskHistory, Clock clock, Supplier<Optional<Path>> askUserForLogFile) {
        this.settings = settings;
        this.taskHistory = taskHistory;
        this.clock = clock;
        this.askUserForLogFile = askUserForLogFile;
    }

    /**
     * Records the entry and returns true, or returns false when there was no log file and the user did not choose one
     * (nothing is written and the task is not remembered, so the user can try again).
     */
    public boolean record(TaskName task, String text) {
        log.trace("record(task, text): recording task {}", task);
        Optional<Path> logFile = settings.logFile().or(this::askAndRemember);
        if (logFile.isEmpty()) {
            log.debug("No log file configured and none chosen, nothing recorded");
            return false;
        }
        LogEntry entry = new LogEntry(LocalDateTime.now(clock), task, text);
        LogFileAppender.append(logFile.get(), entry);
        taskHistory.remember(task);
        log.info("Entry saved to {}", logFile.get());
        return true;
    }

    /** This asks the user for a log file and persists the answer, so the question is asked only once. */
    private Optional<Path> askAndRemember() {
        log.trace("askAndRemember(): no log file configured, asking the user");
        Optional<Path> chosen = askUserForLogFile.get();
        chosen.ifPresent(settings::saveLogFile);
        return chosen;
    }
}
