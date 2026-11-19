package daylog.task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class remembers the tasks the user logged before, most recent last, without duplicates.
 * It keeps them in memory (read once at startup, so autocomplete is instant) and mirrors them to a file with one task per line.
 */
public class TaskHistory {

    private static final Logger log = LoggerFactory.getLogger(TaskHistory.class);

    private final Path historyFile;
    private final List<TaskName> tasks;

    /** Loads the history from the file, a missing file is an empty history and lines that are not valid tasks are skipped. */
    public TaskHistory(Path historyFile) {
        this.historyFile = historyFile;
        this.tasks = load(historyFile);
        log.debug("Loaded {} tasks from {}", tasks.size(), historyFile);
    }

    /** This returns the most recently saved task, used to prefill the dialog. */
    public Optional<TaskName> last() {
        if (tasks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tasks.get(tasks.size() - 1));
    }

    /** This returns all tasks, oldest first, as an unmodifiable copy. */
    public List<TaskName> all() {
        return List.copyOf(tasks);
    }

    /**
     * Here we add the task as the most recent one. If it was already known it moves to the end instead of being duplicated.
     * The whole file is rewritten, which is fine because a diary has few distinct tasks.
     */
    public void remember(TaskName task) {
        log.trace("remember(task): {}", task);
        tasks.remove(task);
        tasks.add(task);
        save();
    }

    /** This writes the in memory list back to the history file. */
    private void save() {
        List<String> lines = tasks.stream().map(TaskName::value).toList();
        try {
            Files.write(historyFile, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + historyFile, e);
        }
    }

    /** This reads the file into a mutable list, skipping blank lines. */
    private static List<TaskName> load(Path historyFile) {
        List<TaskName> loaded = new ArrayList<>();
        if (!Files.exists(historyFile)) {
            return loaded;
        }
        try {
            for (String line : Files.readAllLines(historyFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                TaskName task = new TaskName(line);
                loaded.remove(task);
                loaded.add(task);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + historyFile, e);
        }
        return loaded;
    }
}
