package er.extensions.migration;

import java.math.BigDecimal;
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
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.ERXJDBCUtilities;

/**
 * ERXMigrationTable provides table-level migration API's.  To obtain a table, you
 * should call ERXMigrationDatabase.existingTableNamed or ERXMigrationDatabase.newTableNamed.
 * Note: The .newXxxColumn API's cannot reference prototypes for the same reason that migrations
 * in general cannot reference EOModels.
 * 
 * @author mschrag
 */
public class ERXMigrationTable {
	private ERXMigrationDatabase _database;
	private NSMutableArray<ERXMigrationColumn> _columns;
	private String _name;
	private boolean _new;

	/**
	 * Constructs an ERXMigrationTable.
	 * 
	 * @param database the database this table is within
	 * @param name the name of this table
	 */
	protected ERXMigrationTable(ERXMigrationDatabase database, String name) {
		_database = database;
		_columns = new NSMutableArray<ERXMigrationColumn>();
		_name = name;
		_new = true;
	}

	/**
	 * Returns the ERXMigrationDatabase parent of this table.
	 * 
	 * @return the ERXMigrationDatabase parent of this table
	 */
	public ERXMigrationDatabase database() {
		return _database;
	}

	/**
	 * Sets the name of this table.  This does not perform a table rename operation.
	 * 
	 * @param name the name of this table
	 */
	public void _setName(String name) {
		_name = name;
	}

	/**
	 * Returns the name of this table.
	 * 
	 * @return the name of this table
	 */
	public String name() {
		return _name;
	}
	
	/**
	 * Returns true if this table has not yet been created in the database.
	 * 
	 * @return if this table has not yet been created in the database
	 */
	public boolean isNew() {
		return _new;
	}
	
	/**
	 * Sets whether or not this table has been created in the database.
	 * 
	 * @param isNew if true, the table has been created
	 */
	public void _setNew(boolean isNew) {
		_new = isNew;
	}

	/**
	 * Returns an EOEntity representing this table with no
	 * EOAttributes in it.
	 *  
	 * @return a shell of an EOEntity for this table
	 */
	public EOEntity _blankEntity() {
		EOModel newModel = _database._blankModel();
		EOEntity newEntity = new EOEntity();
		newEntity.setExternalName(_name);
		newEntity.setName(_name);
		newModel.addEntity(newEntity);
		return newEntity;
	}

	/**
	 * Returns an EOEntity representing this table that
	 * contains all of the EOAttributes for any 
	 * ERXMigrationColumn that has been created or
	 * retrieved from this table.
	 * 
	 * @return an EOAttributeful EOEntity for this table 
	 */
	public EOEntity _newEntity() {
		EOEntity entity = _blankEntity();
		for (ERXMigrationColumn column : _columns) {
			column._newAttribute(entity);
		}
		return entity;
	}

	/**
	 * Returns the ERMigrationColumn for the column with the given name.  If 
	 * no column has already been created via a newColumn call, then this
	 * will simply return a shell ERXMigrationColumn that should be sufficient
	 * for performing drop, rename, and other reference operations.
	 * 
	 * @param name the name of the column to retrieve
	 * @return the ERXMigrationColumn for the column name
	 */
	@SuppressWarnings("unchecked")
	public ERXMigrationColumn existingColumnNamed(String name) {
		NSArray<ERXMigrationColumn> existingColumns = EOQualifier.filteredArrayWithQualifier(_columns, new EOKeyValueQualifier("name", EOQualifier.QualifierOperatorCaseInsensitiveLike, name));
		ERXMigrationColumn column;
		if (existingColumns.count() == 0) {
			try {
				column = _newColumn(name, 0, 0, 0, 0, false, null, false);
			}
			catch (SQLException e) {
				throw new IllegalStateException("This should never have executed a database operation.", e);
			}
			column._setNew(false);
			_columns.addObject(column);
		}
		else {
			column = existingColumns.objectAtIndex(0);
		}
		return column;
	}

	/**
	 * Returns a simple single-attribute-mapping EORelationship between two columns.  This
	 * is called by the foreign key generator.
	 *  
	 * @param sourceColumn the source attribute of the relationship 
	 * @param destinationColumn the destination attribute of the relationship
	 * @return the EORelationship that joins the two given columns
	 */
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

