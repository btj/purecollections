package purecollections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A purely functional list with constant-time plus, plusFirst, getFirst, and minusFirst operations.
 * The iterator() method and the iterator object's hasNext() and next() methods are also constant-time.
 * <p>
 * Most other methods, including get(int) and with(int, T) (the functional counterpart of set(int, T)) are linear-time.
 * </p>
 * <p>
 * Instances of this class are immutable. This class' implementations of the mutator methods of the {@link java.util.List} interface are deprecated and throw {@link UnsupportedOperationException}.
 * </p>
 * <p>Functional counterparts of the mutator methods:</p>
 * <table border="1">
 * <tr><td><code><del>list.add(x);</del></code></td><td><code>list = list.plus(x);</code></td></tr>
 * <tr><td><code><del>list.add(i, x);</del></code></td><td><code>list = list.plus(i, x);</code></td></tr>
 * <tr><td><code><del>list.addAll(c);</del></code></td><td><code>list = list.plusAll(c);</code></td></tr>
 * <tr><td><code><del>list.remove(x);</del></code></td><td><code>list = list.minus(x);</code></td></tr>
 * <tr><td><code><del>list.remove(i);</del></code></td><td><code>list = list.minus(i);</code></td></tr>
 * <tr><td><code><del>list.removeAll(c);</del></code></td><td><code>list = list.minusAll(c);</code></td></tr>
 * <tr><td><code><del>list.set(i, x);</del></code></td><td><code>list = list.with(i, x);</code></td></tr>
 * </table>
 * <p>There is only one empty instance of this class. It is returned by static method PList.empty().</p>
 * <p>
 * To turn any collection c into a PList, write PList.empty().plusAll(c).
 * </p>
 * <p>
 * Null elements are not allowed.
 * </p>
 * <p>
 * This is an implementation of the queue data structure described in Chris Okasaki, <i>Simple and Efficient Purely Functional Queues and Deques</i>, Journal of Functional Programming 5(4), October 1995.
 * </p>
 * @param <T> element type
 */
public final class PList<T> implements List<T> {
	
	private static final PList<Object> emptyInstance = new PList<Object>(null, null, null);
	
	/**
	 * The empty list.
	 * @param <T> element type
	 * @return the empty list
	 */
	@SuppressWarnings("unchecked")
	public static <T> PList<T> empty() { return (PList<T>)emptyInstance; }
	
	private final ICons<T> left;
	private final Cons<T> right;
	private final ICons<T> leftTail;
	
	private PList(ICons<T> left, Cons<T> right, ICons<T> leftTail) {
		this.left = left;
		this.right = right;
		this.leftTail = leftTail;
	}
	
	/**
	 * Returns true iff this list is empty. O(1). 
	 */
	@Override
	public boolean isEmpty() {
		return left == null;
	}

	/**
	 * Returns the first element of this list. O(1).
	 * @return the first element of this list
	 * @throws NoSuchElementException if this list is empty
	 */
	public T getFirst() {
		if (left == null)
			throw new NoSuchElementException();
		return left.getHead();
	}
	
	/**
	 * Returns this list with the first element removed. O(1).
	 * @return this list minus the first element
	 * @throws NoSuchElementException if this list is empty
	 */
	public PList<T> minusFirst() {
		if (left == null)
			throw new NoSuchElementException();
		return make(left.getTail(), right, leftTail);
	}
	
	static final class PListIterator<T> implements Iterator<T> {
		
		PList<T> remaining;
		
		public PListIterator(PList<T> list) {
			remaining = list;
		}
		
		public boolean hasNext() {
			return !remaining.isEmpty();
		}
		
