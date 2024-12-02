package kanban.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import com.google.gson.Gson;
import kanban.HttpTaskServer;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;
import kanban.model.Task;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static kanban.model.TaskStatus.DONE;
import static kanban.model.TaskStatus.NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerPrioritizedTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubTasks();
        manager.deleteAllEpics();

        manager.createTask(new Task("Первая задача", "_1", TaskStatus.IN_PROGRESS,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 0),
                Duration.ofMinutes(10)));
        manager.createTask(new Task("Вторая задача", "_2", NEW,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 25),
                Duration.ofMinutes(20)));
        manager.createTask(new Task("Третья задача", "_3", DONE,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 10),
                Duration.ofMinutes(15)));

        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void getHistoryTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> responseTasks = gson.fromJson(response.body(), new HttpTaskManagerTasksTest.TaskListTypeToken().getType());
        List<Task> expectedTasks = manager.getPrioritizedTasks().stream()
                .map(task -> new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus()))
                .toList();
        assertEquals(200, response.statusCode());
        assertEquals(manager.getPrioritizedTasks().size(), responseTasks.size());
        assertEquals(expectedTasks, responseTasks);
    }
}