	/**
	 * Returns a new ERXMigrationColumn with the given attributes.  This method is the
	 * most general-purpose of the .newXxx methods.  Calling this method will not
	 * actually create the column, rather it will only return a metadata wrapper of
	 * the attributes you specify.  Call .create() on the resulting column object
	 * to create the column (or create several columns and then call .create() on
	 * this table to create an entire table).
	 *  
	 * @param name the name of the column to create
	 * @param jdbcType the JDBC type of the column (see java.sql.Types)
	 * @param width the width of the column (or 0 for unspecified)
	 * @param precision the precision of the column (or 0 for unspecified)
	 * @param scale the scale of the column (or 0 for unspecified)
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value for the column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn _newColumn(String name, int jdbcType, int width, int precision, int scale, boolean allowsNull, Object defaultValue, boolean autocreate) throws SQLException {
		ERXMigrationColumn newColumn = new ERXMigrationColumn(this, name, jdbcType, width, precision, scale, allowsNull, defaultValue);
		_columns.addObject(newColumn);
		if (autocreate) {
			newColumn.create();
		}
		return newColumn;
	}

	/**
	 * Returns a new ERXMigrationColumn with the given attributes.  This method is the
	 * most general-purpose of the .newXxx methods.  If this table already exists, 
	 * calling the .newXxxColumn methods will immediate execute the SQL to add the
	 * columns to the table.  If this table is new, however, calling .newXxxColumn
	 * will only return a metadata object, and you must call .create() on
	 * the table.
	 *  
	 * @param name the name of the column to create
	 * @param jdbcType the JDBC type of the column (see java.sql.Types)
	 * @param width the width of the column (or 0 for unspecified)
	 * @param precision the precision of the column (or 0 for unspecified)
	 * @param scale the scale of the column (or 0 for unspecified)
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value for the column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newColumn(String name, int jdbcType, int width, int precision, int scale, boolean allowsNull, Object defaultValue) throws SQLException {
		return _newColumn(name, jdbcType, width, precision, scale, allowsNull, defaultValue, !_new);
	}

	/**
	 * Returns a new ERXMigrationColumn with the given attributes.  This method is the
	 * most general-purpose of the .newXxx methods.  If this table already exists, 
	 * calling the .newXxxColumn methods will immediate execute the SQL to add the
	 * columns to the table.  If this table is new, however, calling .newXxxColumn
	 * will only return a metadata object, and you must call .create() on
	 * the table.
	 *  
	 * @param name the name of the column to create
	 * @param jdbcType the JDBC type of the column (see java.sql.Types)
	 * @param width the width of the column (or 0 for unspecified)
	 * @param precision the precision of the column (or 0 for unspecified)
	 * @param scale the scale of the column (or 0 for unspecified)
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newColumn(String name, int jdbcType, int width, int precision, int scale, boolean allowsNull) throws SQLException {
		return _newColumn(name, jdbcType, width, precision, scale, allowsNull, null, !_new);
	}

	/**
	 * Returns a new String column (VARCHAR).  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param width the max width of the varchar
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newStringColumn(String name, int width, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.VARCHAR, width, 0, 0, allowsNull);
	}

	/**
	 * Returns a new String column (VARCHAR).  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param width the max width of the varchar
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newStringColumn(String name, int width, boolean allowsNull, String defaultValue) throws SQLException {
		return newColumn(name, Types.VARCHAR, width, 0, 0, allowsNull, defaultValue);
	}

	/**
	 * Returns a new integer column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newIntegerColumn(String name, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.INTEGER, 0, 0, 0, allowsNull);
	}

	/**
	 * Returns a new integer column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newIntegerColumn(String name, boolean allowsNull, Integer defaultValue) throws SQLException {
		return newColumn(name, Types.INTEGER, 0, 0, 0, allowsNull, defaultValue);
	}

	/**
	 * Returns a new integer column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param scale the scale of the integer
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newIntegerColumn(String name, int scale, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.INTEGER, 0, 0, scale, allowsNull);
	}

	/**
	 * Returns a new integer column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param scale the scale of the integer
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newIntegerColumn(String name, int scale, boolean allowsNull, Integer defaultValue) throws SQLException {
		return newColumn(name, Types.INTEGER, 0, 0, scale, allowsNull, defaultValue);
	}

	/**
	 * Returns a new integer column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param scale the scale of the integer
	 * @param precision the precision of the integer
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newIntegerColumn(String name, int scale, int precision, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.INTEGER, 0, scale, precision, allowsNull);
	}

	/**
	 * Returns a new integer column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param scale the scale of the integer
	 * @param precision the precision of the integer
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newIntegerColumn(String name, int scale, int precision, boolean allowsNull, Object defaultValue) throws SQLException {
		return newColumn(name, Types.INTEGER, 0, scale, precision, allowsNull, defaultValue);
	}

	/**
	 * Returns a new float column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param scale the scale of the float
	 * @param precision the precision of the float
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newFloatColumn(String name, int precision, int scale, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.FLOAT, 0, precision, scale, allowsNull);
	}

	/**
	 * Returns a new float column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param scale the scale of the float
	 * @param precision the precision of the float
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newFloatColumn(String name, int precision, int scale, boolean allowsNull, Float defaultValue) throws SQLException {
		return newColumn(name, Types.FLOAT, 0, precision, scale, allowsNull, defaultValue);
	}

	/**
	 * Returns a new BigDecimal column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param scale the scale of the BigDecimal
	 * @param precision the precision of the BigDecimal
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newBigDecimalColumn(String name, int precision, int scale, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.DECIMAL, 0, precision, scale, allowsNull);
	}

	/**
	 * Returns a new BigDecimal column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param scale the scale of the BigDecimal
	 * @param precision the precision of the BigDecimal
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newBigDecimalColumn(String name, int precision, int scale, boolean allowsNull, BigDecimal defaultValue) throws SQLException {
		return newColumn(name, Types.DECIMAL, 0, precision, scale, allowsNull, defaultValue);
	}

	/**
	 * Returns a new varchar(5) boolean column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newBooleanColumn(String name, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.VARCHAR, 5, 0, 0, allowsNull);
	}

	/**
	 * Returns a new varchar(5) boolean column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newBooleanColumn(String name, boolean allowsNull, Boolean defaultValue) throws SQLException {
		return newColumn(name, Types.VARCHAR, 5, 0, 0, allowsNull, defaultValue);
	}

	/**
	 * Returns a new integer boolean column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newIntBooleanColumn(String name, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.INTEGER, 0, 0, 0, allowsNull);
	}

	/**
	 * Returns a new integer boolean column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newIntBooleanColumn(String name, boolean allowsNull, Boolean defaultValue) throws SQLException {
		return newColumn(name, Types.INTEGER, 0, 0, 0, allowsNull, defaultValue);
	}

	/**
	 * Returns a new Blob column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param width the width of the blob
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newBlobColumn(String name, int width, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.BLOB, width, 0, 0, allowsNull);
	}

	/**
	 * Returns a new Blob column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param width the width of the blob
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newBlobColumn(String name, int width, boolean allowsNull, NSData defaultValue) throws SQLException {
		return newColumn(name, Types.BLOB, width, 0, 0, allowsNull, defaultValue);
	}

	/**
	 * Returns a new timestamp column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param allowsNull if true, the column will allow null values
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newTimestampColumn(String name, boolean allowsNull) throws SQLException {
		return newColumn(name, Types.TIMESTAMP, 0, 0, 0, allowsNull);
	}

	/**
	 * Returns a new timestamp column.  See newColumn(..) for the full docs.
	 * 
	 * @param name the name of the column
	 * @param allowsNull if true, the column will allow null values
	 * @param defaultValue the default value of this column
	 * @return the new ERXMigrationColumn
	 * @throws SQLException if the column cannot be created 
	 */
	public ERXMigrationColumn newTimestampColumn(String name, boolean allowsNull, NSTimestamp defaultValue) throws SQLException {
		return newColumn(name, Types.TIMESTAMP, 0, 0, 0, allowsNull, defaultValue);
	}

