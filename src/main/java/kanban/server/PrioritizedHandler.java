package kanban.server;

import java.io.IOException;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.HttpTaskServer;
import kanban.manager.TaskManager;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Gson gson = HttpTaskServer.getGson();
            String response = gson.toJson(taskManager.getPrioritizedTasks());
            sendSuccess(exchange, response);
        } catch (Exception e) {
            sendInternalServerError(exchange, e.getMessage());
        }
    }
}
