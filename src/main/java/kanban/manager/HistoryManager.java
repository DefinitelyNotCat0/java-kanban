package kanban.manager;

import java.util.List;

import kanban.model.Task;

public interface HistoryManager {
    // Пометить задачу как просмотренную
    void add(Task task);

    // Удалить задачу из просмотра
    void remove(Long id);

    // Получить историю просмотров задач
    List<Task> getHistory();
}
