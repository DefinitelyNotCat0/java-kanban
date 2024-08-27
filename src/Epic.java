public class Epic extends Task {

    public Epic(Long id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
