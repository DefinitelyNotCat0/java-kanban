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
import com.google.gson.reflect.TypeToken;
import kanban.HttpTaskServer;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;
import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerSubTasksTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubTasks();
        manager.deleteAllEpics();
        manager.createEpic(new Epic("Epic 1", "Testing epic 1"));
        manager.createEpic(new Epic("Epic 2", "Testing epic 2"));
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void addSubTaskTest() throws IOException, InterruptedException {
        SubTask subTask = new SubTask("Test 2", "Testing subTask 2",
                TaskStatus.NEW, 1L, LocalDateTime.now(), Duration.ofMinutes(5));
        String subTaskJson = gson.toJson(subTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subTaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTaskList();

        assertNotNull(subTasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test 2", subTasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void updateSubTaskTest() throws IOException, InterruptedException {
        SubTask subTask1 = new SubTask("Test 1", "Testing subTask 1",
                TaskStatus.NEW, 1L, LocalDateTime.now(), Duration.ofMinutes(5));
        Long subTask1Id = manager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask(subTask1Id, "Test 2", "Testing subTask 2",
                TaskStatus.NEW, 1L,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                Duration.ofMinutes(10));

        String subTaskJson = gson.toJson(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subTaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTaskList();

        assertNotNull(subTasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test 2", subTasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
        assertEquals("Testing subTask 2",
                subTasksFromManager.getFirst().getDescription(),
                "Некорректное описание подзадачи");
        assertEquals(LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                subTasksFromManager.getFirst().getStartTime(),
                "Некорректное время начало подзадачи");
        assertEquals(Duration.ofMinutes(10),
                subTasksFromManager.getFirst().getDuration(),
                "Некорректное время выполнения подзадачи");
    }

    @Test
    public void IntersectTest() throws IOException, InterruptedException {
        SubTask subTask1 = new SubTask("Test 1", "Testing subTask 1",
                TaskStatus.NEW, 1L,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                Duration.ofMinutes(10));
        manager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Test 2", "Testing subTask 2",
                TaskStatus.NEW, 1L,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 35),
                Duration.ofMinutes(10));

        String subTaskJson = gson.toJson(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subTaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals(1, manager.getSubTaskList().size(), "Некорректное количество подзадач");

        subTask2.setStartTime(LocalDateTime.of(2024, Month.SEPTEMBER, 2, 15, 30));
        Long subTask2Id = manager.createSubTask(subTask2);
        SubTask subTask2Update = new SubTask(subTask2Id, "Test 2 updated", "Testing subTask 2",
                TaskStatus.NEW, 1L,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 35),
                Duration.ofMinutes(10));
        subTaskJson = gson.toJson(subTask2Update);

        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subTaskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals(2, manager.getSubTaskList().size(), "Некорректное количество подзадач");
        assertEquals("Test 2", manager.getSubTaskById(subTask2Id).getName());
    }

    @Test
    public void deleteSubTaskTest() throws IOException, InterruptedException {
        Long subTaskId = manager.createSubTask(new SubTask("Test 1", "Testing subTask 1",
                TaskStatus.NEW, 1L,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                Duration.ofMinutes(10)));

        assertEquals(1, manager.getSubTaskList().size(), "Некорректное количество подзадач");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subTaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(0, manager.getSubTaskList().size(), "Некорректное количество подзадач");
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getSubTasks() throws IOException, InterruptedException {
        Long subTask1Id = manager.createSubTask(new SubTask("Test 1", "Testing subTask 1",
                TaskStatus.NEW, 1L,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                Duration.ofMinutes(10)));
        manager.createSubTask(new SubTask("Test 2", "Testing subTask 2",
                TaskStatus.NEW, 1L,
                LocalDateTime.of(2024, Month.SEPTEMBER, 2, 15, 30),
                Duration.ofMinutes(10)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subTask1Id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(manager.getSubTaskById(subTask1Id), gson.fromJson(response.body(), SubTask.class));

        client = HttpClient.newHttpClient();
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(manager.getSubTaskList(), gson.fromJson(response.body(), new SubTaskListTypeToken().getType()));
    }

    static class SubTaskListTypeToken extends TypeToken<List<SubTask>> {
    }
}
