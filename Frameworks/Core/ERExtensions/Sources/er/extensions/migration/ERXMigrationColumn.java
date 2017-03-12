package er.extensions.migration;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaSynchronization;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

import er.extensions.jdbc.ERXJDBCUtilities;
import er.extensions.jdbc.ERXSQLHelper;

/**
 * ERXMigrationColumn is conceptually equivalent to an EOAttribute in the
 * ERXMigrationXxx model. To obtain an ERXMigrationColumn, call
 * ERXMigrationTable.newXxxColumn(..).
 * 
 * @author mschrag
 */
public class ERXMigrationColumn {
	private static final Logger log = LoggerFactory.getLogger(ERXMigrationDatabase.class);

	public static final String NULL_VALUE_TYPE = "___NULL_VALUE_TYPE___";

	/**
	 * Constant for use with ERXMigrationTable.newXxxColumn AllowsNull columns.
	 */
	public static final boolean AllowsNull = true;

	/**
	 * Constant for use with ERXMigrationTable.newXxxColumn NotNull columns.
	 */
	public static final boolean NotNull = false;
	
	private ERXMigrationTable _table;
	private String _name;
	private int _jdbcType;
	private int _width;
	private int _precision;
	private int _scale;
	private boolean _allowsNull;
	private Object _defaultValue;
	private boolean _new;
	private String _overrideValueType;
	private String _overrideExternalType;
	
	private boolean _primaryKey;

