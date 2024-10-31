package kanban.exception;

import kanban.model.Task;

public class CreateTaskException extends RuntimeException {

    public CreateTaskException(String message) {
        super(message);
    }

    public CreateTaskException(Task task, String message) {
        super(String.format("%s (id = %d): %s",
                task.getClass().getSimpleName(),
                task.getId(),
                message));
    }
}