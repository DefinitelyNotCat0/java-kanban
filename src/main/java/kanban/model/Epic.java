package kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class Epic extends Task {
    private static final TaskType TASK_TYPE = TaskType.EPIC;
    private final ArrayList<SubTask> subTaskList = new ArrayList<>();

    public Epic(Long id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
    }

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public ArrayList<SubTask> getSubTaskList() {
        return subTaskList;
    }

    @Override
    public TaskType getTaskType() {
        return TASK_TYPE;
    }

    @Override
    public LocalDateTime getStartTime() {
        return getSubTaskList().stream()
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public Duration getDuration() {
        return getSubTaskList().stream()
                .map(SubTask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public LocalDateTime getEndTime() {
        return getSubTaskList().stream()
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", taskType=" + getTaskType() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration().toMinutes() +
                ", endTime=" + getEndTime() +
                ", getSubTaskList=" + getSubTaskList() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Epic epic = (Epic) obj;
        return Objects.equals(super.getId(), epic.getId()) &&
                Objects.equals(super.getName(), epic.getName()) &&
                Objects.equals(super.getDescription(), epic.getDescription()) &&
                Objects.equals(super.getStatus(), epic.getStatus()) &&
                Arrays.equals(subTaskList.toArray(), epic.subTaskList.toArray());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();

        result = prime * result + subTaskList.hashCode();
        return result;
    }
}
