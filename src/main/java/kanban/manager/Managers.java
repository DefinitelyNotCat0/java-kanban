package kanban.manager;

import java.io.File;

public class Managers {
    private static final String fileName = "data.csv";

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static FileBackedTaskManager getFileBackedTaskManager() {
        return new FileBackedTaskManager(new File(fileName));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
