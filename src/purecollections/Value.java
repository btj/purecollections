package purecollections;

final class Value<T> implements Computation<T> {

	private final T value;
	
	public Value(T value) {
		this.value = value;
	}
	
	public T compute() {
		return value;
	}
	
}
