package er.extensions.eof;

/**
 * Defines possible behaviors for handling <code>null</code> values when sorting lists.
 * 
 * @author sgaertner
 */
public enum ERXSortNullHandling {

	/**
	 * <code>null</code> values always come first.
	 */
	FIRST,

	/**
	 * <code>null</code> values always come last.
	 */
	LAST,

	/**
	 * <code>null</code> values are considered the smallest possible values.
	 * When sorting ascending they come first, when sorting descending they
	 * come last.
	 */
	SMALLEST,

	/**
	 * <code>null</code> values are considered the largest possible values.
	 * When sorting ascending they come last, when sorting descending they
	 * come first.
	 */
	LARGEST,

	/**
	 * No behavior defined, use default.
	 */
	DEFAULT

}
