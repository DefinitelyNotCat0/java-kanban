package kanban.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kanban.HttpTaskServer;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;
import kanban.model.Epic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerEpicsTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void addEpicTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 2", "Testing epic 2");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpicList();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 2", epicsFromManager.getFirst().getName(), "Некорректное имя эпика");
    }

    @Test
    public void updateEpicTest() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Testing epic 1");
        Long epic1Id = manager.createEpic(epic1);
        Epic epic2 = new Epic(epic1Id, "Epic 2", "Testing epic 2");

        String epicJson = gson.toJson(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpicList();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 2", epicsFromManager.getFirst().getName(), "Некорректное имя эпика");
        assertEquals("Testing epic 2",
                epicsFromManager.getFirst().getDescription(),
                "Некорректное описание эпика");
    }

    @Test
    public void deleteEpicTest() throws IOException, InterruptedException {
        Long epicId = manager.createEpic(new Epic("Epic 1", "Testing epic 1"));

        assertEquals(1, manager.getEpicList().size(), "Некорректное количество эпиков");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(0, manager.getEpicList().size(), "Некорректное количество эпиков");
        assertEquals(200, response.statusCode());
    }

    @Test
    public void getEpics() throws IOException, InterruptedException {
        Long epic1Id = manager.createEpic(new Epic("Epic 1", "Testing epic 1"));
        Long epic2Id = manager.createEpic(new Epic("Epic 2", "Testing epic 2"));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic1Id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(manager.getEpicById(epic1Id), gson.fromJson(response.body(), Epic.class));

        client = HttpClient.newHttpClient();
        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(manager.getEpicList(), gson.fromJson(response.body(), new EpicListTypeToken().getType()));
    }

    static class EpicListTypeToken extends TypeToken<List<Epic>> {
    }
}
