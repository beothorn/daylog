package daylog.task;

import java.util.List;
import java.util.Optional;

/**
 * This is the autocomplete rule: given what the user typed and the task history, which task should be suggested.
 * It is a pure function, with no UI, so it is easy to test. The dialog only applies its result.
 */
public final class TaskCompletion {

    private TaskCompletion() {
    }

    /**
     * Returns the most recent task that starts with the typed text, ignoring case, and is longer than it.
     * History is ordered oldest first, so we search from the end. Blank input suggests nothing.
     */
    public static Optional<TaskName> suggestionFor(String typedText, List<TaskName> historyOldestFirst) {
        if (typedText == null || typedText.isBlank()) {
            return Optional.empty();
        }
        String typedLowerCase = typedText.toLowerCase();
        for (int index = historyOldestFirst.size() - 1; index >= 0; index--) {
            TaskName candidate = historyOldestFirst.get(index);
            String candidateLowerCase = candidate.value().toLowerCase();
            if (candidateLowerCase.startsWith(typedLowerCase) && candidate.value().length() > typedText.length()) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