	/**
	 * Constructs a new ERXMigrationColumn.
	 * 
	 * @param table
	 *            the parent table
	 * @param name
	 *            the name of the column to create
	 * @param jdbcType
	 *            the JDBC type of the column (see java.sql.Types)
	 * @param width
	 *            the width of the column (or 0 for unspecified)
	 * @param precision
	 *            the precision of the column (or 0 for unspecified)
	 * @param scale
	 *            the scale of the column (or 0 for unspecified)
	 * @param allowsNull
	 *            if true, the column will allow null values
	 * @param overrideValueType
	 *            value type associated with the underlying attribute (or <code>null</code> for autoselect)
	 * @param defaultValue
	 *            this will set the "Default" hint in the EOAttribute's userInfo
	 *            dictionary (your plugin must support this)
	 */
	protected ERXMigrationColumn(ERXMigrationTable table, String name, int jdbcType, int width, int precision, int scale, boolean allowsNull, String overrideValueType, Object defaultValue) {
		_table = table;
		_name = name;
		_jdbcType = jdbcType;
		_width = width;
		_precision = precision;
		_scale = scale;
		_allowsNull = allowsNull;
		_overrideValueType = overrideValueType;
		_defaultValue = defaultValue;
		_new = true;
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
	 * Sets the name of this column. This does not perform a column rename
	 * operation.
	 * 
	 * @param name
	 *            the name of this column
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
	 * Sets the width of this column. This does not perform a column resize
	 * operation.
	 * 
	 * @param width
	 *            the width of this column
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
	 * Sets whether or not this column allows nulls. This does not perform a
	 * column change operation.
	 * 
	 * @param allowsNull
	 *            if true, this column allows nulls
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
	 * Sets the precision of this column. This does not perform a column change
	 * operation.
	 * 
	 * @param precision
	 *            the precision of this column
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
	 * Sets the scale of this column. This does not perform a column change
	 * operation.
	 * 
	 * @param scale
	 *            the scale of this column
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
	 * Overrides the external type of this column.
	 * 
	 * @param overrideExternalType
	 *            the external type to override
	 */
	public void _setOverrideExternalType(String overrideExternalType) {
		_overrideExternalType = overrideExternalType;
	}

	/**
	 * Returns the external type of this column (or null if there is no
	 * override).
	 * 
	 * @return the external type of this column
	 */
	public String overrideExternalType() {
		return _overrideExternalType;
	}

	/**
	 * Sets the value type for the underlying attribute for this column.
	 * 
	 * @param overrideValueType
	 *            the value type for the underlying attribute for this column
	 */
	public void _setOverrideValueType(String overrideValueType) {
		_overrideValueType = overrideValueType;
	}

	/**
	 * Returns the value type associated with the underlying attribute (or null
	 * to have this autoselected).
	 * 
	 * @return the value type associated with the underlying attribute
	 */
	public String overrideValueType() {
		return _overrideValueType;
	}

	/**
	 * Sets the default value of this column.
	 * 
	 * @param defaultValue
	 *            the default value of this column
	 */
	public void setDefaultValue(Object defaultValue) {
		_defaultValue = defaultValue;
	}

	/**
	 * Returns the default value of this column.
	 * 
	 * @return the default value of this column
	 */
	public Object defaultValue() {
		return _defaultValue;
	}
	
	/**
	 * Sets whether or not this column is a primary key.
	 * 
	 * @param primaryKey whether or not this column is a primary key
	 */
	public void _setPrimaryKey(boolean primaryKey) {
		_primaryKey = primaryKey;
	}
	
	/**
	 * Returns whether or not this column is a primary key (note this
	 * is only valid if you told migrations that this column is a
	 * primary key).
	 * 
	 * @return whether or not this column is a primary key
	 */
	public boolean isPrimaryKey() {
		return _primaryKey;
	}

	/**
	 * Returns true if this column has not yet been created in the database.
	 * 
	 * @return if this column has not yet been created in the database
	 */
	public boolean isNew() {
		return _new;
	}

	/**
	 * Sets whether or not this column has been created in the database.
	 * 
	 * @param isNew
	 *            if true, the column has been created
	 */
	public void _setNew(boolean isNew) {
		_new = isNew;
	}

	/**
	 * Returns an EOAttribute with all of its fields filled in based on the
	 * properties of this ERXMigrationColumn. The attribute is attached to a
	 * table._blankEntity().
	 * 
	 * @return an EOAttribute with all of its fields filled in
	 */
	public EOAttribute _newAttribute() {
		return _newAttribute(_table._blankEntity());
	}

	/**
	 * Returns an EOAttribute with all of its fields filled in based on the
	 * properties of this ERXMigrationColumn. The attribute is attached to the
	 * given entity.
	 * 
	 * @param entity
	 *            the entity to add the attribute to
	 * @return an EOAttribute with all of its fields filled in
	 */
	@SuppressWarnings("unchecked")
	public EOAttribute _newAttribute(EOEntity entity) {
	  EOAdaptor eoAdaptor = _table.database().adaptor();
	  // MS: Hack to make Memory adaptor migrations "work"
	  if (!(eoAdaptor instanceof JDBCAdaptor)) {
	    EOAttribute nonJdbcAttribute = new EOAttribute();
	    nonJdbcAttribute.setName(_name);
	    nonJdbcAttribute.setColumnName(_name);
	    nonJdbcAttribute.setExternalType("nonJdbcAttribute");
		entity.addAttribute(nonJdbcAttribute);
	    return nonJdbcAttribute;
	  }
	  
		JDBCAdaptor adaptor = (JDBCAdaptor)_table.database().adaptor();
		ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(adaptor);
		String externalType = sqlHelper.externalTypeForJDBCType(adaptor, _jdbcType);
		if (externalType == null) {
			externalType = "IF_YOU_ARE_SEEING_THIS_SOMETHING_WENT_WRONG_WITH_EXTERNAL_TYPES";
		}
		EOAttribute attribute = adaptor.createAttribute(_name, _name, _jdbcType, externalType, _precision, _scale, _allowsNull ? 1 : 0);
		if (_width > 0) {
			attribute.setWidth(_width);
		}
		if (_defaultValue != null) {
			NSDictionary userInfo = attribute.userInfo();
			NSMutableDictionary mutableUserInfo;
			if (userInfo == null) {
				mutableUserInfo = new NSMutableDictionary();
			}
			else {
				mutableUserInfo = userInfo.mutableClone();
			}
			mutableUserInfo.setObjectForKey(_defaultValue, "default");
			attribute.setUserInfo(mutableUserInfo);
		}

		if (_overrideValueType != null) {
			if (ERXMigrationColumn.NULL_VALUE_TYPE.equals(_overrideValueType)) {
				attribute.setValueType(null);
			}
			else {
				attribute.setValueType(_overrideValueType);
			}
			if (sqlHelper.reassignExternalTypeForValueTypeOverride(attribute)) {
				adaptor.assignExternalTypeForAttribute(attribute);
			}
		}

		if (_overrideExternalType != null) {
			attribute.setExternalType(_overrideExternalType);
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
		ERXMigrationDatabase._ensureNotEmpty(expressions, "add column", true);
		return expressions;
	}

	/**
	 * Executes the SQL operations to create this column.
	 * 
	 * @throws SQLException
	 *             if the creation fails
	 */
	public void create() throws SQLException {
		if (_new) {
			ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_createExpressions()));
			_new = false;
		}
		else {
			log.warn("You called .create() on the column '{}', but it was already created.", _name);
		}
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
		ERXMigrationDatabase._ensureNotEmpty(expressions, "delete column", true);
		return expressions;
	}

