package purecollections;

final class EmptyMap<K, V> extends PMap<K, V> {

	static final EmptyMap<Object, Object> instance = new EmptyMap<Object, Object>();
	
	private EmptyMap() {}

	@Override
	public boolean isEmpty() { return true; }
	
	@Override
	V get(int hash, Object key) {
		return null;
	}

	@Override
	PMap<K, V> plus(int hash, K key, V value) {
		return new LeafMap<K, V>(key, value);
	}

	@Override
	PMap<K, V> minus(int hash, K key) {
		return this;
	}
	
	@Override
	public int size() { return 0; }

	@Override
	ICons<LeafMap<K, V>> toLazyList(Computation<ICons<LeafMap<K, V>>> cont) {
		return cont.compute();
	}
	
	@Override
	boolean equals(PMap<?, ?> other) {
		return other == instance;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
}
