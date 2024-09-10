package kanban.manager;

import java.util.ArrayList;
import java.util.List;

import kanban.task.Task;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> taskHistoryList = new ArrayList<>(10);

    @Override
    public void add(Task task) {
        if (taskHistoryList.size() >= 10) {
            taskHistoryList.remove(0);
        }
        taskHistoryList.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return taskHistoryList;
    }
}
