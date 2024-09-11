package kanban.manager;

import java.util.ArrayList;
import java.util.List;

import kanban.model.Task;

public class InMemoryHistoryManager implements HistoryManager {
    public static final Integer MAX_SIZE = 10;
    private final List<Task> taskHistoryList = new ArrayList<>(MAX_SIZE);

    @Override
    public void add(Task task) {
        if (task == null) {
            System.out.println("Пустая задача. Добавление в историю не произведено");
            return;
        }

        if (taskHistoryList.size() >= MAX_SIZE) {
            taskHistoryList.remove(0);
        }

        taskHistoryList.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(taskHistoryList);
    }
}
