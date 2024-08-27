public class SubTask extends Task {
    private final Long epicId;

    public SubTask(Long id, String name, String description, TaskStatus status, Long epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Long getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                '}';
    }
}
