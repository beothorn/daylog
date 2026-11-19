package daylog.task;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the autocomplete rule: given what the user typed and the task history, which tasks should be offered in the drop-down.
 * It is a pure function, with no UI, so it is easy to test. The dialog only displays its result.
 */
public final class TaskCompletion {

    private TaskCompletion() {
    }

    /**
     * Returns up to maxSuggestions tasks that start with the typed text, ignoring case, most recent first.
     * History is ordered oldest first, so we walk it from the end. Blank input offers the most recent tasks.
     * A task exactly equal to what was typed is not offered, there is nothing left to complete.
     */
    public static List<TaskName> suggestionsFor(String typedText, List<TaskName> historyOldestFirst, int maxSuggestions) {
        String typedLowerCase = typedText == null ? "" : typedText.strip().toLowerCase();
        List<TaskName> suggestions = new ArrayList<>();
        for (int index = historyOldestFirst.size() - 1; index >= 0 && suggestions.size() < maxSuggestions; index--) {
            TaskName candidate = historyOldestFirst.get(index);
            boolean matches = candidate.value().toLowerCase().startsWith(typedLowerCase);
            boolean isExactlyWhatWasTyped = candidate.value().equals(typedText);
            if (matches && !isExactlyWhatWasTyped) {
                suggestions.add(candidate);
            }
        }
        return suggestions;
    }
}