	/**
	 * Executes the SQL operations to delete this column.
	 * 
	 * @throws SQLException
	 *             if the delete fails
	 */
	public void delete() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_deleteExpressions()));
		_table._columnDeleted(this);
	}

	/**
	 * Returns an array of EOSQLExpressions for renaming this column.
	 * 
	 * @param newName
	 *            the new name of this column
	 * @return an array of EOSQLExpressions for renaming this column
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _renameToExpressions(String newName) {
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToRenameColumnNamed(name(), _table.name(), newName, NSDictionary.EmptyDictionary);
		ERXMigrationDatabase._ensureNotEmpty(expressions, "rename column", true);
		_setName(newName);
		return expressions;
	}

	/**
	 * Executes the SQL operations to rename this column.
	 * 
	 * @param newName
	 *            the new name of this column
	 * @throws SQLException
	 *             if the rename fails
	 */
	public void renameTo(String newName) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_renameToExpressions(newName)));
	}

	/**
	 * Changes the "allows null" state of this column.
	 * 
	 * @param allowsNull
	 *            if true, this column allows nulls
	 * @throws SQLException
	 *             if the change fails
	 */
	@SuppressWarnings("unchecked")
	public void setAllowsNull(boolean allowsNull) throws SQLException {
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToModifyColumnNullRule(name(), _table.name(), allowsNull, NSDictionary.EmptyDictionary);
		ERXMigrationDatabase._ensureNotEmpty(expressions, "modify allows null", true);
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(expressions));
	}

	/**
	 * Changes the data type of this column.
	 * 
	 * @param jdbcType
	 *            the new JDBC type of the column (see java.sql.Types)
	 * @param scale
	 *            the new scale
	 * @param precision
	 *            the new precision
	 * @param width
	 *            the new width
	 * @param options
	 *            the options to use for conversion (or null)
	 * @throws SQLException
	 *             if the change fails
	 */
	@SuppressWarnings("unchecked")
	public void setDataType(int jdbcType, int scale, int precision, int width, NSDictionary options) throws SQLException {
		JDBCAdaptor adaptor = (JDBCAdaptor) _table.database().adaptor();
		String externalType = ERXSQLHelper.newSQLHelper(adaptor).externalTypeForJDBCType(adaptor, jdbcType);
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToConvertColumnType(_name, _table.name(), null, new _ColumnType(externalType, scale, precision, width), options);
		ERXMigrationDatabase._ensureNotEmpty(expressions, "convert column type", true);
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(expressions));
		_jdbcType = jdbcType;
		_scale = scale;
		_precision = precision;
		_width = width;
	}

	/**
	 * Changes the data type of this column to a type that has a width.
	 * 
	 * @param jdbcType
	 *            the new JDBC type of the column (see java.sql.Types)
	 * @param width
	 *            the new width
	 * @param options
	 *            the options to use for conversion (or null)
	 * @throws SQLException
	 *             if the change fails
	 */
	public void setWidthType(int jdbcType, int width, NSDictionary options) throws SQLException {
		setDataType(jdbcType, 0, 0, width, options);
	}

	/**
	 * Changes the data type of this column to a new numeric type.
	 * 
	 * @param jdbcType
	 *            the new JDBC type of the column (see java.sql.Types)
	 * @param scale
	 *            the new scale
	 * @param precision
	 *            the new precision
	 * @param options
	 *            the options to use for conversion (or null)
	 * @throws SQLException
	 *             if the change fails
	 */
	public void setNumericType(int jdbcType, int scale, int precision, NSDictionary options) throws SQLException {
		setDataType(jdbcType, scale, precision, 0, options);
	}

	/**
	 * Implements EOSchemaSynchronization.ColumnTypes
	 * 
	 * @author mschrag
	 */
	public static class _ColumnType implements EOSchemaSynchronization.ColumnTypes {
		private String _name;
		private int _scale;
		private int _precision;
		private int _width;

		public _ColumnType(String name, int scale, int precision, int width) {
			_name = name;
			_scale = scale;
			_precision = precision;
			_width = width;
		}

		public String name() {
			return _name;
		}

		public int precision() {
			return _precision;
		}

		public int scale() {
			return _scale;
		}

		public int width() {
			return _width;
		}

	}
}
