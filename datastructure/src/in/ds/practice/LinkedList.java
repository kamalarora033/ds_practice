package in.ds.practice;

public class LinkedList<T> {

	Node<T> head;

	static class Node<T> {
		Node<T> next;
		T data;

		public Node(T t) {
			this.data = t;
		}

	}

	public void printLinkedList() {
		Node<T> node = head;
		while (node != null) {
			System.out.println(node.data + "");
			node = node.next;
		}
	}

	public void pushFrontNode(int data) {
		Node<Integer> newNode = new Node<Integer>(data);
		newNode.next = (Node<Integer>) head;
		head = (Node<T>) newNode;

	}

	public void pushNodeAfterGivenNode(Node<Integer> headNode, int previous, int data) {

		if (headNode != null && headNode.next != null && headNode.data.equals(previous)) {
			Node<Integer> newNode = new Node<>(data);
			newNode.next = headNode.next;
			headNode.next = newNode;
			return;
		}
		pushNodeAfterGivenNode(headNode.next, previous, data);
	}

	public void appendNode(Node<Integer> headNode, int data) {
		if (headNode != null && headNode.next == null) {
			Node<Integer> newNode = new Node<>(data);
			headNode.next = newNode;
			return;
		}
		appendNode(headNode.next, data);
	}

	public void deleteNode(int data) {
		Node<Integer> headNode = (Node<Integer>) this.head;
		Node<Integer> prev = null;
		if (headNode != null && headNode.data.equals(data)) {
			headNode = headNode.next;
			return;
		}

		while (headNode != null && !headNode.data.equals(data)) {
			prev = headNode;
			headNode = headNode.next;
		}
		if (headNode == null) {
			return;
		}
		prev.next = headNode.next;
	}

	public static void main(String[] args) {
		LinkedList<Integer> lList = new LinkedList<>();

		lList.head = new Node<Integer>(1);
		Node<Integer> second = new Node<>(2);
		Node<Integer> third = new Node<>(3);

		lList.head.next = second;
		second.next = third;
		lList.pushFrontNode(7);
		lList.pushNodeAfterGivenNode(lList.head, 7, 10);
		lList.appendNode(lList.head, 15);
		lList.deleteNode(3);
		lList.printLinkedList();
	}

}
