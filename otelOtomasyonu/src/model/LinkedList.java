package model;

public class LinkedList<T> {

    private class Node {
        T data;
        Node next;

        Node(T data) { this.data = data; }
    }

    private Node head;

    public void add(T data) {
        Node node = new Node(data);
        if (head == null) {
            head = node;
            return;
        }
        Node temp = head;
        while (temp.next != null) temp = temp.next;
        temp.next = node;
    }

    public Node getHead() {
        return head;
    }
}
