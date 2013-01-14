package trxsys.transaction;

public class Node {
	public final int value;
	public Node next;
	
	public Node(int value) {
		this.value = value; 
	}
	
	public Node(int value, Node next) {
		this(value);
		this.next = next;
	}
	
	public String asString() {
		return toString()+"["+value+"]->"+(next != null ? next.asString() : "null");
	}
}
