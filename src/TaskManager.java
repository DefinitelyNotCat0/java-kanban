import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TaskManager {
    private Long nextId = 1L;
    private final HashMap<Long, Task> taskHashMap = new HashMap<>();
    private final HashMap<Long, Epic> epicHashMap = new HashMap<>();
    private final HashMap<Long, SubTask> subTaskHashMap = new HashMap<>();

    // Создать задачу
    public void createTask(Task task) {
        task.setId(getNextId());
        taskHashMap.put(task.getId(), task);
    }

    // Создать эпик
    public void createEpic(Epic epic) {
        epic.setId(getNextId());
        epicHashMap.put(epic.getId(), epic);
    }

    // Создать подзадачу
    public void createSubTask(SubTask subTask) {
        if (!epicHashMap.containsKey(subTask.getEpicId())) {
            System.out.println("Невозможно связать подзадачу " + subTask.getId() + " с эпиком. Эпик с id " +
                    subTask.getEpicId() + " не найден. Подзадача не создана");
            return;
        }

        subTask.setId(getNextId());
        subTaskHashMap.put(subTask.getId(), subTask);

        // Добавляем id в список подзадач эпика и проверяем статус
        Epic epic = getEpicById(subTask.getEpicId());
        epic.getSubTaskIdArrayList().add(subTask.getId());
        epic.setStatus(getEpicStatus(subTask.getEpicId()));
    }

    // Удалить все задачи
    public void deleteAllTasks() {
        taskHashMap.clear();
    }

    // Удалить все эпики (и все подзадачи)
    public void deleteAllEpics() {
        epicHashMap.clear();
        subTaskHashMap.clear();
    }

    // Удалить все подзадачи
    public void deleteAllSubTasks() {
        subTaskHashMap.clear();

        for (Epic epic : getEpicList()) {
            epic.setStatus(TaskStatus.NEW);
            epic.getSubTaskIdArrayList().clear();
        }
    }

    // Получить задачу по идентификатору
    public Task getTaskById(Long id) {
        return taskHashMap.get(id);
    }

    // Получить эпик по идентификатору
    public Epic getEpicById(Long id) {
        return epicHashMap.get(id);
    }

    // Получить подзадачу по идентификатору
    public SubTask getSubTaskById(Long id) {
        return subTaskHashMap.get(id);
    }

    // Удалить задачу по идентификатору
    public void deleteTaskById(Long id) {
        taskHashMap.remove(id);
    }

    // Удалить эпик по идентификатору
    public void deleteEpicById(Long id) {
        // Удаляем связанные подзадачи
        ArrayList<Long> epicSubTaskIdList = new ArrayList<>(getEpicById(id).getSubTaskIdArrayList());
        for (Long subTaskId : epicSubTaskIdList) {
            deleteSubTaskById(subTaskId);
        }

        epicHashMap.remove(id);
    }

    // Удалить подзадачу по идентификатору
    public void deleteSubTaskById(Long id) {
        Epic epic = getEpicById(getSubTaskById(id).getEpicId());
        subTaskHashMap.remove(id);
        epic.getSubTaskIdArrayList().remove(id);
        epic.setStatus(getEpicStatus(epic.getId()));
    }

    // Обновить задачу
    public void updateTask(Task task) {
        if (!taskHashMap.containsKey(task.getId())) {
            System.out.println("Задча с id " + task.getId() + " не найдена. Обновление не применено");
            return;
        }
        taskHashMap.put(task.getId(), task);
    }

    // Обновить эпик
    public void updateEpic(Epic epic) {
        if (!epicHashMap.containsKey(epic.getId())) {
            System.out.println("Эпик с id " + epic.getId() + " не найден. Обновление не применено");
            return;
        }

        // Статус и список подзадач возьмем из эпика, который обновляем
        epic.setStatus(getEpicById(epic.getId()).getStatus());
        epic.setSubTaskIdArrayList(getEpicById(epic.getId()).getSubTaskIdArrayList());

        epicHashMap.put(epic.getId(), epic);
    }

    // Обновить позадачу
    public void updateSubTask(SubTask subTask) {
        if (!subTaskHashMap.containsKey(subTask.getId())) {
            System.out.println("Подзадача с id " + subTask.getId() + " не найдена. Обновление не применено");
            return;
        }
        subTaskHashMap.put(subTask.getId(), subTask);
        // Прооверяем статус эпика
        getEpicById(subTask.getEpicId()).setStatus(getEpicStatus(subTask.getEpicId()));
    }

    // Получить список задач
    public ArrayList<Task> getTaskList() {
        return new ArrayList<>(taskHashMap.values());
    }

    // Получить список эпиков
    public ArrayList<Epic> getEpicList() {
        return new ArrayList<>(epicHashMap.values());
    }

    // Получить список подзадач
    public ArrayList<SubTask> getSubTaskList() {
        return new ArrayList<>(subTaskHashMap.values());
    }

    // Получить список подзадач определенного эпика
    public ArrayList<SubTask> getSubTaskListByEpicId(Long id) {
        return subTaskHashMap.values()
                .stream()
                .filter(subTask -> getEpicById(id).getSubTaskIdArrayList()
                        .contains(subTask.getId())).collect(Collectors.toCollection(ArrayList::new));
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

    public Long getNextId() {
        return nextId++;
    }
}
