package kanban.server;

import java.io.IOException;
import java.util.NoSuchElementException;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.HttpTaskServer;
import kanban.exception.CreateTaskException;
import kanban.exception.EmptyRequestBodyException;
import kanban.exception.UpdateTaskException;
import kanban.manager.TaskManager;
import kanban.model.Epic;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(requestPath, exchange.getRequestMethod());
        Long epicId = null;

        if (Endpoint.GET_EPIC_BY_ID.equals(endpoint) || Endpoint.DELETE_EPIC.equals(endpoint)) {
            try {
                epicId = Long.parseLong(exchange.getRequestURI().getPath().split("/")[2]);
            } catch (Exception e) {
                sendError(exchange, "Некорректный идентификатор подзадачи", 400);
                return;
            }
        }

        try {
            switch (endpoint) {
                case GET_ALL_EPICS:
                    handleGetAllEpics(exchange);
                    break;
                case GET_EPIC_BY_ID:
                    handleGetEpicById(exchange, epicId);
                    break;
                case POST_EPIC:
                    handlePostEpic(exchange);
                    break;
                case DELETE_EPIC:
                    handleDeleteEpicById(exchange, epicId);
                default:
                    sendNotFound(exchange, "Такого эндпоинта не существует");
            }
        } catch (NoSuchElementException e) {
            // 404
            sendNotFound(exchange, e.getMessage());
        } catch (UpdateTaskException | CreateTaskException e) {
            // 406
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            // 500
            sendInternalServerError(exchange, e.getMessage());
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_ALL_EPICS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_EPIC;
            }
        } else if (pathParts.length == 3) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPIC_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_EPIC;
            }
        }

        return Endpoint.UNKNOWN;
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getEpicList());
        sendSuccess(exchange, response);
    }

    private void handleGetEpicById(HttpExchange exchange, long epicId) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getEpicById(epicId));
        sendSuccess(exchange, response);
    }

    private void handleDeleteEpicById(HttpExchange exchange, long epicId) throws IOException {
        taskManager.deleteEpicById(epicId);
        sendSuccess(exchange, String.format("Эпик %d и его подзадачи удалены", epicId));
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String requestBody = getRequestBody(exchange);
        if (requestBody == null || requestBody.isEmpty() || requestBody.length() == 2) {
            throw new EmptyRequestBodyException();
        }
        Epic epic = gson.fromJson(requestBody, Epic.class);
        taskManager.createEpic(epic);
        sendSuccess(exchange);
    }

    private enum Endpoint {
        GET_ALL_EPICS,
        GET_EPIC_BY_ID,
        POST_EPIC,
        DELETE_EPIC,
        UNKNOWN
    }
}
