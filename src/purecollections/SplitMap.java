package purecollections;

final class SplitMap<K, V> extends PMap<K, V> {
	
	int mask; // The decision bit
	int hash; // The common prefix
	final PMap<K, V> left;
	final PMap<K, V> right;

	SplitMap(int mask, int hash, PMap<K, V> left, PMap<K, V> right) {
		this.mask = mask;
		this.hash = hash;
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean isEmpty() { return false; }
	
	@Override
	V get(int hash, Object key) {
		if ((hash & (this.mask - 1)) != this.hash)
			return null;
		else if ((hash & this.mask) == 0)
			return left.get(hash, key);
		else
			return right.get(hash, key);
	}

	@Override
	PMap<K, V> plus(int hash, K key, V value) {
		if ((hash & (this.mask - 1)) == this.hash) { // Target node is below this node.
			if ((hash & this.mask) == 0)
				return new SplitMap<K, V>(this.mask, this.hash, left.plus(hash, key, value), right);
			else
				return new SplitMap<K, V>(this.mask, this.hash, left, right.plus(hash, key, value));
		} else {
			int hashDiff = hash ^ this.hash;
			int mask = hashDiff & -hashDiff; // Least significant bit of hash difference
			if ((hash & mask) == 0)
				return new SplitMap<K, V>(mask, hash & (mask - 1), new LeafMap<K, V>(key, value), this);
			else
				return new SplitMap<K, V>(mask, hash & (mask - 1), this, new LeafMap<K, V>(key, value));
		}
	}

	@Override
	PMap<K, V> minus(int hash, K key) {
		if ((hash & (this.mask - 1)) != this.hash)
			return this;
		else if ((hash & this.mask) == 0) {
			PMap<K, V> newLeft = left.minus(hash, key);
			if (newLeft.isEmpty())
				return right;
			else
				return new SplitMap<K, V>(this.mask, this.hash, newLeft, right);
		} else {
			PMap<K, V> newRight = right.minus(hash, key);
			if (newRight.isEmpty())
				return left;
			else
				return new SplitMap<K, V>(this.mask, this.hash, left, newRight);
		}
	}

	@Override
	public int size() { return left.size() + right.size(); }

	@Override
	ICons<LeafMap<K, V>> toLazyList(final Computation<ICons<LeafMap<K, V>>> cont) {
		return left.toLazyList(new Computation<ICons<LeafMap<K, V>>>() {

			@Override
			public ICons<LeafMap<K, V>> compute() {
				return right.toLazyList(cont);
			}
			
		});
	}
	
	@Override
	boolean equals(PMap<?, ?> other) {
		if (!(other instanceof SplitMap))
			return false;
		SplitMap<?, ?> otherMap = (SplitMap<?, ?>)other;
		return mask == otherMap.mask && hash == otherMap.hash && left.equals(otherMap.left) && right.equals(otherMap.right);
	}
	
	@Override
	public int hashCode() {
		return left.hashCode() + right.hashCode();
	}
	
}
