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
import kanban.model.Task;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerTasksTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubTasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void addTaskTest() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTaskList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void updateTaskTest() throws IOException, InterruptedException {
        Task task1 = new Task("Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Long task1Id = manager.createTask(task1);
        Task task2 = new Task(task1Id, "Test 2", "Testing task 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                Duration.ofMinutes(10));

        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTaskList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
        assertEquals("Testing task 2",
                tasksFromManager.getFirst().getDescription(),
                "Некорректное описание задачи");
        assertEquals(LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                tasksFromManager.getFirst().getStartTime(),
                "Некорректное время начало задачи");
        assertEquals(Duration.ofMinutes(10),
                tasksFromManager.getFirst().getDuration(),
                "Некорректное время выполнения задачи");
    }

    @Test
    public void IntersectTest() throws IOException, InterruptedException {
        Task task1 = new Task("Test 1", "Testing task 1",
                TaskStatus.NEW,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                Duration.ofMinutes(10));
        manager.createTask(task1);
        Task task2 = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 35),
                Duration.ofMinutes(10));

        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals(1, manager.getTaskList().size(), "Некорректное количество задач");

        task2.setStartTime(LocalDateTime.of(2024, Month.SEPTEMBER, 2, 15, 30));
        Long task2Id = manager.createTask(task2);
        Task task2Update = new Task(task2Id, "Test 2 updated", "Testing task 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 35),
                Duration.ofMinutes(10));
        taskJson = gson.toJson(task2Update);

        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals(2, manager.getTaskList().size(), "Некорректное количество задач");
        assertEquals("Test 2", manager.getTaskById(task2Id).getName());
    }

    @Test
    public void deleteTaskTest() throws IOException, InterruptedException {
        Long taskId = manager.createTask(new Task("Test 1", "Testing task 1",
                TaskStatus.NEW,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                Duration.ofMinutes(10)));

        assertEquals(1, manager.getTaskList().size(), "Некорректное количество задач");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(0, manager.getTaskList().size(), "Некорректное количество задач");
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getTasks() throws IOException, InterruptedException {
        Long task1Id = manager.createTask(new Task("Test 1", "Testing task 1",
                TaskStatus.NEW,
                LocalDateTime.of(2024, Month.SEPTEMBER, 1, 15, 30),
                Duration.ofMinutes(10)));
        Long task2Id = manager.createTask(new Task("Test 2", "Testing task 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, Month.SEPTEMBER, 2, 15, 30),
                Duration.ofMinutes(10)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task1Id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(manager.getTaskById(task1Id), gson.fromJson(response.body(), Task.class));

        client = HttpClient.newHttpClient();
        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(manager.getTaskList(), gson.fromJson(response.body(), new TaskListTypeToken().getType()));
    }

    static class TaskListTypeToken extends TypeToken<List<Task>> {
    }
}
