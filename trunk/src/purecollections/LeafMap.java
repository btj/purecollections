package purecollections;

final class LeafMap<K, V> extends PMap<K, V> {

	final K key;
	final V value;
	final PMap<K, V> next;
	
	LeafMap(K key, V value, PMap<K, V> next) {
		this.key = key;
		this.value = value;
		this.next = next;
	}
	
	LeafMap(K key, V value) {
		this.key = key;
		this.value = value;
		this.next = PMap.<K, V>empty();
	}

	@Override
	public boolean isEmpty() { return false; }
	
	@Override
	V get(int hash, Object key) {
		return key.equals(this.key) ? value : next.get(key); 
	}

	@Override
	PMap<K, V> plus(int hash, K key, V value) {
		int myHash = this.key.hashCode();
		int hashDiff = myHash ^ hash;
		int mask = hashDiff & -hashDiff; // Least significant bit of hash difference
		if (mask == 0) {
			if (key.equals(this.key))
				return new LeafMap<K, V>(key, value, next);
			else
				return new LeafMap<K, V>(this.key, this.value, next.plus(hash, key, value));
		} else {
			if ((myHash & mask) == 0)
				return new SplitMap<K, V>(mask, hash & (mask - 1), this, new LeafMap<K, V>(key, value));
			else
				return new SplitMap<K, V>(mask, hash & (mask - 1), new LeafMap<K, V>(key, value), this);
		}
	}

	@Override
	public PMap<K, V> minus(int hash, K key) {
		if (key.equals(this.key))
			return next;
		else
			return new LeafMap<K, V>(this.key, this.value, next.minus(hash, key));
	}

	@Override
	public int size() { return 1 + next.size(); }

	@Override
	ICons<LeafMap<K, V>> toLazyList(final Computation<ICons<LeafMap<K, V>>> cont) {
		return new LazyCons<LeafMap<K, V>>(this, new Computation<ICons<LeafMap<K, V>>>() {

			@Override
			public ICons<LeafMap<K, V>> compute() {
				return next.toLazyList(cont);
			}
			
		});
	}
	
	@Override
	boolean equals(PMap<?, ?> other) {
		if (!(other instanceof LeafMap))
			return false;
		LeafMap<?, ?> otherLeaf = (LeafMap<?, ?>)other;
		if (otherLeaf.size() != size())
			return false;
		LeafMap<K, V> map = this;
		for (;;) {
			if (!map.value.equals(otherLeaf.get(map.key)))
				return false;
			if (map.next.isEmpty())
				return true;
			map = (LeafMap<K, V>)map.next;
		}
	}
	
	@Override
	public int hashCode() {
		return (key.hashCode() ^ value.hashCode()) + next.hashCode();
	}
	
}
