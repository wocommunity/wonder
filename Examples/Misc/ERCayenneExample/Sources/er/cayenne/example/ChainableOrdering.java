package er.cayenne.example;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

/**
 * Provides support for defining Orderings in a "chain". Especially useful in combination with "Key".
 */

public class ChainableOrdering extends Ordering {

	/**
	 * Constructs a ChainableOrdering.
	 */
	public ChainableOrdering( String key, SortOrder sortOrder ) {
		super( key, sortOrder );
	}

	/**
	 * Constructs a ChainableOrdering.
	 */
	public ChainableOrdering( Key<?> key, SortOrder sortOrder ) {
		this( key.key(), sortOrder );
	}

	/**
	 * Adds an ordering after this one.
	 */
	public ChainableOrderings then( Ordering nextSortOrdering ) {
		ChainableOrderings sortOrderings = array();
		sortOrderings.add( nextSortOrdering );
		return sortOrderings;
	}

	/**
	 * @return this ordering in a list
	 */
	public ChainableOrderings array() {
		ChainableOrderings sortOrderings = new ChainableOrderings();
		sortOrderings.add( this );
		return sortOrderings;
	}

	/**
	 * Sorts a List using this Ordering.
	 */
	public <T> void sort( List<T> array ) {
		Ordering.orderList( array, new ChainableOrderings( this ) );
	}

	/**
	 * Wraps a List of Orderings to support chaining.
	 */
	public static class ChainableOrderings extends ArrayList<Ordering> {

		/**
		 * Constructs a new ChainableOrderings
		 */
		public ChainableOrderings() {
			super();
		}

		/**
		 * Constructs a new ChainableOrderings with the given Ordering
		 */
		public ChainableOrderings( Ordering ordering ) {
			add( ordering );
		}

		/**
		 * Constructs a new ChainableOrderings with the list of Orderings
		 */
		public ChainableOrderings( Ordering... orderings ) {
			for( Ordering o : orderings ) {
				add( o );
			}
		}

		/**
		 * Constructs a new ChainableOrderings from a List of Orderings.
		 */
		public ChainableOrderings( List<Ordering> orderings ) {
			super( orderings );
		}

		/**
		 * Adds an Ordering to the list and returns itself to be chained again.
		 */
		public ChainableOrderings then( Ordering nextOrdering ) {
			add( nextOrdering );
			return this;
		}

		/**
		 * Sorts a List using this Ordering.
		 */
		public <T> void sort( List<T> list ) {
			Ordering.orderList( list, this );
		}
	}
}