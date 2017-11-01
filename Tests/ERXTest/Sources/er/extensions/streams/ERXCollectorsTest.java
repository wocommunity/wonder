package er.extensions.streams;

import static er.extensions.stream.ERXCollectors.toNSArray;
import static er.extensions.stream.ERXCollectors.toNSDictionary;
import static er.extensions.stream.ERXCollectors.toNSSet;
import static java.util.stream.Stream.concat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

/**
 * @author <a href="mailto:hprange@gmail.com.br">Henrique Prange</a>
 */
public class ERXCollectorsTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void collectEmptyNSArrayWhenStreamReturnsNoElements() throws Exception {
		NSArray<?> result = Stream.empty().collect(toNSArray());

		assertThat(result.isEmpty(), is(true));
	}

	@Test
	public void collectNSArrayWithOneElementWhenStreamReturnsOneElement() throws Exception {
		NSArray<String> result = Stream.of("test").collect(toNSArray());

		assertThat(result.size(), is(1));
		assertThat(result, hasItem("test"));
	}

	@Test
	public void collectNSArrayWithMoreThanOneElementWhenStreamReturnsTwoElements() throws Exception {
		NSArray<String> result = Stream.of("test1", "test2").collect(toNSArray());

		assertThat(result.size(), is(2));
		assertThat(result, hasItems("test1", "test2"));
	}

	@Test
	public void collectCombinedArrayWhenStreamCombinedWithAnotherInParallel() throws Exception {
		NSArray<String> result = concat(Stream.of("test1").parallel(), Stream.of("test2").parallel())
				.collect(toNSArray());

		assertThat(result.size(), is(2));
		assertThat(result, hasItems("test1", "test2"));
	}

	@Test
	public void collectImmutableNSArrayWhenCollectingNSArray() throws Exception {
		NSArray<String> result = Stream.of("test").collect(toNSArray());

		assertThat(result, instanceOf(NSArray.class));
		assertThat(result, not(instanceOf(NSMutableArray.class)));
	}

	@Test
	public void collectEmptyNSDictionaryWhenStreamReturnsNoElements() throws Exception {
		NSDictionary<?, ?> result = Stream.empty().collect(toNSDictionary(Object::toString, Function.identity()));

		assertThat(result.isEmpty(), is(true));
	}

	@Test
	public void collectNSDictionaryWhenStreamReturnsOneElement() throws Exception {
		NSDictionary<Integer, String> result = Stream.of("test1")
				.collect(toNSDictionary(String::length, Function.identity()));

		assertThat(result.size(), is(1));
		assertThat(result.get(5), is("test1"));
	}

	@Test
	public void collectNSDictionaryWithMoreThanOneElementWhenStreamReturnsTwoElements() throws Exception {
		NSDictionary<Integer, String> result = Stream.of("test1", "test12")
				.collect(toNSDictionary(String::length, Function.identity()));

		assertThat(result.size(), is(2));
		assertThat(result.get(5), is("test1"));
		assertThat(result.get(6), is("test12"));
	}

	@Test
	public void collectNSDictionaryMergingElementsWhenMergingFunctionProvided() throws Exception {
		NSDictionary<Integer, String> result = Stream.of("test1", "test2")
				.collect(toNSDictionary(String::length, Function.identity(), (s1, s2) -> s1.concat(s2)));

		assertThat(result.size(), is(1));
		assertThat(result.get(5), is("test1test2"));
	}

	@Test
	public void collectImmutableNSDictionaryWhenCollectingNSDictionary() throws Exception {
		NSDictionary<Integer, String> result = Stream.of("test1")
				.collect(toNSDictionary(String::length, Function.identity()));

		assertThat(result, instanceOf(NSDictionary.class));
		assertThat(result, not(instanceOf(NSMutableDictionary.class)));
	}

	@Test
	public void throwExceptionWhenCollectingDictionaryAndMergeIsRequired() throws Exception {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(is("Duplicate key test1"));

		Stream.of("test1", "test2").collect(toNSDictionary(String::length, Function.identity()));
	}

	@Test
	public void collectEmptyNSSetWhenStreamReturnsNoElements() throws Exception {
		NSSet<?> result = Stream.empty().collect(toNSSet());

		assertThat(result.isEmpty(), is(true));
	}

	@Test
	public void collectNSSetWithOneElementWhenStreamReturnsOneElement() throws Exception {
		NSSet<String> result = Stream.of("test").collect(toNSSet());

		assertThat(result.size(), is(1));
		assertThat(result, hasItem("test"));
	}

	@Test
	public void collectNSSetWithMoreThanOneElementWhenStreamReturnsTwoElements() throws Exception {
		NSSet<String> result = Stream.of("test1", "test2").collect(toNSSet());

		assertThat(result.size(), is(2));
		assertThat(result, hasItems("test1", "test2"));
	}

	@Test
	public void collectCombinedSetWhenStreamCombinedWithAnotherInParallel() throws Exception {
		NSSet<String> result = concat(Stream.of("test1").parallel(), Stream.of("test2").parallel())
				.collect(toNSSet());

		assertThat(result.size(), is(2));
		assertThat(result, hasItems("test1", "test2"));
	}

	@Test
	public void collectImmutableNSSetWhenCollectingNSSet() throws Exception {
		NSSet<String> result = Stream.of("test").collect(toNSSet());

		assertThat(result, instanceOf(NSSet.class));
		assertThat(result, not(instanceOf(NSMutableSet.class)));
	}

	@Test
	public void collectNSSetWithOneElementWhenStreamReturnsTwoRepeatedElements() throws Exception {
		NSSet<String> result = Stream.of("test1", "test1").collect(toNSSet());

		assertThat(result.size(), is(1));
		assertThat(result, hasItems("test1"));
	}
}
