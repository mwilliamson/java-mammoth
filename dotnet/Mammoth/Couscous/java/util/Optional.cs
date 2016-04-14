using System;
using Mammoth.Couscous.java.util.function;

namespace Mammoth.Couscous.java.util
{
    internal interface Optional<T>
	{
		bool isPresent();
		Optional<U> map<U>(Function<T, U> function);
		Optional<U> flatMap<U>(Function<T, Optional<U>> function);
		T orElse(T value);
		T orElseGet(Supplier<T> supplier);
		T orElseThrow<TException>(Supplier<TException> exceptionSupplier) where TException : Exception;
		T get();
		void ifPresent(Consumer<T> consumer);
    }

	internal class None<T> : Optional<T> {
		public bool isPresent() {
			return false;
		}

		public Optional<U> map<U>(Function<T, U> function) {
			return new None<U>();
		}

		public Optional<U> flatMap<U>(Function<T, Optional<U>> function) {
			return new None<U>();
		}

		public T orElse(T value) {
			return value;
		}
		
		public T orElseGet(Supplier<T> supplier) {
			return supplier.get();
		}
		
		public T orElseThrow<TException>(Supplier<TException> exceptionSupplier) where TException : Exception {
			throw exceptionSupplier.get();
		}

		public T get() {
			throw new NoSuchElementException();
		}
		
		public void ifPresent(Consumer<T> consumer) {
		}
	}

	internal class Some<T> : Optional<T> {
		private readonly T _value;

		internal Some(T value) {
			_value = value;
		}

		public bool isPresent() {
			return true;
		}

		public Optional<U> map<U>(Function<T, U> function) {
			return new Some<U>(function.apply(_value));
		}

		public Optional<U> flatMap<U>(Function<T, Optional<U>> function) {
			return function.apply(_value);
		}

		public T orElse(T value) {
			return _value;
		}
		
		public T orElseGet(Supplier<T> supplier) {
			return _value;
		}
		
		public T orElseThrow<TException>(Supplier<TException> exceptionSupplier) where TException : Exception {
			return _value;
		}

		public T get() {
			return _value;
		}
		
		public void ifPresent(Consumer<T> consumer) {
			consumer.accept(_value);
		}
	}

    internal static class Optional
    {
		internal static Optional<T> empty<T>()
        {
			return new None<T>();
        }
        
        internal static Optional<T> of<T>(T value)
        {
			return new Some<T> (value);
        }
    }
}
