package kanban.manager;

import java.util.List;

import kanban.task.Task;

public interface HistoryManager {
    // Пометить задачу как просмотренную
    void add(Task task);

    // Получить историю просмотров задач
    List<Task> getHistory();
}