package er.extensions.migration;

import er.extensions.jdbc.ERXSQLHelper;

/**
 * ERXMigrationIndex provides a wrapper around the
 * definition of an index for migrations.
 * 
 * @author mschrag
 */
public class ERXMigrationIndex {
	private String _name;
	private ERXSQLHelper.ColumnIndex[] _columns;
	private boolean _unique;
	
	/**
	 * Creates a new index reference.
	 * 
	 * @param name the name of the index
	 * @param unique if true, the index will be a unique index
	 * @param columns the columns to index on
	 */
	public ERXMigrationIndex(String name, boolean unique, ERXSQLHelper.ColumnIndex... columns) {
		_name = name;
		_unique = unique;
		_columns = columns;
	}
	
	/**
	 * Returns the name of this index.
	 * 
	 * @return the name of this index
	 */
	public String name() {
		return _name;
	}
	
	/**
	 * Returns the columns being indexed.
	 * 
	 * @return the columns being indexed
	 */
	public ERXSQLHelper.ColumnIndex[] columns() {
		return _columns;
	}
	
	/**
	 * Returns whether or not this is a unique index.
	 * 
	 * @return whether or not this is a unique index
	 */
	public boolean isUnique() {
		return _unique;
	}
}