package kanban.model;

public class Node {
    private Task item;
    private Node next;
    private Node prev;

    public Node(Node prev, Task element, Node next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }

    public Task getItem() {
        return item;
    }

    public void setItem(Task item) {
        this.item = item;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }
}
