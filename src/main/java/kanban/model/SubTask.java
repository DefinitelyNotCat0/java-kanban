package kanban.model;

import java.util.Objects;

public class SubTask extends Task {
    private static final TaskType TASK_TYPE = TaskType.SUBTASK;
    private Long epicId;

    public SubTask(Long id, String name, String description, TaskStatus status, Long epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, TaskStatus status, Long epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TASK_TYPE;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + getId() +
                ", taskType=" + getTaskType() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + getEpicId() +
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
        SubTask subTask = (SubTask) obj;
        return Objects.equals(super.getId(), subTask.getId()) &&
                Objects.equals(super.getName(), subTask.getName()) &&
                Objects.equals(super.getDescription(), subTask.getDescription()) &&
                Objects.equals(super.getStatus(), subTask.getStatus()) &&
                Objects.equals(epicId, subTask.epicId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();

        result = prime * result + ((epicId == null) ? 0 : epicId.hashCode());
        return result;
    }
}