		public T next() {
			T result = remaining.getFirst();
			remaining = remaining.minusFirst();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	/**
	 * Returns an iterator over the elements in this list. O(1).
	 * <p>
	 * The iterator's hasNext and next methods are also O(1).
	 * </p> 
	 */
	@Override
	public Iterator<T> iterator() {
		return new PListIterator<T>(this);
	}
	
	/**
	 * Returns this list with the specified element appended to the end. O(1).
	 * @param element the element to append to this list
	 * @return the list obtained by appending the specified element to the end of this list
	 * @throws NullPointerException if the element is null
	 */
	public PList<T> plus(T element) {
		if (element == null)
			throw new NullPointerException();
		return make(left, new Cons<T>(element, right), leftTail);
	}
	
	private static <T> PList<T> make(ICons<T> left, Cons<T> right, ICons<T> leftTail) {
		if (left == null && right == null)
			return empty();
		if (leftTail == null) {
			ICons<T> newLeft = rotate(left, right, null);
			return new PList<T>(newLeft, null, newLeft);
		} else {
			return new PList<T>(left, right, leftTail.getTail());
		}
	}
	
	private static <T> ICons<T> rotate(ICons<T> left, final Cons<T> right, final Cons<T> accumulator) {
		if (left == null) {
			if (right == null)
				return accumulator;
			else
				return new Cons<T>(right.getHead(), accumulator);
		} else {
			final Computation<ICons<T>> tail = left.getTailThunk();
			return new LazyCons<T>(left.getHead(), new Computation<ICons<T>>() {
				@Override
				public ICons<T> compute() {
					if (right == null)
						return rotate(tail.compute(), null, accumulator);
					else
						return rotate(tail.compute(), right.getTail(), new Cons<T>(right.getHead(), accumulator));
				}
			});
		}
	}
	
	/**
	 * Returns this list with the elements of the specified collection appended to the end.  
	 * @param c the collection whose elements to append to this list
	 * @return this list with the elements of the specified collection appended to the end
	 * @throws NullPointerException if the specified collection is null
	 * @throws NullPointerException if any of the elements of the specified collection are null
	 */
	public PList<T> plusAll(Collection<T> c) {
		PList<T> list = this;
		for (T o : c)
			list = list.plus(o);
		return list;
	}
	
	/**
	 * Returns this list with the specified element appended to the front.
	 * @param element the element to append to the front
	 * @return this list with the specified element appended to the front
	 * @throws NullPointerException if the specified element is null
	 */
	public PList<T> plusFirst(T element) {
		if (element == null)
			throw new NullPointerException();
		return new PList<T>(new LazyCons<T>(element, new Value<ICons<T>>(left)), right, leftTail);
	}
	
	/**
	 * Returns this list with the specified element inserted at the specified index.
	 * @param index index at which the specified element is to be inserted
	 * @param element element to be inserted
	 * @return this list with the specified element inserted at the specified index
	 * @throws NullPointerException if the specified element is null
	 */
	public PList<T> plus(int index, T element) {
		if (index == 0) {
			return plusFirst(element);
		} else {
 			PList<T> result = PList.empty();
 			PList<T> list = this;
 			for (;;) {
 				if (index == 0)
 					return result.plus(element).plusAll(list);
 				if (list.isEmpty())
 					throw new IndexOutOfBoundsException();
 				result = result.plus(list.getFirst());
 				list = list.minusFirst();
 				index--;
 			}
		}
	}
	
	/**
	 * Returns this list minus the first element that is equal to the specified object. O(n).
	 * Returns this list if the specified object is null.
	 * @param o the object to remove
	 * @return this list minus the first element that is equal to the specified object
	 */
	public PList<T> minus(T o) {
		if (o == null)
			return this;
		PList<T> result = PList.empty();
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst()) {
			if (o.equals(list.getFirst()))
				return result.plusAll(list.minusFirst());
			result = result.plus(list.getFirst());
		}
		return this;
	}
	
	/**
	 * Returns this list minus the element at the specified index. O(n).
	 * @throws IndexOutOfBoundsException if the specified index is less than zero or not less than the size of this list
	 */
	public PList<T> minus(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException();
		PList<T> result = PList.empty();
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst(), index--) {
			if (index == 0)
				return result.plusAll(list.minusFirst());
			result = result.plus(list.getFirst());
		}
		throw new IndexOutOfBoundsException();
	}
	
