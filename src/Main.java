public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        /* Тестирование */
        // Создадим 2 задачи
        Task task1 = new Task(taskManager.getNextId(), "Первая задача", "123", TaskStatus.IN_PROGRESS);
        Task task2 = new Task(taskManager.getNextId(), "Вторая задача", "890", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Создадим эпик с 2 подзадачами
        Epic epic1 = new Epic(taskManager.getNextId(), "Первый эпик", "111");
        SubTask subTask1Epic1 = new SubTask(taskManager.getNextId(), "Задача 1 первого эпика",
                "1_1", TaskStatus.NEW, epic1.getId());
        SubTask subTask2Epic1 = new SubTask(taskManager.getNextId(), "Задача 2 первого эпика",
                "1_2", TaskStatus.NEW, epic1.getId());
        taskManager.createEpic(epic1);
        taskManager.createSubTask(subTask1Epic1);
        taskManager.createSubTask(subTask2Epic1);

        // Создадим эпик с 1 подзадачей
        Epic secondEpic = new Epic(taskManager.getNextId(), "Второй эпик", "222");
        SubTask subTask1Epic2 = new SubTask(taskManager.getNextId(), "Задача 1 второго эпика",
                "2_1", TaskStatus.NEW, secondEpic.getId());
        taskManager.createEpic(secondEpic);
        taskManager.createSubTask(subTask1Epic2);

        // Вывод
        System.out.println("Печатакем список эпиков:");
        System.out.println(taskManager.getEpicList());

        System.out.println("\nПечатакем список задач:");
        System.out.println(taskManager.getTaskList());

        System.out.println("\nПечатакем список подзадач:");
        System.out.println(taskManager.getSubTaskList());

        // Обновление
        taskManager.updateTask(new Task(task1.getId(), "Первая задача (upd)", "123--1", TaskStatus.NEW));
        taskManager.updateTask(new Task(task2.getId(), "Задача 2", "aaaa", TaskStatus.DONE));

        taskManager.updateEpic(new Epic(epic1.getId(), "Первый эпик updated", "111 --s"));

        taskManager.updateSubTask(new SubTask(subTask1Epic1.getId(), "Задача 1 первого эпика updated",
                "1_1_1", TaskStatus.IN_PROGRESS, epic1.getId()));
        taskManager.updateSubTask(new SubTask(subTask1Epic2.getId(), "Задача 1 второго эпика upd",
                "2_1_1", TaskStatus.DONE, secondEpic.getId()));

        // Удалении
        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteEpicById(epic1.getId());
    }
}
