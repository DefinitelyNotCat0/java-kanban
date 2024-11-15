package kanban.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import kanban.exception.CreateTaskException;
import kanban.exception.UpdateTaskException;
import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import kanban.model.TaskStatus;
import kanban.model.TaskType;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Long, Task> taskHashMap = new HashMap<>();
    private final HashMap<Long, Epic> epicHashMap = new HashMap<>();
    private final HashMap<Long, SubTask> subTaskHashMap = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Set<Task> prioritizedTasks =
            new TreeSet<>(Comparator.comparing(Task::getStartTime));
    private Long nextId = 1L;

    // Создать задачу
    @Override
    public Long createTask(Task task) {
        if (task.getId() == null) {
            task.setId(getNextId());
        }

        if (doesTaskIntersect(task)) {
            throw new CreateTaskException(task, "Найдены пересечения. Задача не создана");
        }

        addPrioritizedTasks(task);
        taskHashMap.put(task.getId(), task);
        return task.getId();
    }

    // Создать эпик
    @Override
    public Long createEpic(Epic epic) {
        if (epic.getId() == null) {
            epic.setId(getNextId());
        }

        epicHashMap.put(epic.getId(), epic);
        return epic.getId();
    }

    // Создать подзадачу
    @Override
    public Long createSubTask(SubTask subTask) {
        if (!epicHashMap.containsKey(subTask.getEpicId())) {
            throw new CreateTaskException(subTask,
                    String.format("Невозможно связать подзадачу %d с эпиком. Эпик с id %d не найден. " +
                            "Подзадача не создана", subTask.getId(), subTask.getEpicId()));
        }

        if (doesTaskIntersect(subTask)) {
            throw new CreateTaskException(subTask, "Найдены пересечения. Подзадача не создана");
        }

        if (subTask.getId() == null) {
            subTask.setId(getNextId());
        }
        addPrioritizedTasks(subTask);
        subTaskHashMap.put(subTask.getId(), subTask);

        // Добавляем id в список подзадач эпика и проверяем статус
        Epic epic = epicHashMap.get(subTask.getEpicId());
        epic.getSubTaskList().add(subTask);
        epic.setStatus(getEpicStatus(subTask.getEpicId()));

        return subTask.getId();
    }

    // Удалить все задачи
    @Override
    public void deleteAllTasks() {
        taskHashMap.clear();
        new ArrayList<>(prioritizedTasks).forEach(task -> {
            if (TaskType.TASK.equals(task.getTaskType())) {
                prioritizedTasks.remove(task);
            }
        });
    }

    // Удалить все эпики (и все подзадачи)
    @Override
    public void deleteAllEpics() {
        epicHashMap.clear();
        subTaskHashMap.clear();
        new ArrayList<>(prioritizedTasks).forEach(epic -> {
            if (TaskType.SUBTASK.equals(epic.getTaskType())) {
                prioritizedTasks.remove(epic);
            }
        });
    }

    // Удалить все подзадачи
    @Override
    public void deleteAllSubTasks() {
        subTaskHashMap.clear();

        new ArrayList<>(prioritizedTasks).forEach(subTask -> {
            if (TaskType.SUBTASK.equals(subTask.getTaskType())) {
                prioritizedTasks.remove(subTask);
            }
        });

        getEpicList().forEach(epic -> {
            epic.setStatus(TaskStatus.NEW);
            epic.getSubTaskList().clear();
        });
    }

    // Получить задачу по идентификатору
    @Override
    public Task getTaskById(Long id) {
        Task task = taskHashMap.get(id);
        if (task == null) {
            throw new NoSuchElementException(String.format("Задача с id %d не найдена", id));
        }

        historyManager.add(task);
        return task;
    }

    // Получить эпик по идентификатору
    @Override
    public Epic getEpicById(Long id) {
        Epic epic = epicHashMap.get(id);
        if (epic == null) {
            throw new NoSuchElementException(String.format("Эпик с id %d не найден", id));
        }

        historyManager.add(epic);
        return epic;
    }

    // Получить подзадачу по идентификатору
    @Override
    public SubTask getSubTaskById(Long id) {
        SubTask subTask = subTaskHashMap.get(id);
        if (subTask == null) {
            throw new NoSuchElementException(String.format("Подзадача с id %d не найдена", id));
        }

        historyManager.add(subTask);
        return subTask;
    }

    // Удалить задачу по идентификатору
    @Override
    public void deleteTaskById(Long id) {
        prioritizedTasks.remove(taskHashMap.get(id));
        taskHashMap.remove(id);
    }

    // Удалить эпик по идентификатору
    @Override
    public void deleteEpicById(Long id) {
        // Удаляем связанные подзадачи
        epicHashMap.get(id).getSubTaskList()
                .forEach(subTask -> subTaskHashMap.remove(subTask.getId()));

        epicHashMap.remove(id);
    }

    // Удалить подзадачу по идентификатору
    @Override
    public void deleteSubTaskById(Long id) {
        Epic epic = epicHashMap.get(subTaskHashMap.get(id).getEpicId());
        epic.getSubTaskList().remove(subTaskHashMap.get(id));
        prioritizedTasks.remove(subTaskHashMap.get(id));
        subTaskHashMap.remove(id);
        epic.setStatus(getEpicStatus(epic.getId()));
    }

    // Обновить задачу
    @Override
    public void updateTask(Task task) {
        if (!taskHashMap.containsKey(task.getId())) {
            throw new NoSuchElementException(
                    String.format("Задача с id %d не найдена. Обновление не применено", task.getId()));
        }

        if (doesTaskIntersect(task)) {
            throw new UpdateTaskException(task, "Найдены пересечения. Задача не обновлена");
        }

        // Обновляем задачу
        Task currentTask = taskHashMap.get(task.getId());
        currentTask.setName(task.getName());
        currentTask.setDescription(task.getDescription());
        currentTask.setStatus(task.getStatus());
        currentTask.setStartTime(task.getStartTime());
        currentTask.setDuration(task.getDuration());
    }

    // Обновить эпик
    @Override
    public void updateEpic(Epic epic) {
        if (!epicHashMap.containsKey(epic.getId())) {
            throw new NoSuchElementException(
                    String.format("Эпик с id %d не найден. Обновление не применено", epic.getId()));
        }

        // Обновляем эпик
        Epic currentEpic = epicHashMap.get(epic.getId());
        currentEpic.setName(epic.getName());
        currentEpic.setDescription(epic.getDescription());
    }

    // Обновить позадачу
    @Override
    public void updateSubTask(SubTask subTask) {
        if (!subTaskHashMap.containsKey(subTask.getId())) {
            throw new NoSuchElementException(
                    String.format("Подзадача с id %d не найдена. Обновление не применено", subTask.getId()));
        }

        if (doesTaskIntersect(subTask)) {
            throw new UpdateTaskException(subTask, "Найдены пересечения. Подзадача не обновлена");
        }

        // Обновляем подзадачу
        SubTask currentSubTask = subTaskHashMap.get(subTask.getId());
        currentSubTask.setName(subTask.getName());
        currentSubTask.setDescription(subTask.getDescription());
        currentSubTask.setStatus(subTask.getStatus());
        currentSubTask.setStartTime(subTask.getStartTime());
        currentSubTask.setDuration(subTask.getDuration());

        Long oldEpicId = currentSubTask.getEpicId();
        Long newEpicId = subTask.getEpicId();
        if (!oldEpicId.equals(newEpicId)) {
            // У подзадачи поменялся эпик
            epicHashMap.get(oldEpicId).getSubTaskList().remove(currentSubTask);
            epicHashMap.get(newEpicId).getSubTaskList().add(currentSubTask);
            currentSubTask.setEpicId(newEpicId);

            epicHashMap.get(oldEpicId).setStatus(getEpicStatus(oldEpicId));
        }

        // Прооверяем статус эпика
        epicHashMap.get(newEpicId).setStatus(getEpicStatus(newEpicId));
    }

    // Получить список задач
    @Override
    public ArrayList<Task> getTaskList() {
        return new ArrayList<>(taskHashMap.values());
    }

    // Получить список эпиков
    @Override
    public ArrayList<Epic> getEpicList() {
        return new ArrayList<>(epicHashMap.values());
    }

    // Получить список подзадач
    @Override
    public ArrayList<SubTask> getSubTaskList() {
        return new ArrayList<>(subTaskHashMap.values());
    }

    // Получить список подзадач определенного эпика
    @Override
    public ArrayList<SubTask> getSubTaskListByEpicId(Long id) {
        return epicHashMap.get(id).getSubTaskList();
    }

    // Получить историю просмотров задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Определяем статус эпика по его подзадачам
    private TaskStatus getEpicStatus(Long id) {
        ArrayList<SubTask> epicSubTaskList = getSubTaskListByEpicId(id);
        if (epicSubTaskList.isEmpty()
                || epicSubTaskList.stream().allMatch(o -> TaskStatus.NEW.equals(o.getStatus()))) {
            return TaskStatus.NEW;
        } else if (epicSubTaskList.stream().allMatch(o -> TaskStatus.DONE.equals(o.getStatus()))) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }

    private void addPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    // Проверить наличие пересечений
    private boolean doesTaskIntersect(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                // Не проверяем пересечения с той же задачей, которую обновлеям
                .filter(prioritizedTask -> !prioritizedTask.getId().equals(task.getId()))
                .anyMatch(prioritizedTask ->
                        (task.getStartTime().equals(prioritizedTask.getStartTime())
                                && task.getEndTime().equals(prioritizedTask.getEndTime())) ||
                                (task.getStartTime().isAfter(prioritizedTask.getStartTime())
                                        && task.getStartTime().isBefore(prioritizedTask.getEndTime())) ||
                                (task.getEndTime().isAfter(prioritizedTask.getStartTime())
                                        && task.getEndTime().isBefore(prioritizedTask.getEndTime())));
    }

    private Long getNextId() {
        return nextId++;
    }
}