	/**
	 * Returns this list with the element at the specified index replaced by the specified object. O(n).
	 * @param index the index of the element to replace
	 * @param o the object to replace the element at the specified index with
	 * @return this list with the element at the specified index replaced by the specified object
	 * @throws IndexOutOfBoundsException if the specified index is less than zero or not less than the size of this list
	 * @throws NullPointerException if the specified object is null
	 */
	public PList<T> with(int index, T o) {
		if (index < 0)
			throw new IndexOutOfBoundsException();
		PList<T> result = PList.empty();
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst(), index--) {
			if (index == 0)
				return result.plus(o).plusAll(list.minusFirst());
			result = result.plus(list.getFirst());
		}
		throw new IndexOutOfBoundsException();
	}
	
	/**
	 * Returns this list minus the elements of the specified collection. O(n * c) where c is the cost of a 
	 * call of method c.contains.
	 * @param c the collection whose elements are to be removed from this list
	 * @throws NullPointerException if the specified collection is null
	 */
	public PList<T> minusAll(Collection<T> c) {
		PList<T> result = PList.empty();
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst())
			if (!c.contains(list.getFirst()))
				result = result.plus(list.getFirst());
		return result;
	}

	/**
	 * Returns the size of this list. O(n).
	 */
	@Override
	public int size() {
		int n = 0;
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst())
			n++;
		return n;
	}

	/**
	 * Returns true iff this list contains an element that equals the specified object, per the specified object's equals method. O(n).
	 * Returns false if the specified object is null.
	 */
	@Override
	public boolean contains(Object o) {
		if (o == null)
			return false;
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst())
			if (o.equals(list.getFirst()))
				return true;
		return false;
	}

	/**
	 * Returns a new array containing the elements of this list. O(n).
	 * If this list is empty, this method returns a zero-length array.
	 */
	@Override
	public Object[] toArray() {
		ArrayList<Object> buffer = new ArrayList<Object>();
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst())
			buffer.add(list.getFirst());
		return buffer.toArray();
	}

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element); the runtime type of
     * the returned array is that of the specified array.  If the list fits
     * in the specified array, it is returned therein.  Otherwise, a new
     * array is allocated with the runtime type of the specified array and
     * the size of this list.
     *
     * <p>If the list fits in the specified array with room to spare (i.e.,
     * the array has more elements than the list), the element in the array
     * immediately following the end of the list is set to <tt>null</tt>.
     * (This is useful in determining the length of the list <i>only</i> if
     * the caller knows that the list does not contain any null elements.)
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a list known to contain only strings.
     * The following code can be used to dump the list into a newly
     * allocated array of <tt>String</tt>:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of this list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */
	@SuppressWarnings("unchecked")
	@Override
	public <U> U[] toArray(U[] a) {
		ArrayList<U> buffer = new ArrayList<U>();
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst())
			buffer.add((U)list.getFirst());
		return buffer.toArray(a);
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

    /**
     * Returns <tt>true</tt> if this list contains all of the elements of the
     * specified collection. O(c.size() * this.size()).
     * Returns <tt>false</tt> if the specified collection contains null.
     *
     * @param  c collection to be checked for containment in this list
     * @return <tt>true</tt> if this list contains all of the elements of the
     *         specified collection
     * @throws NullPointerException if the specified collection is null
     * @see #contains(Object)
     */
	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o))
				return false;
		return true;
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public boolean retainAll(Collection<?> c) {
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
     * Returns the element at the specified position in this list. O(n).
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
	@Override
	public T get(int index) {
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst())
			if (index-- == 0)
				return list.getFirst();
		throw new IndexOutOfBoundsException();
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method always throws UnsupportedOperationException. 
	 */
	@Deprecated
	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

    /**
     * Returns the index of the first occurrence of the specified object
     * in this list, or -1 if this list does not contain the object.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>o.equals(get(i))</tt>,
     * or -1 if there is no such index.
     * Returns -1 if the specified object is null.
     *
     * @param o object to search for
     * @return the index of the first occurrence of the specified object in
     *         this list, or -1 if this list does not contain the object
     */
	@Override
	public int indexOf(Object o) {
		if (o == null)
			return -1;
		int i = 0;
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst(), i++)
			if (o.equals(list.getFirst()))
				return i;
		return -1;
	}

    /**
     * Returns the index of the last occurrence of the specified object
     * in this list, or -1 if this list does not contain the object. O(n).
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>o.equals(get(i))</tt>,
     * or -1 if there is no such index.
     * Returns -1 if the specified object is null.
     *
     * @param o object to search for
     * @return the index of the last occurrence of the specified object in
     *         this list, or -1 if this list does not contain the object
     */
	@Override
	public int lastIndexOf(Object o) {
		if (o == null)
			return -1;
		int result = -1;
		int i = 0;
		for (PList<T> list = this; !list.isEmpty(); list = list.minusFirst(), i++)
			if (o.equals(list.getFirst()))
				result = i; 
		return result;
	}

    /**
     * Returns a list iterator over the elements in this list (in proper sequence). O(1).
     *
     * @return a list iterator over the elements in this list (in proper sequence)
     */
	@Override
	public ListIterator<T> listIterator() {
		return new ListIterator<T>() {
			
			Cons<T> before = null;
			PList<T> after = PList.this;
			int index = 0;

			@Override
			public boolean hasNext() {
				return !after.isEmpty();
			}

			@Override
			public T next() {
				T result = after.getFirst();
				before = new Cons<T>(result, before);
				after  = after.minusFirst();
				index++;
				return result;
			}

			@Override
			public boolean hasPrevious() {
				return before != null;
			}

			@Override
			public T previous() {
				T result = before.getHead();
				before = before.getTail();
				after = after.plus(0, result);
				index--;
				return result;
			}

			@Override
			public int nextIndex() {
				return index;
			}

			@Override
			public int previousIndex() {
				return index - 1;
			}

			@Deprecated
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Deprecated
			@Override
			public void set(T e) {
				throw new UnsupportedOperationException();
			}

			@Deprecated
			@Override
			public void add(T e) {
				throw new UnsupportedOperationException();
			}
			
		};
	}

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list. O(index).
     * The specified index indicates the first element that would be
     * returned by an initial call to {@link ListIterator#next next}.
     * An initial call to {@link ListIterator#previous previous} would
     * return the element with the specified index minus one.
     *
     * @param index index of first element to be returned from the
     *              list iterator (by a call to the <tt>next</tt> method)
     * @return a list iterator of the elements in this list (in proper
     *         sequence), starting at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
	@Override
	public ListIterator<T> listIterator(int index) {
		ListIterator<T> iterator = listIterator();
		for (int i = 0; i < index; i++)
			iterator.next();
		return iterator;
	}

    /**
     * Returns the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.  (If
     * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
     * empty.) O(n).
     *
     * @param fromIndex low endpoint (inclusive) of the sublist
     * @param toIndex high endpoint (exclusive) of the sublist
     * @return the specified portion of this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *         (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *         fromIndex &gt; toIndex</tt>)
     */
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		if (fromIndex < 0 || toIndex < fromIndex)
			throw new IndexOutOfBoundsException();
		ArrayList<T> buffer = new ArrayList<T>(toIndex - fromIndex);
		PList<T> list = this;
		for (int i = 0; i < fromIndex; i++) {
			if (list.isEmpty())
				throw new IndexOutOfBoundsException();
			list = list.minusFirst();
		}
		for (int i = fromIndex; i < toIndex; i++) {
			if (list.isEmpty())
				throw new IndexOutOfBoundsException();
			buffer.add(list.getFirst());
			list = list.minusFirst();
		}
		return Collections.unmodifiableList(buffer);
	}
	
	/**
	 * Returns a textual representation of this list, of the form [e1, e2, e3].
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		if (!isEmpty()) {
			builder.append(getFirst());
			for (PList<T> list = minusFirst(); !list.isEmpty(); list = list.minusFirst()) {
				builder.append(", ");
				builder.append(list.getFirst());
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Returns <tt>true</tt> iff the specified object is a {@link java.util.List} and it has the same size and
	 * each element in this list is equal (per its {@link java.lang.Object#equals} method)
	 * to the element at the same index in the specified list.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof List)) return false;
		List<?> otherList = (List<?>)other;
		PList<T> list = this;
		Iterator<?> iterator = otherList.iterator();
		for (;;) {
			if (list.isEmpty() == iterator.hasNext())
				return false;
			if (list.isEmpty())
				return true;
			if (!list.getFirst().equals(iterator.next()))
				return false;
			list = list.minusFirst();
		}
	}
	
	/**
	 * Returns the hash code for this list.
	 */
	public int hashCode() {
	    int hashCode = 1;
	    for (T e : this)
	        hashCode = 31 * hashCode + e.hashCode();
	    return hashCode;
	}
	
}
