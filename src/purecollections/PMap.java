package purecollections;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A purely functional map.
 * <p>
 * Instances of this class are immutable. This class' implementations of the mutator methods of the {@link java.util.Map} interface are deprecated and throw {@link UnsupportedOperationException}.
 * </p>
 * <p>Functional counterparts of the mutator methods:</p>
 * <table border="1">
 * <tr><td><code><del>map.put(k, v);</del></code></td><td><code>map = map.plus(k, v);</code></td></tr>
 * <tr><td><code><del>map.putAll(m);</del></code></td><td><code>map = map.plusAll(m);</code></td></tr>
 * <tr><td><code><del>map.remove(k);</del></code></td><td><code>map = map.minus(k);</code></td></tr>
 * </table>
 * <p>There is only one empty instance of this class. It is returned by static method PMap.empty().</p>
 * <p>
 * To turn any {@link java.util.Map} m into a PMap, write PMap.empty().plusAll(m).
 * </p>
 * <p>
 * Null keys or values are not allowed.
 * </p>
 * <p>
 * We say a map is <i>well-hashed</i> if the hash function disperses the elements properly,
 * i.e. if the log(n) least significant bits of the elements' hash codes are distinct.
 * Most operations are O(log(n)) if the map is well-hashed, and O(n) otherwise.
 * The documentation for each operation specifies the time complexity for well-hashed maps and for non-well-hashed maps, in that order,
 * if different.
 * </p>
 * <p>
 * This class assumes that each object's hashCode() method is consistent with its equals() method, i.e.
 * if o1.equals(o2), then o1.hashCode() == o2.hashCode().
 * </p>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public abstract class PMap<K, V> implements Map<K, V> {
	
	PMap() {}

	/**
	 * The empty map.
	 * @param <K> the type of keys
	 * @param <V> the type of values
	 * @return the empty map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> PMap<K, V> empty() { return (PMap<K, V>)EmptyMap.instance; }
	
	abstract V get(int hash, Object key);
	abstract PMap<K, V> plus(int hash, K key, V value);
	abstract PMap<K, V> minus(int hash, K key);
	
	/**
	 * Returns <tt>true</tt> iff this map is empty. O(1).
	 */
	public abstract boolean isEmpty();
	
	/**
	 * Returns the value associated with the specified key, or null if this map does not have an entry for this key. O(log(n))/O(n).
	 * @throws NullPointerException if the specified key is null
	 */
	public final V get(Object key) {
		return get(key.hashCode(), key);
	}
	
	/**
	 * Returns a map whose entries are the union of the given entry and the entries of this map. O(log(n))/O(n).
	 * If there is already an entry for the given key in this map, the given value overrides the
	 * value in this map. 
	 * @throws NullPointerException if the specified key or value are null
	 */
	public final PMap<K, V> plus(K key, V value) {
		if (value == null)
			throw new NullPointerException();
		return plus(key.hashCode(), key, value);
	}
	
	/**
	 * Returns a map whose entries are the entries of this map minus the entry with the given key. O(log(n))/O(n).
	 * If there is no entry with the given key in this map, returns a map with the same entries as
	 * this map.
	 * @throws NullPointerException if the specified key is null
	 */
	public final PMap<K, V> minus(K key) {
		return minus(key.hashCode(), key);
	}
	
	/**
	 * Returns the size of this map. O(n).
	 */
	public abstract int size();
	
	abstract ICons<LeafMap<K, V>> toLazyList(Computation<ICons<LeafMap<K, V>>> cont);
	
	final ICons<LeafMap<K, V>> toLazyList() {
		return toLazyList(new Computation<ICons<LeafMap<K, V>>>() {

			@Override
			public ICons<LeafMap<K, V>> compute() {
				return null;
			}
			
		});
	}
	
	/**
	 * Returns this map's entries as a set of {@link java.util.Map.Entry} objects. O(1).
	 * The set's iterator() method is also O(1), and the iterator's hasNext() and next() methods are amortized O(1).
	 * All other methods are O(n). 
	 */
	public Set<Entry<K, V>> entrySet() {
		return new AbstractSet<Map.Entry<K, V>>() {

			@Override
			public Iterator<Entry<K, V>> iterator() {
				return LazyCons.<Entry<K, V>>iterator(LazyCons.<LeafMap<K, V>, Entry<K, V>>map(toLazyList(), new Mapper<LeafMap<K, V>, Entry<K, V>>() {

					@Override
					public Entry<K, V> map(final LeafMap<K, V> a) {
						return new AbstractMap.SimpleEntry<K, V>(a.key, a.value);
					}
					
				}));
			}

			@Override
			public int size() {
				return PMap.this.size();
			}
			
			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Entry))
					return false;
				@SuppressWarnings("unchecked")
				Entry<K, V> entry = (Entry<K, V>)o;
				return entry.getValue() != null && entry.getValue().equals(get(entry.getKey()));
			}
			
			@Override
			public boolean isEmpty() {
				return PMap.this.isEmpty();
			}
			
		};
	}
	
	/**
	 * Returns this map's keys. O(1).
	 * The set's iterator() method is also O(1), and the iterator's hasNext() and next() methods are amortized O(1).
	 * All other methods are O(n).
	 */
	public Set<K> keySet() {
		return new AbstractSet<K>() {

			@Override
			public Iterator<K> iterator() {
				return LazyCons.<K>iterator(LazyCons.<LeafMap<K, V>, K>map(toLazyList(), new Mapper<LeafMap<K, V>, K>() {

					@Override
					public K map(final LeafMap<K, V> a) {
						return a.key;
					}
					
				}));
			}

			@Override
			public int size() {
				return PMap.this.size();
			}
			
			@Override
			public boolean contains(Object o) {
				return o != null && containsKey(o);
			}
			
			@Override
			public boolean isEmpty() {
				return PMap.this.isEmpty();
			}
			
		};
	}
	
	/**
	 * Returns this map's values. O(1).
	 * The collection's iterator() method is also O(1),
	 * and the iterator's hasNext() and next() methods are amortized O(1).
	 * All other methods are O(n).
	 */
	public Collection<V> values() {
		return new AbstractCollection<V>() {

			@Override
			public Iterator<V> iterator() {
				return LazyCons.<V>iterator(LazyCons.<LeafMap<K, V>, V>map(toLazyList(), new Mapper<LeafMap<K, V>, V>() {

					@Override
					public V map(final LeafMap<K, V> a) {
						return a.value;
					}
					
				}));
			}

			@Override
			public int size() {
				return PMap.this.size();
			}
			
			@Override
			public boolean isEmpty() {
				return PMap.this.isEmpty();
			}
			
		};
	}

	/**
	 * Returns <tt>true</tt> iff this map contains a key that is equal to the specified object, per
	 * the specified object's equals method. O(log(n))/O(n).
	 * @throws NullPointerException if the specified key is null
	 */
	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	/**
	 * Returns <tt>true</tt> iff this map contains a value that is equal to the specified value, per
	 * the specified object's equals method. O(n).
	 * Returns <tt>false</tt> if the specified value is null.
	 */
	@Override
	public boolean containsValue(Object value) {
		return values().contains(value);
	}

	/**
	 * This method always throws UnsupportedOperationException.
	 */
	@Deprecated
	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException.
	 */
	@Deprecated
	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException.
	 */
	@Deprecated
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException.
	 */
	@Deprecated
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns this map plus the entries of the specified map. O(map.size() * log(n))/O(map.size() * n). 
	 * The entries of the specified map overwrite any existing entries with matching keys.
	 * @param map the map whose entries are to be added to this map
	 * @return this map plus the entries of the specified map
	 * @throws NullPointerException if the specified map is null
	 */
	public PMap<K, V> plusAll(Map<K, V> map) {
		PMap<K, V> result = this;
		for (Entry<K, V> e : map.entrySet())
			result = result.plus(e.getKey(), e.getValue());
		return result;
	}
	
	/**
	 * Returns a textual representation of this map, of the form {k1: v1, k2: v2, k3: v3}. O(n).
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		boolean first = true;
		for (Entry<K, V> entry : entrySet()) {
			if (first)
				first = false;
			else
				builder.append(", ");
			builder.append(entry.getKey());
			builder.append(": ");
			builder.append(entry.getValue());
		}
		builder.append("}");
		return builder.toString();
	}
	
	/**
	 * Returns <tt>true</tt> iff the specified object is a {@link java.util.Map} and its size equals this map's
	 * size and for each of this map's entries, the specified map contains an entry whose key and value are equal.
	 * <p>
	 * Complexity: If the specified object is a {@link PMap}, and either this map or the specified map is
	 * well-hashed: O(n); if neither map is well-hashed: O(n^2). If the specified object is not a {@link PMap}, then
	 * O(n * c), where c is the cost of the entry set's contains method.
	 * </p> 
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Map))
			return false;
		if (other instanceof PMap)
			return equals((PMap<?, ?>)other);
		return entrySet().equals(((Map<?, ?>)other).entrySet());
	}
	
	abstract boolean equals(PMap<?, ?> other);
	
}
