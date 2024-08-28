import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Long> subTaskIdArrayList = new ArrayList<>();

    public Epic(Long id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
    }

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    public ArrayList<Long> getSubTaskIdArrayList() {
        return subTaskIdArrayList;
    }

    public void setSubTaskIdArrayList(ArrayList<Long> subTaskIdArrayList) {
        this.subTaskIdArrayList = subTaskIdArrayList;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", getSubTaskIdArrayList size=" + getSubTaskIdArrayList().size() +
                '}';
    }
}
