package daylog.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TaskNameTest {

    @Test
    void trimsValue() {
        assertEquals("Offhour work", new TaskName("  Offhour work \t").value());
    }

    @Test
    void rejectsBlankAndNull() {
        assertThrows(IllegalArgumentException.class, () -> new TaskName("   "));
        assertThrows(IllegalArgumentException.class, () -> new TaskName(null));
    }

    @Test
    void rejectsMultipleLines() {
        assertThrows(IllegalArgumentException.class, () -> new TaskName("a\nb"));
    }
}
