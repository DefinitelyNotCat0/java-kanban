package kanban.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kanban.model.Task;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Long, Node> taskNodeMap = new HashMap<>();

    private Node head;
    private Node tail;
    private int size = 0;

    // Узел
    private static class Node {
        Task item;
        Node next;
        Node prev;

        Node(Node prev, Task element, Node next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    // Добавляет узел с задачей в конец
    private Node linkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(oldTail, task, null);
        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        size++;

        return newNode;
    }

    // Удаляем узел
    private void removeNode(Node node) {
        final Node next = node.next;
        final Node prev = node.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.item = null;
        size--;
    }

    // Собирает задачи из узлов в ArrayList
    private ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        Node currentNode = head;

        while (currentNode != null) {
            tasks.add(currentNode.item);
            currentNode = currentNode.next;
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
