public class SubTask extends Task {
    private final Long epicId;

    public SubTask(String name, String description, TaskStatus status, Long epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Long getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + getEpicId() +
                '}';
    }
}
