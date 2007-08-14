package er.extensions.migration;

import java.sql.SQLException;
import java.sql.Types;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSchemaSynchronization;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXJDBCUtilities;

public class ERXMigrationTable {
	private ERXMigrationDatabase _database;
	private NSMutableArray<ERXMigrationColumn> _columns;
	private String _name;

	public ERXMigrationTable(ERXMigrationDatabase database, String name) {
		_database = database;
		_columns = new NSMutableArray<ERXMigrationColumn>();
		_name = name;
	}

	public ERXMigrationDatabase database() {
		return _database;
	}

	public void _setName(String name) {
		_name = name;
	}

	public String name() {
		return _name;
	}

	public EOEntity _blankEntity() {
		EOModel newModel = _database._blankModel();
		EOEntity newEntity = new EOEntity();
		newEntity.setExternalName(_name);
		newEntity.setName(_name);
		newModel.addEntity(newEntity);
		return newEntity;
	}

	public EOEntity _newEntity() {
		EOEntity entity = _blankEntity();
		for (ERXMigrationColumn column : _columns) {
			column._newAttribute(entity);
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public ERXMigrationColumn existingColumnNamed(String name) {
		NSArray<ERXMigrationColumn> existingColumns = EOQualifier.filteredArrayWithQualifier(_columns, new EOKeyValueQualifier("name", EOQualifier.QualifierOperatorCaseInsensitiveLike, name));
		ERXMigrationColumn column;
		if (existingColumns.count() == 0) {
			column = newColumn(name, 0, 0, 0, 0, false);
			_columns.addObject(column);
		}
		else {
			column = existingColumns.objectAtIndex(0);
		}
		return column;
	}

	public ERXMigrationColumn newColumn(String name, int jdbcType, int width, int precision, int scale, boolean allowsNull) {
		ERXMigrationColumn newColumn = new ERXMigrationColumn(this, name, jdbcType, width, precision, scale, allowsNull);
		_columns.addObject(newColumn);
		return newColumn;
	}
	
	public EORelationship _newRelationship(ERXMigrationColumn sourceColumn, ERXMigrationColumn destinationColumn) {
		EOAttribute sourceAttribute = sourceColumn._newAttribute();
		EOEntity entity = sourceAttribute.entity();

		EOAttribute destinationAttribute = destinationColumn._newAttribute();
		EOEntity destinationEntity = destinationAttribute.entity();
		destinationEntity.setPrimaryKeyAttributes(new NSArray<EOAttribute>(destinationAttribute));

		EORelationship relationship = new EORelationship();
		relationship.setEntity(entity);
		
		EOJoin join = new EOJoin(sourceAttribute, destinationAttribute);
		relationship.addJoin(join);
		
		return relationship;
	}

	public ERXMigrationColumn newStringColumn(String name, int width, boolean allowsNull) {
		return newColumn(name, Types.VARCHAR, width, 0, 0, allowsNull);
	}

	public ERXMigrationColumn newIntegerColumn(String name, boolean allowsNull) {
		return newColumn(name, Types.INTEGER, 0, 0, 0, allowsNull);
	}

	public ERXMigrationColumn newIntegerColumn(String name, int scale, boolean allowsNull) {
		return newColumn(name, Types.INTEGER, 0, 0, scale, allowsNull);
	}

	public ERXMigrationColumn newIntegerColumn(String name, int scale, int precision, boolean allowsNull) {
		return newColumn(name, Types.INTEGER, 0, scale, precision, allowsNull);
	}

	public ERXMigrationColumn newFloatColumn(String name, int precision, int scale, boolean allowsNull) {
		return newColumn(name, Types.FLOAT, 0, precision, scale, allowsNull);
	}

	public ERXMigrationColumn newBigDecimalColumn(String name, int precision, int scale, boolean allowsNull) {
		return newColumn(name, Types.DECIMAL, 0, precision, scale, allowsNull);
	}

	public ERXMigrationColumn newBooleanColumn(String name, boolean allowsNull) {
		return newColumn(name, Types.VARCHAR, 5, 0, 0, allowsNull);
	}

	public ERXMigrationColumn newIntBooleanColumn(String name, boolean allowsNull) {
		return newColumn(name, Types.INTEGER, 0, 0, 0, allowsNull);
	}

	public ERXMigrationColumn newBlobColumn(String name, int width, boolean allowsNull) {
		return newColumn(name, Types.BLOB, width, 0, 0, allowsNull);
	}

	public ERXMigrationColumn newTimestampColumn(String name, boolean allowsNull) {
		return newColumn(name, Types.TIMESTAMP, 0, 0, 0, allowsNull);
	}

	public void _columnDeleted(ERXMigrationColumn column) {
		_columns.removeObject(column);
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _createExpressions() {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaGeneration.createTableStatementsForEntityGroup(new NSArray<EOEntity>(_newEntity()));
		return expressions;
	}

	public void create() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_createExpressions()));
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _dropExpressions() {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaGeneration.dropTableStatementsForEntityGroup(new NSArray<EOEntity>(_blankEntity()));
		return expressions;
	}

	public void drop() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_dropExpressions()));
		_database._tableDropped(this);
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _renameToExpressions(String newName) {
		EOSchemaSynchronization schemaSynchronization = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToRenameTableNamed(name(), newName, NSDictionary.EmptyDictionary);
		_setName(newName);
		return expressions;
	}

	public void renameTo(String newName) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_renameToExpressions(newName)));
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _addPrimaryKeyExpressions() {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaGeneration.primaryKeyConstraintStatementsForEntityGroup(new NSArray<EOEntity>(_newEntity()));
		return expressions;
	}

	public void addPrimaryKey() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_addPrimaryKeyExpressions()));
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _addForeignKeyExpressions(ERXMigrationColumn sourceColumn, ERXMigrationColumn destinationColumn) {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaGeneration.foreignKeyConstraintStatementsForRelationship(_newRelationship(sourceColumn, destinationColumn));
		return expressions;
	}

	public void addForeignKey(ERXMigrationColumn sourceColumn, ERXMigrationColumn destinationColumn) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_addForeignKeyExpressions(sourceColumn, destinationColumn)));
	}

	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _dropPrimaryKeyExpressions() {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaGeneration.dropPrimaryKeySupportStatementsForEntityGroup(new NSArray<EOEntity>(_newEntity()));
		return expressions;
	}

	public void dropPrimaryKey() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_dropPrimaryKeyExpressions()));
	}
}
