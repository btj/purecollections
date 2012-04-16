package purecollections;

import java.util.concurrent.atomic.AtomicReference;

final class Thunk<T> implements Computation<T> {
	
	private static abstract class ThunkState<T> {
		
		abstract T force(AtomicReference<ThunkState<T>> state);
		
	}
	
	private static final class UnforcedThunkState<T> extends ThunkState<T> {
		
		private final Computation<T> computation;
		
		public UnforcedThunkState(Computation<T> computation) {
			this.computation = computation;
		}
		
		public T force(AtomicReference<ThunkState<T>> state) {
			T value = computation.compute();
			state.lazySet(new ForcedThunkState<T>(value));
			return value;
		}
		
	}
	
	private static final class ForcedThunkState<T> extends ThunkState<T> {
		
		private final T value;
		
		public ForcedThunkState(T value) {
			this.value = value;
		}
		
		public T force(AtomicReference<ThunkState<T>> state) {
			return value;
		}
		
	}
	
	private final AtomicReference<ThunkState<T>> state;
	
	public Thunk(Computation<T> computation) {
		state = new AtomicReference<ThunkState<T>>(new UnforcedThunkState<T>(computation));
	}
	
	public Thunk(T value) {
		state = new AtomicReference<ThunkState<T>>(new ForcedThunkState<T>(value));
	}
	
	public T compute() {
		return state.get().force(state);
	}

}
