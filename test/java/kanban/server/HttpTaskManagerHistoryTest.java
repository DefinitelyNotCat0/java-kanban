package kanban.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.google.gson.Gson;
import kanban.HttpTaskServer;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;
import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static kanban.model.TaskStatus.DONE;
import static kanban.model.TaskStatus.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerHistoryTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubTasks();
        manager.deleteAllEpics();

        manager.createTask(new Task("Первая задача", "_1", TaskStatus.IN_PROGRESS));
        manager.createTask(new Task("Вторая задача", "_2", NEW));
        manager.createTask(new Task("Третья задача", "_3", DONE));

        Epic epic1 = new Epic("Первый эпик", "111");
        manager.createEpic(epic1);
        manager.createSubTask(new SubTask("Задача 1 первого эпика", "1_1", NEW, epic1.getId()));
        manager.createSubTask(new SubTask("Задача 2 первого эпика", "1_2", NEW, epic1.getId()));

        Epic secondEpic = new Epic("Второй эпик", "222");
        manager.createEpic(secondEpic);
        manager.createSubTask(new SubTask("Задача 1 второго эпика", "2_1", NEW, secondEpic.getId()));

        manager.getTaskById(2L);
        manager.getEpicById(4L);
        manager.getTaskById(1L);
        manager.getSubTaskById(8L);

        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void getHistoryTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> responseTasks = gson.fromJson(response.body(), new HttpTaskManagerTasksTest.TaskListTypeToken().getType());
        List<Task> expectedTasks = manager.getHistory().stream()
                .map(task -> new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus()))
                .toList();
        assertEquals(200, response.statusCode());
        assertEquals(manager.getHistory().size(), responseTasks.size());
        assertEquals(expectedTasks, responseTasks);
    }
}
