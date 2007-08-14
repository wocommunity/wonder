package er.extensions.migration;

import java.sql.SQLException;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaSynchronization;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

import er.extensions.ERXJDBCUtilities;

/**
 * ERXMigrationColumn is conceptually equivalent to an EOAttribute in the ERXMigrationXxx model.  To
 * obtain an ERXMigrationColumn, call ERXMigrationTable.newXxxColumn(..).
 *  
 * @author mschrag
 */
public class ERXMigrationColumn {
	private ERXMigrationTable _table;
	private String _name;
	private int _jdbcType;
	private int _width;
	private int _precision;
	private int _scale;
	private boolean _allowsNull;

	/**
	 * Constructs a new ERXMigrationColumn.
	 * 
	 * @param table the parent table
	 * @param name the name of the column to create
	 * @param jdbcType the JDBC type of the column (see java.sql.Types)
	 * @param width the width of the column (or 0 for unspecified)
	 * @param precision the precision of the column (or 0 for unspecified)
	 * @param scale the scale of the column (or 0 for unspecified)
	 * @param allowsNull if true, the column will allow null values
	 */
	protected ERXMigrationColumn(ERXMigrationTable table, String name, int jdbcType, int width, int precision, int scale, boolean allowsNull) {
		_table = table;
		_name = name;
		_jdbcType = jdbcType;
		_width = width;
		_precision = precision;
		_scale = scale;
		_allowsNull = allowsNull;
	}

	/**
	 * Returns the parent ERXMigrationTable of this column.
	 * 
	 * @return the parent ERXMigrationTable of this column
	 */
	public ERXMigrationTable table() {
		return _table;
	}

	/**
	 * Sets the name of this column.  This does not perform a column rename operation.
	 * 
	 * @param name the name of this column
	 */
	public void _setName(String name) {
		_name = name;
	}

	/**
	 * Returns the name of this column.
	 * 
	 * @return the name of this column
	 */
	public String name() {
		return _name;
	}

	/**
	 * Sets the width of this column.  This does not perform a column resize operation.
	 * 
	 * @param width the width of this column
	 */
	public void _setWidth(int width) {
		_width = width;
	}

	/**
	 * Returns the width of this column.
	 * 
	 * @return the width of this column
	 */
	public int width() {
		return _width;
	}

	/**
	 * Sets whether or not this column allows nulls.  This does not perform a column change operation.
	 * 
	 * @param allowsNull if true, this column allows nulls
	 */
	public void _setAllowsNull(boolean allowsNull) {
		_allowsNull = allowsNull;
	}

	/**
	 * Returns the width of this column.
	 * 
	 * @return the width of this column
	 */
	public boolean allowsNull() {
		return _allowsNull;
	}

	/**
	 * Sets the precision of this column.  This does not perform a column change operation.
	 * 
	 * @param precision the precision of this column
	 */
	public void _setPrecision(int precision) {
		_precision = precision;
	}

	/**
	 * Returns the precision of this column.
	 * 
	 * @return the precision of this column
	 */
	public int precision() {
		return _precision;
	}

	/**
	 * Sets the scale of this column.  This does not perform a column change operation.
	 * 
	 * @param scale the scale of this column
	 */
	public void _setScale(int scale) {
		_scale = scale;
	}

	/**
	 * Returns the scale of this column.
	 * 
	 * @return the scale of this column
	 */
	public int scale() {
		return _scale;
	}

	/**
	 * Returns an EOAttribute with all of its fields filled in based
	 * on the properties of this ERXMigrationColumn.  The attribute
	 * is attached to a table._blankEntity().
	 * 
	 * @return an EOAttribute with all of its fields filled in
	 */
	public EOAttribute _newAttribute() {
		return _newAttribute(_table._blankEntity());
	}

	/**
	 * Returns an EOAttribute with all of its fields filled in based
	 * on the properties of this ERXMigrationColumn.  The attribute
	 * is attached to the given entity.
	 * 
	 * @param entity the entity to add the attribute to
	 * @return an EOAttribute with all of its fields filled in
	 */
	public EOAttribute _newAttribute(EOEntity entity) {
		JDBCAdaptor adaptor = (JDBCAdaptor) _table.database().adaptor();
		EOAttribute attribute = adaptor.createAttribute(_name, _name, _jdbcType, adaptor.externalTypeForJDBCType(_jdbcType), _precision, _scale, _allowsNull ? 1 : 0);
		if (_width > 0) {
			attribute.setWidth(_width);
		}
		entity.addAttribute(attribute);
		return attribute;
	}

	/**
	 * Returns an array of EOSQLExpressions for creating this column.
	 * 
	 * @return an array of EOSQLExpressions for creating this column
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _createExpressions() {
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToInsertColumnForAttribute(_newAttribute(), NSDictionary.EmptyDictionary);
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		return expressions;
	}

	/**
	 * Executes the SQL operations to create this column.
	 * 
	 * @throws SQLException if the creation fails
	 */
	public void create() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_createExpressions()));
	}

	/**
	 * Returns an array of EOSQLExpressions for deleting this column.
	 * 
	 * @return an array of EOSQLExpressions for deleting this column
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _deleteExpressions() {
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToDeleteColumnNamed(name(), _table.name(), NSDictionary.EmptyDictionary);
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		return expressions;
	}

	/**
	 * Executes the SQL operations to delete this column.
	 * 
	 * @throws SQLException if the delete fails
	 */
	public void delete() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_deleteExpressions()));
		_table._columnDeleted(this);
	}

	/**
	 * Returns an array of EOSQLExpressions for renaming this column.
	 * 
	 * @param newName the new name of this column
	 * @return an array of EOSQLExpressions for renaming this column
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _renameToExpressions(String newName) {
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToRenameColumnNamed(name(), _table.name(), newName, NSDictionary.EmptyDictionary);
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		_setName(newName);
		return expressions;
	}

	/**
	 * Executes the SQL operations to rename this column.
	 * 
	 * @param newName the new name of this column
	 * @throws SQLException if the rename fails
	 */
	public void renameTo(String newName) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_renameToExpressions(newName)));
	}
}