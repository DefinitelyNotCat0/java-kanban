package kanban.server;

import java.io.IOException;
import java.util.NoSuchElementException;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.HttpTaskServer;
import kanban.exception.CreateTaskException;
import kanban.exception.UpdateTaskException;
import kanban.manager.TaskManager;
import kanban.model.Task;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        Endpoint endpoint = getEndpoint(requestPath, exchange.getRequestMethod());
        Long taskId = null;

        if (Endpoint.GET_TASK_BY_ID.equals(endpoint) || Endpoint.DELETE_TASK.equals(endpoint)) {
            try {
                taskId = Long.parseLong(exchange.getRequestURI().getPath().split("/")[2]);
            } catch (Exception e) {
                sendError(exchange, "Некорректный идентификатор задачи", 400);
                return;
            }
        }

        try {
            switch (endpoint) {
                case GET_ALL_TASKS:
                    handleGetAllTasks(exchange);
                    break;
                case GET_TASK_BY_ID:
                    handleGetTaskById(exchange, taskId);
                    break;
                case POST_TASK:
                    handlePostTask(exchange);
                    break;
                case DELETE_TASK:
                    handleDeleteTaskById(exchange, taskId);
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
                return Endpoint.GET_ALL_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_TASK;
            }
        } else if (pathParts.length == 3) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }

        return Endpoint.UNKNOWN;
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getTaskList());
        sendSuccess(exchange, response);
    }

    private void handleGetTaskById(HttpExchange exchange, long taskId) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        String response = gson.toJson(taskManager.getTaskById(taskId));
        sendSuccess(exchange, response);
    }

    private void handleDeleteTaskById(HttpExchange exchange, long taskId) throws IOException {
        taskManager.deleteTaskById(taskId);
        sendSuccess(exchange, String.format("Задача %d удалена", taskId));
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();
        Task task = gson.fromJson(getRequestBody(exchange), Task.class);
        if (task.getId() == null) {
            taskManager.createTask(task);
        } else {
            taskManager.updateTask(task);
        }
        sendSuccess(exchange);
    }

    private enum Endpoint {
        GET_ALL_TASKS,
        GET_TASK_BY_ID,
        POST_TASK,
        DELETE_TASK,
        UNKNOWN
    }
}
