package daylog.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TaskCompletionTest {

    private final List<TaskName> history = List.of(new TaskName("Offhour work"), new TaskName("Office chores"), new TaskName("Review"));

    @Test
    void offersMatchesMostRecentFirst() {
        assertEquals(List.of(new TaskName("Office chores"), new TaskName("Offhour work")), TaskCompletion.suggestionsFor("Off", history, 8));
    }

    @Test
    void ignoresCase() {
        assertEquals(List.of(new TaskName("Review")), TaskCompletion.suggestionsFor("rev", history, 8));
    }

    @Test
    void blankOffersMostRecentTasksUpToTheLimit() {
        assertEquals(List.of(new TaskName("Review"), new TaskName("Office chores")), TaskCompletion.suggestionsFor("", history, 2));
        assertEquals(3, TaskCompletion.suggestionsFor(null, history, 8).size());
    }

    @Test
    void nothingForUnknownOrExactMatch() {
        assertTrue(TaskCompletion.suggestionsFor("zzz", history, 8).isEmpty());
        assertTrue(TaskCompletion.suggestionsFor("Review", history, 8).isEmpty());
    }
}
