package de.flapdoodle.embed.process.types;

public abstract class Try {

	public static <T, E extends Exception> ThrowingSupplier<T, E> with(ThrowingSupplier<T, E> supplier) {
		return supplier;
	}
	
	public static <T, E extends Exception> ThrowingConsumer<T, E> with(ThrowingConsumer<T, E> supplier) {
		return supplier;
	}

}
