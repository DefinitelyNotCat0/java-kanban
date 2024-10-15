package kanban.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kanban.model.Node;
import kanban.model.Task;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Long, Node> taskNodeMap = new HashMap<>();

    private Node head;
    private Node tail;
    private int size = 0;

    // Добавляет узел с задачей в конец
    private Node linkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(oldTail, task, null);
        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.setNext(newNode);
        }
        size++;

        return newNode;
    }

    // Удаляем узел
    private void removeNode(Node node) {
        final Node next = node.getNext();
        final Node prev = node.getPrev();

        if (prev == null) {
            head = next;
        } else {
            prev.setNext(next);
            node.setPrev(null);
        }

        if (next == null) {
            tail = prev;
        } else {
            next.setPrev(prev);
            node.setNext(null);
        }

        node.setItem(null);
        size--;
    }

    // Собирает задачи из узлов в ArrayList
    private ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        Node currentNode = head;

        while (currentNode != null) {
            tasks.add(currentNode.getItem());
            currentNode = currentNode.getNext();
        }

        return tasks;
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            System.out.println("Пустая задача. Добавление в историю не произведено");
            return;
        }

        remove(task.getId());
        Node newNode = linkLast(task);
        taskNodeMap.put(task.getId(), newNode);
    }

    @Override
    public void remove(Long id) {
        Node node = taskNodeMap.get(id);

        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
