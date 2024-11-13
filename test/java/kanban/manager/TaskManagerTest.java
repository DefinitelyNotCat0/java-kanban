package kanban.manager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import kanban.exception.CreateTaskException;
import kanban.exception.UpdateTaskException;
import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static kanban.model.TaskStatus.DONE;
import static kanban.model.TaskStatus.IN_PROGRESS;
import static kanban.model.TaskStatus.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected void createTestTasks() {
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
    void createTask() {
        Task task = new Task("Test createTask", "Test createTask description", NEW);
        final Long taskId = taskManager.createTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTaskList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
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
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void createSubtask() {
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
        assertEquals(subTask, subTasks.getFirst(), "Подзадачи не совпадают.");

        assertEquals(1, epic.getSubTaskList().size(), "Неверное количество подзадач у эпика.");
        assertEquals(subTask, epic.getSubTaskList().getFirst(),
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
                taskManager.getEpicById(epicId).getSubTaskList().size(),
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
                taskManager.getEpicById(epicId).getSubTaskList().getFirst(),
                "Подзадача не совпадает с подзадачей эпика");
        assertEquals(1,
                taskManager.getEpicById(epicId).getSubTaskList().size(),
                "Неверное количестов подзадач у эпика");

        newSubTask.setStatus(DONE);
        taskManager.updateSubTask(newSubTask);

        assertEquals(DONE, taskManager.getEpicById(epicId).getStatus(), "Неверный статус эпика");
        assertEquals(newSubTask, taskManager.getSubTaskById(subTaskId), "Подзадачи не равны");
        assertEquals(1, taskManager.getSubTaskList().size(), "Неверное количестов подзадач");
        assertEquals(taskManager.getSubTaskById(subTaskId),
                taskManager.getEpicById(epicId).getSubTaskList().getFirst(),
                "Подзадача не совпадает с подзадачей эпика");
        assertEquals(1,
                taskManager.getEpicById(epicId).getSubTaskList().size(),
                "Неверное количестов подзадач у эпика");

        newSubTask.setStatus(IN_PROGRESS);
        newSubTask.setEpicId(epicId2);
        taskManager.updateSubTask(newSubTask);

        assertEquals(NEW, taskManager.getEpicById(epicId).getStatus(), "Неверный статус эпика");
        assertEquals(IN_PROGRESS, taskManager.getEpicById(epicId2).getStatus(), "Неверный статус эпика");
        assertEquals(newSubTask, taskManager.getSubTaskById(subTaskId), "Подзадачи не равны");
        assertEquals(1, taskManager.getSubTaskList().size(), "Неверное количестов подзадач");
        assertEquals(0,
                taskManager.getEpicById(epicId).getSubTaskList().size(),
                "Подзадача не была удалена из эпика");
        assertEquals(taskManager.getSubTaskById(subTaskId),
                taskManager.getEpicById(epicId2).getSubTaskList().getFirst(),
                "Подзадача не совпадает с подзадачей эпика");
        assertEquals(1,
                taskManager.getEpicById(epicId2).getSubTaskList().size(),
                "Неверное количестов подзадач у эпика");
    }


    @Test
    void getSubTaskListByEpicId() {
        createTestTasks();
        Long epicId = taskManager.getEpicList().getFirst().getId();
        assertEquals(2, taskManager.getSubTaskListByEpicId(epicId).size());
    }

    @Test
    void getHistoryTasks() {
        createTestTasks();
        Set<Task> expected = new HashSet<>();
        Task task;
        Epic epic;
        SubTask subTask;

        task = taskManager.getTaskById(1L);
        expected.add(task);
        epic = taskManager.getEpicById(6L);
        expected.add(epic);
        task = taskManager.getTaskById(1L);
        expected.add(task);
        task = taskManager.getTaskById(2L);
        expected.add(task);
        task = taskManager.getTaskById(1L);
        expected.add(task);
        subTask = taskManager.getSubTaskById(4L);
        expected.add(subTask);
        epic = taskManager.getEpicById(6L);
        expected.add(epic);
        subTask = taskManager.getSubTaskById(7L);
        expected.add(subTask);
        task = taskManager.getTaskById(2L);
        expected.add(task);
        epic = taskManager.getEpicById(3L);
        expected.add(epic);
        assertEquals(expected.size(), taskManager.getHistory().size());
    }

    @Test
    void shouldBeNewStatusForCreatedEpic() {
        Epic firstEpic = new Epic("Первый эпик", "111");
        taskManager.createEpic(firstEpic);

        assertEquals(taskManager.getEpicById(1L).getStatus(), NEW, "Статус у нового эпика не равен \"NEW\"");
    }

    @Test
    void findNonExistantTasks() {
        assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(1L));
        assertThrows(NoSuchElementException.class, () -> taskManager.getEpicById(1L));
        assertThrows(NoSuchElementException.class, () -> taskManager.getSubTaskById(1L));
    }

    @Test
    void epicTimeCalculationTest() {
        Epic epic = new Epic("Epic", "---");
        Long epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Subtask1", "1_1", NEW, epic.getId(),
                LocalDateTime.of(2000, Month.MARCH, 2, 13, 30),
                Duration.ofMinutes(30));
        SubTask subTask2 = new SubTask("Subtask2", "1_2", NEW, epic.getId());
        SubTask subTask3 = new SubTask("Subtask3", "1_3", NEW, epic.getId(),
                LocalDateTime.of(2000, Month.MARCH, 2, 13, 0),
                Duration.ofMinutes(30));
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        Long subTaskId = taskManager.createSubTask(subTask3);

        assertEquals(Duration.ofMinutes(60), taskManager.getEpicById(epicId).getDuration());
        assertEquals(taskManager.getSubTaskById(subTaskId).getStartTime(), taskManager.getEpicById(epicId).getStartTime());
    }

    @Test
    void getPrioritizedTasksTest() {
        Task task1 = new Task("Test task1", "Test task1 description", NEW);
        final Long task1Id = taskManager.createTask(task1);
        assertEquals(0, taskManager.getPrioritizedTasks().size());
        assertEquals(Duration.ZERO, taskManager.getTaskById(task1Id).getDuration());
        assertNull(taskManager.getTaskById(task1Id).getStartTime());

        Task task2 = new Task("Test task2", "Test task2 description",
                IN_PROGRESS, LocalDateTime.now(), Duration.ofMinutes(20));
        taskManager.createTask(task2);
        assertEquals(task2, taskManager.getPrioritizedTasks().getFirst());
        assertEquals(1, taskManager.getPrioritizedTasks().size());

        Task task3 = new Task("Test task3", "Test task3 description",
                IN_PROGRESS, LocalDateTime.now().plusDays(1), null);
        taskManager.createTask(task3);
        assertEquals(task3, taskManager.getPrioritizedTasks().get(1));
        assertEquals(2, taskManager.getPrioritizedTasks().size());
    }

    @Test
    void TaskIntersectTest() {
        Task task1 = new Task("Test task1", "Test task1 description", IN_PROGRESS,
                LocalDateTime.of(2020, Month.APRIL, 1, 11, 0), Duration.ofMinutes(20));
        final Long task1Id = taskManager.createTask(task1);

        Task task2 = new Task("Test task2", "Test task2 description", IN_PROGRESS,
                LocalDateTime.of(2020, Month.APRIL, 1, 11, 10), Duration.ofMinutes(20));
        assertThrows(CreateTaskException.class, () -> taskManager.createTask(task2));
        task2.setStartTime(LocalDateTime.of(2020, Month.APRIL, 1, 10, 50));
        assertThrows(CreateTaskException.class, () -> taskManager.createTask(task2));
        task2.setStartTime(LocalDateTime.of(2020, Month.APRIL, 1, 10, 40));
        taskManager.createTask(task2);

        task1 = taskManager.getTaskById(task1Id);
        Task task1Updated = new Task(task1Id, task1.getName(), task1.getDescription(),
                task1.getStatus(), task1.getStartTime(), task1.getDuration());
        taskManager.updateTask(task1Updated);
        assertEquals(taskManager.getTaskById(task1Id), task1Updated);
        Task task1Updated2 = new Task(task1Id, task1Updated.getName(), task1Updated.getDescription(),
                task1Updated.getStatus(),
                LocalDateTime.of(2020, Month.APRIL, 1, 10, 50),
                task1Updated.getDuration());
        assertThrows(UpdateTaskException.class, () -> taskManager.updateTask(task1Updated2));

        Epic epic1 = new Epic("Первый эпик", "111");
        taskManager.createEpic(epic1);

        SubTask subTask1Epic1 = new SubTask("Задача 1 первого эпика", "1_1", NEW, epic1.getId(),
                LocalDateTime.of(2020, Month.APRIL, 1, 11, 0),
                Duration.ofMinutes(20));
        assertThrows(CreateTaskException.class, () -> taskManager.createSubTask(subTask1Epic1));
        subTask1Epic1.setStartTime(LocalDateTime.of(2020, Month.APRIL, 1, 12, 0));
        taskManager.createSubTask(subTask1Epic1);

        assertEquals(3, taskManager.getPrioritizedTasks().size());
        taskManager.deleteAllTasks();
        assertEquals(1, taskManager.getPrioritizedTasks().size());
    }
}
