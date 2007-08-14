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

public class ERXMigrationColumn {
	private ERXMigrationTable _table;
	private String _name;
	private int _jdbcType;
	private int _width;
	private int _precision;
	private int _scale;
	private boolean _allowsNull;

	public ERXMigrationColumn(ERXMigrationTable table, String name, int jdbcType, int width, int precision, int scale, boolean allowsNull) {
		_table = table;
		_name = name;
		_jdbcType = jdbcType;
		_width = width;
		_precision = precision;
		_scale = scale;
		_allowsNull = allowsNull;
	}

	public ERXMigrationTable table() {
		return _table;
	}

	public void _setName(String name) {
		_name = name;
	}

	public String name() {
		return _name;
	}

	public void _setWidth(int width) {
		_width = width;
	}

	public int width() {
		return _width;
	}

	public void _setAllowsNull(boolean allowsNull) {
		_allowsNull = allowsNull;
	}

	public boolean allowsNull() {
		return _allowsNull;
	}

	public void _setPrecision(int precision) {
		_precision = precision;
	}

	public int precision() {
		return _precision;
	}

	public void _setScale(int scale) {
		_scale = scale;
	}

	public int scale() {
		return _scale;
	}

	public EOAttribute _newAttribute() {
		return _newAttribute(_table._blankEntity());
	}
	
	public EOAttribute _newAttribute(EOEntity entity) {
		JDBCAdaptor adaptor = _table.database().jdbcAdaptor();
		EOAttribute attribute = adaptor.createAttribute(_name, _name, _jdbcType, adaptor.externalTypeForJDBCType(_jdbcType), _precision, _scale, _allowsNull ? 1 : 0);
		if (_width > 0) {
			attribute.setWidth(_width);
		}
		entity.addAttribute(attribute);
		return attribute;
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _createExpressions() {
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToInsertColumnForAttribute(_newAttribute(), NSDictionary.EmptyDictionary);
		return expressions;
	}

	public void create() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_createExpressions()));
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _deleteExpressions() {
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToDeleteColumnNamed(name(), _table.name(), NSDictionary.EmptyDictionary);
		return expressions;
	}

	public void delete() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_deleteExpressions()));
		_table._columnDeleted(this);
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _renameToExpressions(String newName) {
		EOSchemaSynchronization schemaSynchronization = _table.database().synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToRenameColumnNamed(name(), _table.name(), newName, NSDictionary.EmptyDictionary);
		_setName(newName);
		return expressions;
	}

	public void renameTo(String newName) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_table.database().adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_renameToExpressions(newName)));
	}
}
