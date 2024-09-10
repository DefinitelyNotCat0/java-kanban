package kanban.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Epic extends Task {
    private final ArrayList<SubTask> subTaskList = new ArrayList<>();

    public Epic(Long id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
    }

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public ArrayList<SubTask> getsubTaskList() {
        return subTaskList;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", getSubTaskIdArrayList=" + getsubTaskList() +
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
}
