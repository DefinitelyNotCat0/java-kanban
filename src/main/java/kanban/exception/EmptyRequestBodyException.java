package kanban.exception;

public class EmptyRequestBodyException extends RuntimeException {
    public EmptyRequestBodyException(String message) {
        super(message);
    }

    public EmptyRequestBodyException() {
        super("Ошибка. Пустое тело в запросе");
    }
}
