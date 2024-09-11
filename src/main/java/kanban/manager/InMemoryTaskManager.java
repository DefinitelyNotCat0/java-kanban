package kanban.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import kanban.model.Epic;
import kanban.model.SubTask;
import kanban.model.Task;
import kanban.model.TaskStatus;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Long, Task> taskHashMap = new HashMap<>();
    private final HashMap<Long, Epic> epicHashMap = new HashMap<>();
    private final HashMap<Long, SubTask> subTaskHashMap = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private Long nextId = 1L;

    // Создать задачу
    @Override
    public Long createTask(Task task) {
        task.setId(getNextId());
        taskHashMap.put(task.getId(), task);
        return task.getId();
    }

    // Создать эпик
    @Override
    public Long createEpic(Epic epic) {
        epic.setId(getNextId());
        epicHashMap.put(epic.getId(), epic);
        return epic.getId();
    }

    // Создать подзадачу
    @Override
    public Long createSubTask(SubTask subTask) {
        if (!epicHashMap.containsKey(subTask.getEpicId())) {
            System.out.println("Невозможно связать подзадачу " + subTask.getId() + " с эпиком. Эпик с id " +
                    subTask.getEpicId() + " не найден. Подзадача не создана");
            return null;
        }

        subTask.setId(getNextId());
        subTaskHashMap.put(subTask.getId(), subTask);

        // Добавляем id в список подзадач эпика и проверяем статус
        Epic epic = epicHashMap.get(subTask.getEpicId());
        epic.getsubTaskList().add(subTask);
        epic.setStatus(getEpicStatus(subTask.getEpicId()));

        return subTask.getId();
    }

    // Удалить все задачи
    @Override
    public void deleteAllTasks() {
        taskHashMap.clear();
    }

    // Удалить все эпики (и все подзадачи)
    @Override
    public void deleteAllEpics() {
        epicHashMap.clear();
        subTaskHashMap.clear();
    }

    // Удалить все подзадачи
    @Override
    public void deleteAllSubTasks() {
        subTaskHashMap.clear();

        for (Epic epic : getEpicList()) {
            epic.setStatus(TaskStatus.NEW);
            epic.getsubTaskList().clear();
        }
    }

    // Получить задачу по идентификатору
    @Override
    public Task getTaskById(Long id) {
        Task task = taskHashMap.get(id);
        if (task == null) {
            System.out.printf("Задача с id %d не найдена\n", id);
            throw new NoSuchElementException();
        }

        historyManager.add(task);
        return task;
    }

    // Получить эпик по идентификатору
    @Override
    public Epic getEpicById(Long id) {
        Epic epic = epicHashMap.get(id);
        if (epic == null) {
            System.out.printf("Эпик с id %d не найден\n", id);
            throw new NoSuchElementException();
        }

        historyManager.add(epic);
        return epic;
    }

    // Получить подзадачу по идентификатору
    @Override
    public SubTask getSubTaskById(Long id) {
        SubTask subTask = subTaskHashMap.get(id);
        if (subTask == null) {
            System.out.printf("Подзадача с id %d не найдена\n", id);
            throw new NoSuchElementException();
        }

        historyManager.add(subTask);
        return subTask;
    }

    // Удалить задачу по идентификатору
    @Override
    public void deleteTaskById(Long id) {
        taskHashMap.remove(id);
    }

    // Удалить эпик по идентификатору
    @Override
    public void deleteEpicById(Long id) {
        // Удаляем связанные подзадачи
        for (SubTask subTask : epicHashMap.get(id).getsubTaskList()) {
            subTaskHashMap.remove(subTask.getId());
        }

        epicHashMap.remove(id);
    }

    // Удалить подзадачу по идентификатору
    @Override
    public void deleteSubTaskById(Long id) {
        Epic epic = epicHashMap.get(subTaskHashMap.get(id).getEpicId());
        epic.getsubTaskList().remove(subTaskHashMap.get(id));
        subTaskHashMap.remove(id);
        epic.setStatus(getEpicStatus(epic.getId()));
    }

    // Обновить задачу
    @Override
    public void updateTask(Task task) {
        if (!taskHashMap.containsKey(task.getId())) {
            System.out.println("Задача с id " + task.getId() + " не найдена. Обновление не применено");
            return;
        }

        // Обновляем задачу
        Task currentTask = taskHashMap.get(task.getId());
        currentTask.setName(task.getName());
        currentTask.setDescription(task.getDescription());
        currentTask.setStatus(task.getStatus());
    }

    // Обновить эпик
    @Override
    public void updateEpic(Epic epic) {
        if (!epicHashMap.containsKey(epic.getId())) {
            System.out.println("Эпик с id " + epic.getId() + " не найден. Обновление не применено");
            return;
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
            System.out.println("Подзадача с id " + subTask.getId() + " не найдена. Обновление не применено");
            return;
        }

        // Обновляем подзадачу
        SubTask currentSubTask = subTaskHashMap.get(subTask.getId());
        currentSubTask.setName(subTask.getName());
        currentSubTask.setDescription(subTask.getDescription());
        currentSubTask.setStatus(subTask.getStatus());

        Long oldEpicId = currentSubTask.getEpicId();
        Long newEpicId = subTask.getEpicId();
        if (!oldEpicId.equals(newEpicId)) {
            // У подзадачи поменялся эпик
            epicHashMap.get(oldEpicId).getsubTaskList().remove(currentSubTask);
            epicHashMap.get(newEpicId).getsubTaskList().add(currentSubTask);
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
        return epicHashMap.get(id).getsubTaskList();
    }

    // Получить историю просмотров задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Определяем статус эпика по его подзадачам
    private TaskStatus getEpicStatus(Long id) {
        ArrayList<SubTask> epicSubTaskList = getSubTaskListByEpicId(id);
        if (epicSubTaskList.size() == 0
                || epicSubTaskList.stream().allMatch(o -> TaskStatus.NEW.equals(o.getStatus()))) {
            return TaskStatus.NEW;
        } else if (epicSubTaskList.stream().allMatch(o -> TaskStatus.DONE.equals(o.getStatus()))) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }

    private Long getNextId() {
        return nextId++;
    }
}
