package kanban;

import kanban.manager.Managers;
import kanban.manager.TaskManager;
import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import kanban.model.TaskStatus;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        /* Тестирование */
        // Создадим 2 задачи
        Task task1 = new Task("Первая задача", "123", TaskStatus.IN_PROGRESS);
        Task task2 = new Task("Вторая задача", "890", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Создадим эпик с 2 подзадачами
        Epic epic1 = new Epic("Первый эпик", "111");
        taskManager.createEpic(epic1);

        SubTask subTask1Epic1 = new SubTask("Задача 1 первого эпика", "1_1", TaskStatus.NEW, epic1.getId());
        SubTask subTask2Epic1 = new SubTask("Задача 2 первого эпика", "1_2", TaskStatus.NEW, epic1.getId());
        taskManager.createSubTask(subTask1Epic1);
        taskManager.createSubTask(subTask2Epic1);

        // Создадим эпик с 1 подзадачей
        Epic secondEpic = new Epic("Второй эпик", "222");
        taskManager.createEpic(secondEpic);

        SubTask subTask1Epic2 = new SubTask("Задача 1 второго эпика", "2_1", TaskStatus.NEW, secondEpic.getId());
        taskManager.createSubTask(subTask1Epic2);

        // Получим несколько тикетов (для истории)
        taskManager.getTaskById(task1.getId());
        taskManager.getEpicById(secondEpic.getId());

        // Вывод
        printAllTasks(taskManager);

        // Обновление
        task1.setName("Первая задача (upd)");
        task1.setDescription("123--1");
        taskManager.updateTask(task1);

        task2.setName("Задача 2");
        task2.setDescription("aaaa");
        task2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task2);

        epic1.setName("Первый эпик updated");
        epic1.setDescription("111 --s");
        taskManager.updateEpic(epic1);

        subTask1Epic1.setName("Задача 1 первого эпика updated");
        subTask1Epic1.setDescription("1_1_1");
        subTask1Epic1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTask1Epic1);

        subTask1Epic2.setName("Задача 1 второго эпика upd");
        subTask1Epic2.setDescription("2_1_1");
        subTask1Epic2.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1Epic2);

        // Получим несколько тикетов (для истории)
        taskManager.getSubTaskById(subTask1Epic1.getId());
        taskManager.getSubTaskById(subTask1Epic1.getId());
        taskManager.getTaskById(task1.getId());

        // Вывод
        printAllTasks(taskManager);

        // Удаление
        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteEpicById(epic1.getId());

        // Вывод
        printAllTasks(taskManager);

        // Получим тикеты для истории (суммарно больше 10)
        taskManager.getTaskById(task1.getId());
        taskManager.getSubTaskById(subTask1Epic2.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getEpicById(secondEpic.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getSubTaskById(subTask1Epic2.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getEpicById(secondEpic.getId());

        // Вывод
        printAllTasks(taskManager);
    }

    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("=".repeat(100) + "\nПечатаем список эпиков:");
        System.out.println(taskManager.getEpicList());

        System.out.println("\nПечатаем список задач:");
        System.out.println(taskManager.getTaskList());

        System.out.println("\nПечатаем список подзадач:");
        System.out.println(taskManager.getSubTaskList());

        System.out.println("\nПечатаем историю:");
        System.out.println("Количество тикетов в истории: " + taskManager.getHistory().size());
        System.out.println(taskManager.getHistory());
    }
}
