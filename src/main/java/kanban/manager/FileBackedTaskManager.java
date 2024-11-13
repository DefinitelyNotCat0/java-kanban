package kanban.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import kanban.exception.ManagerSaveException;
import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import kanban.model.TaskStatus;
import kanban.model.TaskType;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private static final String CSV_SEPARATOR = ",";
    private static final String FILE_HEADER = String.format(
            "id%1$stype%1$sname%1$sstatus%1$sdescription%1$sstartTime%1$sduration%1$sepic",
            CSV_SEPARATOR);
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            bufferedReader.readLine();

            while (bufferedReader.ready()) {
                Task task = fromString(bufferedReader.readLine());

                if (TaskType.SUBTASK.equals(task.getTaskType())) {
                    fileBackedTaskManager.createSubTask((SubTask) task);
                } else if (TaskType.EPIC.equals(task.getTaskType())) {
                    fileBackedTaskManager.createEpic((Epic) task);
                } else {
                    fileBackedTaskManager.createTask(task);
                }
            }
        } catch (IOException ioException) {
            throw new RuntimeException(ioException.getMessage());
        }

        return fileBackedTaskManager;
    }

    private static String toString(Task task) {
        StringBuilder stringBuilder = new StringBuilder();

        TaskType taskType = task.getTaskType();
        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String epicId = TaskType.SUBTASK.equals(taskType) ?
                String.valueOf(((SubTask) task).getEpicId()) : "";

        stringBuilder
                .append(task.getId()).append(CSV_SEPARATOR)
                .append(taskType).append(CSV_SEPARATOR)
                .append(task.getName()).append(CSV_SEPARATOR)
                .append(task.getStatus()).append(CSV_SEPARATOR)
                .append(task.getDescription()).append(CSV_SEPARATOR)
                .append(startTime).append(CSV_SEPARATOR)
                .append(task.getDuration().toMinutes()).append(CSV_SEPARATOR)
                .append(epicId);

        return stringBuilder.toString();
    }

    private static Task fromString(String value) {
        String[] parameters = value.split(CSV_SEPARATOR, -1);
        Long id = Long.valueOf(parameters[0]);
        TaskType taskType = TaskType.valueOf(parameters[1]);
        String name = parameters[2];
        TaskStatus taskStatus = TaskStatus.valueOf(parameters[3]);
        String description = parameters[4];
        LocalDateTime startTime = (parameters[5] != null && !parameters[5].isEmpty()) ? LocalDateTime.parse(parameters[5]) : null;
        Duration duration = Duration.ofMinutes(Long.parseLong(parameters[6]));


        Task task;
        if (TaskType.SUBTASK.equals(taskType)) {
            Long epicId = Long.valueOf(parameters[7]);
            task = new SubTask(id, name, description, taskStatus, epicId, startTime, duration);
        } else if (TaskType.EPIC.equals(taskType)) {
            task = new Epic(id, name, description);
        } else {
            task = new Task(id, name, description, taskStatus, startTime, duration);
        }

        return task;
    }

    @Override
    public Long createTask(Task task) {
        Long taskId = super.createTask(task);
        save();
        return taskId;
    }

    @Override
    public Long createEpic(Epic epic) {
        Long epicId = super.createEpic(epic);
        save();
        return epicId;
    }

    @Override
    public Long createSubTask(SubTask subTask) {
        Long subTaskId = super.createSubTask(subTask);
        save();
        return subTaskId;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteTaskById(Long id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(Long id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubTaskById(Long id) {
        super.deleteSubTaskById(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    private void save() {
        List<Task> allTasks = new ArrayList<>(super.getTaskList());
        allTasks.addAll(super.getEpicList());
        allTasks.addAll(super.getSubTaskList());

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(FILE_HEADER);

            for (Task task : allTasks) {
                bufferedWriter.write("\n" + toString(task));
            }

            bufferedWriter.flush();
        } catch (IOException ioException) {
            throw new ManagerSaveException(ioException.getMessage());
        }
    }
}
