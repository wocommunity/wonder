package er.extensions.stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

/**
 * Implementations of {@link Collector} that implement various useful reduction
 * operations related with NSCollection classes.
 *
 * <p>
 * The following are examples of using the predefined collectors to perform
 * common mutable reduction tasks:
 *
 * <pre>
 * // Accumulate names into a NSArray
 * NSArray<String> array = people.stream().map(Person::name).collect(ERXCollectors.toNSArray());
 *
 * // Accumulate names into a NSSet
 * NSSet<String> set = people.stream().map(Person::name).collect(ERXCollectors.toNSSet());
 *
 * // Accumulate people by name into a NSDictionary
 * NSDictionary<String, Person> dictionary = people.stream().collect(ERXCollectors.toNSDictionary(Person::name, Function.identity()));
 * </pre>
 *
 * @author <a href="mailto:hprange@gmail.com">Henrique Prange</a>
 * @since 7.1
 */
public class ERXCollectors {
	/**
	 * Returns a {@code Collector} that accumulates the input elements into a new
	 * {@code NSArray}.
	 *
	 * @param <T>
	 *            the type of the input elements
	 * @return a {@code Collector} which collects all the input elements into a
	 *         {@code NSArray}, in encounter order
	 */
	public static <T> Collector<T, ?, NSArray<T>> toNSArray() {
		return collectingAndThen(toCollection(NSMutableArray::new), NSArray::immutableClone);
	}

	/**
	 * Returns a {@code Collector} that accumulates the input elements into a new
	 * {@code NSSet}.
	 *
	 * @param <T>
	 *            the type of the input elements
	 * @return a {@code Collector} which collects all the input elements into a
	 *         {@code NSSet}, in encounter order
	 */
	public static <T> Collector<T, ?, NSSet<T>> toNSSet() {
		return collectingAndThen(toCollection(NSMutableSet::new), NSSet::immutableClone);
	}

	/**
	 * Returns a {@code Collector} that accumulates the input elements grouped by
	 * key/value into a new {@code NSDictionary}.
	 *
	 * @param keyMapper
	 *            a mapping function to produce keys.
	 * @param valueMapper
	 *            a mapping function to produce values.
	 * @return a {@code Collector} which collects elements into a
	 *         {@code NSDictionary} whose keys and values are the result of applying
	 *         mapping functions to the input elements.
	 */
	public static <T, K, U> Collector<T, ?, NSDictionary<K, U>> toNSDictionary(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
		BinaryOperator<U> throwingMerger = (k, v) -> {
			throw new IllegalStateException(String.format("Duplicate key %s", k));
		};

		return toNSDictionary(keyMapper, valueMapper, throwingMerger);
	}

	/**
	 * Returns a {@code Collector} that accumulates the input elements grouped by
	 * key/value into a new {@code NSDictionary}.
	 *
	 * @param keyMapper
	 *            a mapping function to produce keys.
	 * @param valueMapper
	 *            a mapping function to produce values
	 * @param mergeFunction
	 *            a merge function, used to resolve collisions between values
	 *            associated with the same key, as supplied to
	 *            {@link Map#merge(Object, Object, BiFunction)}
	 * @return a {@code Collector} which collects elements into a
	 *         {@code NSDictionary} whose keys are the result of applying a key
	 *         mapping function to the input elements, and whose values are the
	 *         result of applying a value mapping function to all input elements
	 *         equal to the key and combining them using the merge function
	 */
	@SuppressWarnings("unchecked")
	public static <T, K, U> Collector<T, ?, NSDictionary<K, U>> toNSDictionary(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction) {
		return collectingAndThen(toMap(keyMapper, valueMapper, mergeFunction, NSMutableDictionary::new), dict -> ((NSDictionary<K, U>) dict.immutableClone()));
	}

	/**
	 * Must not be instantiated.
	 */
	private ERXCollectors() {
	}
}
