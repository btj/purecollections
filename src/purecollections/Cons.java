package purecollections;

final class Cons<T> extends ICons<T> {
	
	private final T head;
	private final Cons<T> tail;
	
	public Cons(T head, Cons<T> tail) {
		this.head = head;
		this.tail = tail;
	}
	
	public T getHead() { return head; }
	
	public Cons<T> getTail() { return tail; }

	public Computation<ICons<T>> getTailThunk() { return new Value<ICons<T>>(tail); }
	
}
