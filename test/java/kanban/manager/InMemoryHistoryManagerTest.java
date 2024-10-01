package kanban.manager;

import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static kanban.model.TaskStatus.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void addAndGetHistory() {
        Task task = new Task(1L, "Test task", "Test task description", NEW);
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size(), "Неверное количество элементов'");
        assertEquals(task, historyManager.getHistory().get(0), "Задачи не совпадают");

        Epic epic = new Epic(2L, "Test epic", "Test epic description");
        historyManager.add(epic);

        assertEquals(2, historyManager.getHistory().size(), "Неверное количество элементов");
        assertEquals(epic, historyManager.getHistory().get(1), "Эпики не совпадают");

        SubTask subTask = new SubTask(3L, "Test createSubtask",
                "Test createSubtask description", NEW, epic.getId());
        historyManager.add(subTask);

        assertEquals(3, historyManager.getHistory().size(), "Неверное количество элементов");
        assertEquals(subTask, historyManager.getHistory().get(2), "Подзадачи не совпадают");
    }

    @Test
    void addSameTaskTwice() {
        Task task = new Task(1L, "Test task", "Test task description", NEW);

        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "Неверное количество элементов'");
        assertEquals(task, historyManager.getHistory().get(0), "Задачи не совпадают");

        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "Неверное количество элементов'");
        assertEquals(task, historyManager.getHistory().get(0), "Задачи не совпадают");
    }
}