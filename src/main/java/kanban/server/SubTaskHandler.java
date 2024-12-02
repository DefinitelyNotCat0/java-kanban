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
import kanban.model.SubTask;

public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubTaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(requestPath, exchange.getRequestMethod());
        Long subTaskId = null;

        if (Endpoint.GET_SUBTASK_BY_ID.equals(endpoint) || Endpoint.DELETE_SUBTASK.equals(endpoint)) {
            try {
                subTaskId = Long.parseLong(exchange.getRequestURI().getPath().split("/")[2]);
            } catch (Exception e) {
                sendError(exchange, "Некорректный идентификатор подзадачи", 400);
                return;
            }
        }

        try {
            switch (endpoint) {
                case GET_ALL_SUBTASKS:
                    handleGetAllSubTasks(exchange);
                    break;
                case GET_SUBTASK_BY_ID:
                    handleGetSubTaskById(exchange, subTaskId);
                    break;
                case POST_SUBTASK:
                    handlePostSubTask(exchange);
                    break;
                case DELETE_SUBTASK:
                    handleDeleteSubTaskById(exchange, subTaskId);
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
                return Endpoint.GET_ALL_SUBTASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_SUBTASK;
            }
        } else if (pathParts.length == 3) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_SUBTASK;
            }
        }

        return Endpoint.UNKNOWN;
    }

    private void handleGetAllSubTasks(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getSubTaskList());
        sendSuccess(exchange, response);
    }

    private void handleGetSubTaskById(HttpExchange exchange, long subTaskId) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getSubTaskById(subTaskId));
        sendSuccess(exchange, response);
    }

    private void handleDeleteSubTaskById(HttpExchange exchange, long subTaskId) throws IOException {
        taskManager.deleteSubTaskById(subTaskId);
        sendSuccess(exchange, String.format("Подзадача %d удалена", subTaskId));
    }

    private void handlePostSubTask(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String requestBody = getRequestBody(exchange);
        if (requestBody == null || requestBody.isEmpty() || requestBody.length() == 2) {
            throw new EmptyRequestBodyException();
        }
        SubTask subTask = gson.fromJson(requestBody, SubTask.class);
        if (subTask.getId() == null) {
            taskManager.createSubTask(subTask);
        } else {
            taskManager.updateSubTask(subTask);
        }
        sendSuccess(exchange);
    }

    private enum Endpoint {
        GET_ALL_SUBTASKS,
        GET_SUBTASK_BY_ID,
        POST_SUBTASK,
        DELETE_SUBTASK,
        UNKNOWN
    }
}