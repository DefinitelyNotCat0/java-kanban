package kanban.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static kanban.model.TaskStatus.DONE;
import static kanban.model.TaskStatus.IN_PROGRESS;
import static kanban.model.TaskStatus.NEW;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileBackedTaskManagerTest {
    private static final String testFileName = "FileBackedTaskManagerTest";
    private static final String fileExtension = ".csv";
    private static File testFile;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void setUp() {
        try {
            testFile = File.createTempFile(testFileName, fileExtension);
            taskManager = new FileBackedTaskManager(testFile);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException.getMessage());
        }
    }

    private void createTestTasks() {
        // Создадим задачи для тестирования
        Task task1 = new Task("Первая задача", "123", TaskStatus.IN_PROGRESS);
        Task task2 = new Task("Вторая задача", "890", NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Epic epic1 = new Epic("Первый эпик", "111");
        taskManager.createEpic(epic1);

        SubTask subTask1Epic1 = new SubTask("Задача 1 первого эпика", "1_1", NEW, epic1.getId());
        SubTask subTask2Epic1 = new SubTask("Задача 2 первого эпика", "1_2", NEW, epic1.getId());
        taskManager.createSubTask(subTask1Epic1);
        taskManager.createSubTask(subTask2Epic1);

        Epic secondEpic = new Epic("Второй эпик", "222");
        taskManager.createEpic(secondEpic);

        SubTask subTask1Epic2 = new SubTask("Задача 1 второго эпика", "2_1", NEW, secondEpic.getId());
        taskManager.createSubTask(subTask1Epic2);
    }

    private int getAllTaskTypesSize() {
        return taskManager.getTaskList().size() + taskManager.getEpicList().size() + taskManager.getSubTaskList().size();
    }

    @Test
    void createFileAndLoadFromIt() {
        // Слздаем задачи (и заполнеям файл)
        createTestTasks();
        // Создам новй менеджер без задач
        FileBackedTaskManager taskManager2 = new FileBackedTaskManager(null);
        assertEquals(0, taskManager2.getTaskList().size());
        assertEquals(0, taskManager2.getSubTaskList().size());
        assertEquals(0, taskManager2.getEpicList().size());

        // Заполняем новый менеджер задачами из файла, который был создан первым менеджером
        taskManager2 = FileBackedTaskManager.loadFromFile(testFile);
        assertEquals(2, taskManager2.getTaskList().size());
        assertEquals(3, taskManager2.getSubTaskList().size());
        assertEquals(2, taskManager2.getEpicList().size());

        // Проверяем, что задачи первого менеджера совпали с задачами второго
        assertArrayEquals(taskManager.getTaskList().toArray(), taskManager2.getTaskList().toArray());

        // Проверяем, что подзадачи первого менеджера совпали с подзадачами второго
        assertArrayEquals(taskManager.getSubTaskList().toArray(), taskManager2.getSubTaskList().toArray());

        // Проверяем, что эпики первого менеджера совпали с эпиками второго
        assertArrayEquals(taskManager.getEpicList().toArray(), taskManager2.getEpicList().toArray());
    }

    @Test
    void createTask() {
        Task task = new Task("Test createTask", "Test createTask description", NEW);
        final Long taskId = taskManager.createTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTaskList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Test createEpic", "Test createTask description");
        final Long epicId = taskManager.createEpic(epic);

        final Task savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
        assertEquals(savedEpic.getStatus(), NEW, "Созданный эпик имеет статус отличный от \"NEW\"");

        final List<Epic> epics = taskManager.getEpicList();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void createSubTask() {
        Epic epic = new Epic("Test createEpic", "Test createTask description");
        final Long epicId = taskManager.createEpic(epic);
        final Task savedEpic = taskManager.getEpicById(epicId);

        SubTask subTask = new SubTask("Test createSubtask", "Test createSubtask description", NEW, epicId);
        final Long subTaskId = taskManager.createSubTask(subTask);
        final Task savedSubTask = taskManager.getSubTaskById(subTaskId);

        assertNotNull(savedEpic, "Подзадача не найдена.");
        assertEquals(subTask, savedSubTask, "Подзадачи не совпадают.");
        assertEquals(savedEpic.getStatus(), NEW, "Эпик с новой подзадачей иммет статус отличный от \"NEW\"");

        final List<SubTask> subTasks = taskManager.getSubTaskList();

        assertNotNull(subTasks, "Подзадачи не возвращаются.");
        assertEquals(1, subTasks.size(), "Неверное количество подзадач.");
        assertEquals(subTask, subTasks.get(0), "Подзадачи не совпадают.");

        assertEquals(1, epic.getsubTaskList().size(), "Неверное количество подзадач у эпика.");
        assertEquals(subTask, epic.getsubTaskList().get(0),
                "ID подзадачи у эпика не совпадает с ID подзадачи.");

        subTask.setStatus(IN_PROGRESS);
        taskManager.updateSubTask(subTask);
        assertEquals(savedEpic.getStatus(), IN_PROGRESS, "Статус эпика подзадачи не равен \"IN_PROGRESS\"");

        subTask.setStatus(DONE);
        taskManager.updateSubTask(subTask);
        assertEquals(savedEpic.getStatus(), DONE, "Статус эпика подзадачи не равен \"DONE\"");
    }

    @Test
    void deleteAllTasks() {
        createTestTasks();

        taskManager.deleteAllTasks();
        int expectedSize = getAllTaskTypesSize();

        assertEquals(5, expectedSize, "Задачи не удалены");
    }

    @Test
    void deleteAllEpics() {
        createTestTasks();
        taskManager.deleteAllEpics();

        assertEquals(0, taskManager.getSubTaskList().size(), "Эпики не удалены");
        assertEquals(0, taskManager.getSubTaskList().size(), "Подзадачи эпиков не удалены");
    }

    @Test
    void deleteAllSubTasks() {
        createTestTasks();

        taskManager.deleteAllSubTasks();
        int expectedSize = getAllTaskTypesSize();

        assertEquals(4, expectedSize, "Подзадачи не удалены");
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("Test createTask", "Test createTask description", NEW);
        final Long taskId = taskManager.createTask(task);

        Task newTask = new Task("Test createTask2", "Test createTask description2", IN_PROGRESS);
        newTask.setId(taskId);
        taskManager.updateTask(newTask);

        taskManager.deleteTaskById(taskId);
        assertThrows(NoSuchElementException.class,
                () -> taskManager.getTaskById(taskId),
                "Задача не удалена");
    }

    @Test
    void deleteEpicById() {
        Epic epic = new Epic("Test createEpic", "Test createTask description");
        final Long epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask("Test createSubtask", "Test createSubtask description", NEW, epicId);
        final Long subTaskId = taskManager.createSubTask(subTask);

        taskManager.deleteEpicById(epicId);
        assertThrows(NoSuchElementException.class,
                () -> taskManager.getEpicById(epicId),
                "Эпик не удален");
        assertThrows(NoSuchElementException.class,
                () -> taskManager.getSubTaskById(subTaskId),
                "Подзадача эпика не удалена");
    }

    @Test
    void deleteSubTaskById() {
        Epic epic = new Epic("Test createEpic", "Test createTask description");
        final Long epicId = taskManager.createEpic(epic);

        SubTask subTask = new SubTask("Test createSubtask", "Test createSubtask description", NEW, epicId);
        final Long subTaskId = taskManager.createSubTask(subTask);

        taskManager.deleteSubTaskById(subTaskId);
        assertThrows(NoSuchElementException.class,
                () -> taskManager.getSubTaskById(subTaskId),
                "Подзадача не удалена");
        assertEquals(0,
                taskManager.getEpicById(epicId).getsubTaskList().size(),
                "Подзадача не удалена из эпика");
    }

    @Test
    void updateTask() {
        Task task = new Task("Test createTask", "Test createTask description", NEW);
        final Long taskId = taskManager.createTask(task);

        Task newTask = new Task(taskId, "Updated", "Updated", IN_PROGRESS);
        taskManager.updateTask(newTask);

        assertEquals(newTask, taskManager.getTaskById(taskId), "Задачи не равны");
        assertEquals(1, taskManager.getTaskList().size(), "Неверное количестов задач");
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("Test createEpic", "Test createEpic description");
        final Long epicId = taskManager.createEpic(epic);

        Epic newEpic = new Epic(epicId, "Updated", "Updated");
        taskManager.updateEpic(newEpic);

        assertEquals(newEpic, taskManager.getEpicById(epicId), "Эпики не равны");
        assertEquals(1, taskManager.getEpicList().size(), "Неверное количестов эпиков");
    }

    @Test
    void updateSubTask() {
        Epic epic = new Epic("Test createEpic", "Test createEpic description");
        final Long epicId = taskManager.createEpic(epic);

        Epic epic2 = new Epic("Test createEpic2", "Test createEpic description2");
        final Long epicId2 = taskManager.createEpic(epic2);

        SubTask subTask = new SubTask("Test createSubTask", "Test createSubTask description", NEW, epicId);
        final Long subTaskId = taskManager.createSubTask(subTask);

        SubTask newSubTask = new SubTask(subTaskId, "Updated", "Updated", NEW, epicId);
        taskManager.updateSubTask(newSubTask);

        assertEquals(newSubTask, taskManager.getSubTaskById(subTaskId), "Подзадачи не равны");
        assertEquals(1, taskManager.getSubTaskList().size(), "Неверное количестов подзадач");
        assertEquals(taskManager.getSubTaskById(subTaskId),
                taskManager.getEpicById(epicId).getsubTaskList().get(0),
                "Подзадача не совпадает с подзадачей эпика");
        assertEquals(1,
                taskManager.getEpicById(epicId).getsubTaskList().size(),
                "Неверное количестов подзадач у эпика");

        newSubTask.setStatus(DONE);
        taskManager.updateSubTask(newSubTask);

        assertEquals(DONE, taskManager.getEpicById(epicId).getStatus(), "Неверный статус эпика");
        assertEquals(newSubTask, taskManager.getSubTaskById(subTaskId), "Подзадачи не равны");
        assertEquals(1, taskManager.getSubTaskList().size(), "Неверное количестов подзадач");
        assertEquals(taskManager.getSubTaskById(subTaskId),
                taskManager.getEpicById(epicId).getsubTaskList().get(0),
                "Подзадача не совпадает с подзадачей эпика");
        assertEquals(1,
                taskManager.getEpicById(epicId).getsubTaskList().size(),
                "Неверное количестов подзадач у эпика");

        newSubTask.setStatus(IN_PROGRESS);
        newSubTask.setEpicId(epicId2);
        taskManager.updateSubTask(newSubTask);

        assertEquals(NEW, taskManager.getEpicById(epicId).getStatus(), "Неверный статус эпика");
        assertEquals(IN_PROGRESS, taskManager.getEpicById(epicId2).getStatus(), "Неверный статус эпика");
        assertEquals(newSubTask, taskManager.getSubTaskById(subTaskId), "Подзадачи не равны");
        assertEquals(1, taskManager.getSubTaskList().size(), "Неверное количестов подзадач");
        assertEquals(0,
                taskManager.getEpicById(epicId).getsubTaskList().size(),
                "Подзадача не была удалена из эпика");
        assertEquals(taskManager.getSubTaskById(subTaskId),
                taskManager.getEpicById(epicId2).getsubTaskList().get(0),
                "Подзадача не совпадает с подзадачей эпика");
        assertEquals(1,
                taskManager.getEpicById(epicId2).getsubTaskList().size(),
                "Неверное количестов подзадач у эпика");
    }
}