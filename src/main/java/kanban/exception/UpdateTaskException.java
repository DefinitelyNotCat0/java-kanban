package kanban.exception;

import kanban.model.Task;

public class UpdateTaskException extends RuntimeException {

    public UpdateTaskException(String message) {
        super(message);
    }

    public UpdateTaskException(Task task, String message) {
        super(String.format("%s (id = %d): %s",
                task.getClass().getSimpleName(),
                task.getId(),
                message));
    }
}
