package er.extensions.eof;

import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.foundation.NSSelector;

/**
 * Sort ordering class allowing generation of custom SQL,
 * which will then be used in the ORDER BY clause.
 * 
 * @version 2016
 * @author Copyright (c) 2016 NUREG. All rights reserved.
 * @author sgaertner
 */
public abstract class ERXCustomSQLSortOrdering extends ERXSortOrdering {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 *
	 * @param key
	 *            the {@link ERXKey}
	 * @param selector
	 *            the {@link NSSelector}
	 */
	public ERXCustomSQLSortOrdering(ERXKey key, NSSelector selector) {
		super(key, selector);
	}

	/**
	 * Should return the SQL code, which will be used as part of the ORDER BY clause.
	 *
	 * @param sqlExpression
	 *            the {@link EOSQLExpression}
	 * @return see above
	 */
	public abstract String sqlStringForSQLExpression(EOSQLExpression sqlExpression);

}
