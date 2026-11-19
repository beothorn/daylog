package daylog.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TaskCompletionTest {

    private final List<TaskName> history = List.of(new TaskName("Offhour work"), new TaskName("Office chores"), new TaskName("Review"));

    @Test
    void suggestsMostRecentMatch() {
        assertEquals(Optional.of(new TaskName("Office chores")), TaskCompletion.suggestionFor("Off", history));
    }

    @Test
    void ignoresCase() {
        assertEquals(Optional.of(new TaskName("Review")), TaskCompletion.suggestionFor("rev", history));
    }

    @Test
    void nothingForBlankOrUnknownOrExactMatch() {
        assertTrue(TaskCompletion.suggestionFor("", history).isEmpty());
        assertTrue(TaskCompletion.suggestionFor(null, history).isEmpty());
        assertTrue(TaskCompletion.suggestionFor("zzz", history).isEmpty());
        assertTrue(TaskCompletion.suggestionFor("Review", history).isEmpty());
    }
}
