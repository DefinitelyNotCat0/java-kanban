package kanban.manager;

import java.util.ArrayList;
import java.util.List;

import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;

public interface TaskManager {
    // Создать задачу
    Long createTask(Task task);

    // Создать эпик
    Long createEpic(Epic epic);

    // Создать подзадачу
    Long createSubTask(SubTask subTask);

    // Удалить все задачи
    void deleteAllTasks();

    // Удалить все эпики (и все подзадачи)
    void deleteAllEpics();

    // Удалить все подзадачи
    void deleteAllSubTasks();

    // Получить задачу по идентификатору
    Task getTaskById(Long id);

    // Получить эпик по идентификатору
    Epic getEpicById(Long id);

    // Получить подзадачу по идентификатору
    SubTask getSubTaskById(Long id);

    // Удалить задачу по идентификатору
    void deleteTaskById(Long id);

    // Удалить эпик по идентификатору
    void deleteEpicById(Long id);

    // Удалить подзадачу по идентификатору
    void deleteSubTaskById(Long id);

    // Обновить задачу
    void updateTask(Task task);

    // Обновить эпик
    void updateEpic(Epic epic);

    // Обновить позадачу
    void updateSubTask(SubTask subTask);

    // Получить список задач
    ArrayList<Task> getTaskList();

    // Получить список эпиков
    ArrayList<Epic> getEpicList();

    // Получить список подзадач
    ArrayList<SubTask> getSubTaskList();

    // Получить список подзадач определенного эпика
    ArrayList<SubTask> getSubTaskListByEpicId(Long id);

    // Получить историю просмотров задач
    List<Task> getHistory();

    // Получить задачи отсортированные по приоритету
    List<Task> getPrioritizedTasks();
}
