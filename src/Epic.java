import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Long> subTaskIdArrayList = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public ArrayList<Long> getSubTaskIdArrayList() {
        return subTaskIdArrayList;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", getSubTaskIdArrayList=" + getSubTaskIdArrayList() +
                '}';
    }
}
