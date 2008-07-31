package er.extensions;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;


/**
 * <p>
 * ERXSortOrdering is an EOSortOrdering subclass that provides support for
 * chaining (like ERXKey).
 * </p>
 * <p>
 * Examples:
 * </p>
 * 
 * <pre>
 * Person.COMPANY.dot(Company.NAME).asc().then(Person.FIRST_NAME.desc())
 * </pre>
 * 
 * @author mschrag
 * 
 */
public class ERXSortOrdering extends EOSortOrdering {
	/**
	 * Constructs an ERXSortOrdering (see EOSortOrdering).
	 * 
	 * @param key
	 *            the key to sort on
	 * @param selector
	 *            the sort selector
	 */
	public ERXSortOrdering(String key, NSSelector selector) {
		super(key, selector);
	}

	/**
	 * Constructs an ERXSortOrdering (see EOSortOrdering).
	 * 
	 * @param key
	 *            the key to sort on
	 * @param selector
	 *            the sort selector
	 */
	public ERXSortOrdering(ERXKey key, NSSelector selector) {
		this(key.key(), selector);
	}

	/**
	 * Returns ERXSortOrderings with this sort ordering followed by the provided
	 * next sort ordering.
	 * 
	 * @param nextSortOrdering
	 *            the next sort ordering to chain to this
	 * @return an array of sort orderings
	 */
	public ERXSortOrderings then(EOSortOrdering nextSortOrdering) {
		ERXSortOrderings sortOrderings = array();
		sortOrderings.addObject(nextSortOrdering);
		return sortOrderings;
	}

	/**
	 * Returns this sort ordering as an array.
	 * 
	 * @return this sort ordering as an array
	 */
	public ERXSortOrderings array() {
		ERXSortOrderings sortOrderings = new ERXSortOrderings();
		sortOrderings.addObject(this);
		return sortOrderings;
	}

	/**
	 * Constructs an ERXSortOrdering (see EOSortOrdering).
	 * 
	 * @param key
	 *            the key to sort on
	 * @param selector
	 *            the sort selector
	 * @return a new ERXSortOrdering
	 */
	public static ERXSortOrdering sortOrderingWithKey(String key, NSSelector selector) {
		return new ERXSortOrdering(key, selector);
	}

	/**
	 * Constructs an ERXSortOrdering (see EOSortOrdering).
	 * 
	 * @param key
	 *            the key to sort on
	 * @param selector
	 *            the sort selector
	 * @return a new ERXSortOrdering
	 */
	public static ERXSortOrdering sortOrderingWithKey(ERXKey key, NSSelector selector) {
		return new ERXSortOrdering(key, selector);
	}

	/**
	 * ERXSortOrderings is an NSMutableArray<EOSortOrdering> that
	 * provides methods for chaining.
	 * 
	 * @author mschrag
	 */
	public static class ERXSortOrderings extends NSMutableArray {
		/**
		 * Constructs an empty ERXSortOrderings.
		 */
		public ERXSortOrderings() {
			super();
		}

		/**
		 * Constructs an ERXSortOrderings with one sort order.
		 * 
		 * @param sortOrdering the sort ordering to add
		 */
		public ERXSortOrderings(EOSortOrdering sortOrdering) {
			super(sortOrdering);
		}

		/**
		 * Constructs an ERXSortOrderings with the list of sort orders.
		 * 
		 * @param sortOrderings the sort orderings to add
		 */
		public ERXSortOrderings(EOSortOrdering... sortOrderings) {
			super(sortOrderings);
		}

		/**
		 * Constructs an ERXSortOrderings with the array of sort orders.
		 * 
		 * @param sortOrderings the sort orderings to add
		 */
		public ERXSortOrderings(NSArray sortOrderings) {
			super(sortOrderings);
		}

		/**
		 * Adds the given sort ordering to the end of this list and
		 * returns "this" so it can be chained again.
		 * 
		 * @param nextSortOrdering the sort ordering to add
		 * @return this (with the sort ordering appended)
		 */
		public ERXSortOrderings then(EOSortOrdering nextSortOrdering) {
			addObject(nextSortOrdering);
			return this;
		}
	}
}
