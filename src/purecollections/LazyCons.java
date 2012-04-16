package purecollections;

import java.util.Iterator;

final class LazyCons<T> extends ICons<T> {

	private final T head;
	private final Thunk<ICons<T>> tail;
	
	public LazyCons(T head, Thunk<ICons<T>> tail) {
		this.head = head;
		this.tail = tail;
	}
	
	public LazyCons(T head, Computation<ICons<T>> tail) {
		this(head, new Thunk<ICons<T>>(tail));
	}
	
	public T getHead() { return head; }
	
	public Computation<ICons<T>> getTailThunk() { return tail; }
	
	public ICons<T> getTail() { return tail.compute(); }
	
	static <A, B> ICons<B> map(ICons<A> xs, final Mapper<A, B> mapper) {
		if (xs == null)
			return null;
		else {
			final Computation<ICons<A>> tailThunk = xs.getTailThunk();
			return new LazyCons<B>(mapper.map(xs.getHead()), new Computation<ICons<B>>() {

				@Override
				public ICons<B> compute() {
					return map(tailThunk.compute(), mapper);
				}
				
			});
		}
	}
	
	static <T> Iterator<T> iterator(final ICons<T> list) {
		return new Iterator<T>() {
			
			ICons<T> rest = list;

			@Override
			public boolean hasNext() {
				return rest != null;
			}

			@Override
			public T next() {
				T result = rest.getHead();
				rest = rest.getTail();
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
}