	/**
	 * Callback method for ERXMigrationColumn to notify the table that
	 * it has been deleted.
	 * 
	 * @param column the column that has been deleted
	 */
	public void _columnDeleted(ERXMigrationColumn column) {
		_columns.removeObject(column);
	}

	/**
	 * Returns an array of EOSQLExpressions for creating this table and all of its ERXMigrationColumns.
	 * 
	 * @return an array of EOSQLExpressions for creating this table
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _createExpressions() {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaGeneration.createTableStatementsForEntityGroup(new NSArray<EOEntity>(_newEntity()));
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		return expressions;
	}

	/**
	 * Executes the SQL operations to create this table.
	 * 
	 * @throws SQLException if the creation fails
	 */
	public void create() throws SQLException {
		if (_new) {
			ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_createExpressions()));
			for (ERXMigrationColumn column : _columns) {
				column._setNew(false);
			}
			_new = false;
		}
		else {
			ERXMigrationDatabase.log.warn("You called .create() on the table '" + _name + "', but it was already created.");
		}
	}

	/**
	 * Returns an array of EOSQLExpressions for dropping this table.
	 * 
	 * @return an array of EOSQLExpressions for dropping this table
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _dropExpressions() {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaGeneration.dropTableStatementsForEntityGroup(new NSArray<EOEntity>(_blankEntity()));
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		return expressions;
	}

	/**
	 * Executes the SQL operations to drop this table.
	 * 
	 * @throws SQLException if the drop fails
	 */
	public void drop() throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_dropExpressions()));
		_database._tableDropped(this);
	}

	/**
	 * Returns an array of EOSQLExpressions for renaming this table.
	 * 
	 * @return an array of EOSQLExpressions for renaming this table
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _renameToExpressions(String newName) {
		EOSchemaSynchronization schemaSynchronization = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaSynchronization.statementsToRenameTableNamed(name(), newName, NSDictionary.EmptyDictionary);
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		_setName(newName);
		return expressions;
	}

	/**
	 * Executes the SQL operations to rename this table.
	 * 
	 * @throws SQLException if the rename fails
	 */
	public void renameTo(String newName) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_renameToExpressions(newName)));
	}

	/**
	 * Returns an array of EOSQLExpressions for setting the primary key constraint of this table (only supports single attribute PK's right now).
	 * 
	 * @return an array of EOSQLExpressions for setting the primary key constraint of this table
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _setPrimaryKeyExpressions(ERXMigrationColumn column) {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		EOAttribute attribute = column._newAttribute();
		EOEntity entity = attribute.entity();
		entity.setPrimaryKeyAttributes(new NSArray<EOAttribute>(attribute));
		NSArray<EOSQLExpression> expressions = schemaGeneration.primaryKeyConstraintStatementsForEntityGroup(new NSArray<EOEntity>(entity));
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		return expressions;
	}

	/**
	 * Executes the SQL operations to add this primary key constraint (only supports single attribute PK's right now).
	 * 
	 * @param column the primary key column to create
	 * @throws SQLException if the constraint fails
	 */
	public void setPrimaryKey(ERXMigrationColumn column) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_setPrimaryKeyExpressions(column)));
	}

	/**
	 * Returns an array of EOSQLExpressions for adding a foreign key constraint to this table (only supports single attribute FK's right now).
	 * 
	 * @return an array of EOSQLExpressions for adding a foreign key constraint to this table
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _addForeignKeyExpressions(ERXMigrationColumn sourceColumn, ERXMigrationColumn destinationColumn) {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		NSArray<EOSQLExpression> expressions = schemaGeneration.foreignKeyConstraintStatementsForRelationship(_newRelationship(sourceColumn, destinationColumn));
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		return expressions;
	}

	/**
	 * Executes the SQL operations to add this foreign key constraint (only supports single attribute FK's right now).
	 * 
	 * @param sourceColumn the source column of the relationship
	 * @param destinationColumn the destination column of the relationship (should be the PK of the destination table)
	 * @throws SQLException if the add fails
	 */
	public void addForeignKey(ERXMigrationColumn sourceColumn, ERXMigrationColumn destinationColumn) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_addForeignKeyExpressions(sourceColumn, destinationColumn)));
	}

	/**
	 * Returns an array of EOSQLExpressions for removing the primary key constraint of this table (only supports single attribute PK's right now).
	 * 
	 * @return an array of EOSQLExpressions for removing the primary key constraint of this table
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOSQLExpression> _dropPrimaryKeyExpressions(ERXMigrationColumn column) {
		EOSchemaGeneration schemaGeneration = _database.synchronizationFactory();
		EOAttribute attribute = column._newAttribute();
		EOEntity entity = attribute.entity();
		entity.setPrimaryKeyAttributes(new NSArray<EOAttribute>(attribute));
		NSArray<EOSQLExpression> expressions = schemaGeneration.dropPrimaryKeySupportStatementsForEntityGroup(new NSArray<EOEntity>(entity));
		ERXMigrationDatabase._ensureNotEmpty(expressions);
		return expressions;
	}

	/**
	 * Executes the SQL operations to drop this primary key constraint (only supports single attribute PK's right now).
	 * 
	 * @param column the primary key column
	 * @throws SQLException if the drop fails
	 */
	public void dropPrimaryKey(ERXMigrationColumn column) throws SQLException {
		ERXJDBCUtilities.executeUpdateScript(_database.adaptorChannel(), ERXMigrationDatabase._stringsForExpressions(_dropPrimaryKeyExpressions(column)));
	}
}
