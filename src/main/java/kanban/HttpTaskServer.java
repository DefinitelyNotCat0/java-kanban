package kanban;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import kanban.adapter.DurationTypeAdapter;
import kanban.adapter.LocalDateTimeTypeAdapter;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.Managers;
import kanban.manager.TaskManager;
import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import kanban.model.TaskStatus;
import kanban.server.EpicHandler;
import kanban.server.HistoryHandler;
import kanban.server.PrioritizedHandler;
import kanban.server.SubTaskHandler;
import kanban.server.TasksHandler;

import static kanban.model.TaskStatus.DONE;
import static kanban.model.TaskStatus.NEW;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public static void main(String[] args) throws IOException {
        TaskManager t = new InMemoryTaskManager();
        t = Managers.getDefault();
        t.createTask(new Task("Первая задача", "_1", TaskStatus.IN_PROGRESS));
        t.createTask(new Task("Вторая задача", "_2", NEW));
        t.createTask(new Task("Третья задача", "_3", DONE));

        Epic epic1 = new Epic("Первый эпик", "111");
        t.createEpic(epic1);
        t.createSubTask(new SubTask("Задача 1 первого эпика", "1_1", NEW, epic1.getId()));
        t.createSubTask(new SubTask("Задача 2 первого эпика", "1_2", NEW, epic1.getId()));

        Epic secondEpic = new Epic("Второй эпик", "222");
        t.createEpic(secondEpic);
        t.createSubTask(new SubTask("Задача 1 второго эпика", "2_1", NEW, secondEpic.getId()));
        new HttpTaskServer(t).start();
    }

    public static Gson getGson() {
        return new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }

    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

            httpServer.createContext("/tasks", new TasksHandler(taskManager));
            httpServer.createContext("/subtasks", new SubTaskHandler(taskManager));
            httpServer.createContext("/epics", new EpicHandler(taskManager));
            httpServer.createContext("/history", new HistoryHandler(taskManager));
            httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));

            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void stop() {
        httpServer.stop(0);
    }
}
