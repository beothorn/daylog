package daylog.task;

/**
 * This represents the name of a task: a trimmed, non blank, single line text.
 * It can only be created with a valid value, so code receiving a TaskName never needs to validate it again.
 */
public record TaskName(String value) {

    /** The value is trimmed and rejected when blank or when it spans more than one line. */
    public TaskName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Task name cannot be blank");
        }
        value = value.strip();
        if (value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException("Task name must be a single line");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
