package er.extensions.migration;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

/**
 * <p>
 * ERXMigrationDatabase/Table/Column exist to make navigating the wonderous API of EOSynchronizationFactory
 * not totally suck.  Additionally, these simple models provide a way to insulate yourself from a dependency
 * on EOModels during migrations while still taking advantage of the database independence of the SQL
 * generation that EOF provides.
 * </p>
 * 
 * <p><b>ACTUAL JAVADOC WILL BE COMING -- THE WIFE IS REQUIRING ME TO COME HOME, THOUGH :)</b></p>
 * 
 * <p>Some samples in the meantime:</p>
 * 
 * <p>You can add a new column onto an existing table:</p>
 * <code>
 * ERXMigrationDatabase.database(channel).existingTableNamed("Request").newStringColumn("requestedByEmailAddress", 255, true).create();
 * </code>
 * 
 * <p>... or you could create an entirely new table, with a foreign key to some existing table:</p> 
 * <code>
 * ERXMigrationDatabase database = ERXMigrationDatabase.database(channel);
 * ERXMigrationTable table = ERXMigrationDatabase.database(channel).newTableNamed("TestPerson4");
 * table.newStringColumn("FirstName", 100, false);
 * table.newStringColumn("LastName", 100, false);
 * table.newStringColumn("EmailAddress", 100, false);
 * table.newStringColumn("PhoneNumber", 10, true);
 * table.newIntegerColumn("PantSize", true);
 * table.newTimestampColumn("Birthdate", true);
 * table.newBigDecimalColumn("HourlyRate", 32, 4, true);
 * table.newFloatColumn("Rating", 10, 2, true);
 * table.newBooleanColumn("Married", false);
 * table.newIntBooleanColumn("Bald", false);
 * table.newIntegerColumn("CompanyID", false);
 * table.create();
 * table.addForeignKey(table.existingColumnNamed("CompanyID"), database.existingTableNamed("Company").existingColumnNamed("companyID"));
 * </code>
 * 
 * <p>
 * Calling table/database.existingXxx does not perform database introspection.  It only creates a stub entry that is enough to perform operations
 * like deleting, renaming, foreign keys, etc.  Calling table.newXxx does not actually create the element, rather it returns a metadata
 * wrapper (similar to EOAttribute, etc, but with migration-specific API's).  Most .newXxx things allow you to call .create() on them.  In
 * the case of a column, you can .newColumn it, then .create() it.  However, if the table does not yet exist, that would fail, so instead
 * you database.newTableNamed, then .newColumn all the columns in it, followed by a table.create() to create the entire block.  For foreign
 * keys, you must have .create()'d both tables (or use existing tables) prior to calling the foreign key methods.
 * </p>
 * 
 * <p>
 * This relies entirely on EOSynchronizationFactory.  If the sync factory for your plugin is wrong, this SQL generation will likewise be
 * wrong.  This is also BRAND new, so I'm sure some of these types won't be mapped exactly right.  More operations will be added in the future
 * as well -- these are just the initial ones that I've found to be most common.
 * </p>
 * 
 * <p>I'll add the rest of the docs later tonight or tomorrow -- famous last words: 08/13/2007.  Also, I know who killed Kennedy -- more 
 * notes to follow.  Oh, and I found Hoffa's body, I'll write about it tomorrow.</p>
 * 
 * @author mschrag
 */
public class ERXMigrationDatabase {
	private EOAdaptorChannel _adaptorChannel;
	private NSMutableArray<ERXMigrationTable> _tables;

	private ERXMigrationDatabase(EOAdaptorChannel adaptorChannel) {
		_adaptorChannel = adaptorChannel;
		_tables = new NSMutableArray<ERXMigrationTable>();
	}

	public EOSynchronizationFactory synchronizationFactory() {
		return (EOSynchronizationFactory) adaptor().synchronizationFactory();
	}

	public EOAdaptor adaptor() {
		return (JDBCAdaptor) _adaptorChannel.adaptorContext().adaptor();
	}

	public JDBCAdaptor jdbcAdaptor() {
		return (JDBCAdaptor) adaptor();
	}

	public EOAdaptorChannel adaptorChannel() {
		return _adaptorChannel;
	}

	@SuppressWarnings("unchecked")
	public ERXMigrationTable existingTableNamed(String name) {
		NSArray<ERXMigrationTable> existingTables = EOQualifier.filteredArrayWithQualifier(_tables, new EOKeyValueQualifier("name", EOQualifier.QualifierOperatorCaseInsensitiveLike, name));
		ERXMigrationTable table;
		if (existingTables.count() == 0) {
			table = new ERXMigrationTable(this, name);
			_tables.addObject(table);
		}
		else {
			table = existingTables.objectAtIndex(0);
		}
		return table;
	}

	@SuppressWarnings("unchecked")
	public ERXMigrationTable newTableNamed(String name) {
		NSArray<ERXMigrationTable> existingTables = EOQualifier.filteredArrayWithQualifier(_tables, new EOKeyValueQualifier("name", EOQualifier.QualifierOperatorCaseInsensitiveLike, name));
		if (existingTables.count() > 0) {
			throw new IllegalArgumentException("You've already referenced a table named '" + name + "'.");
		}
		ERXMigrationTable newTable = new ERXMigrationTable(this, name);
		_tables.addObject(newTable);
		return newTable;
	}

	public EOModel _blankModel() {
		EOModel blankModel = new EOModel();
		blankModel.setConnectionDictionary(_adaptorChannel.adaptorContext().adaptor().connectionDictionary());
		blankModel.setAdaptorName(_adaptorChannel.adaptorContext().adaptor().name());
		return blankModel;
	}

	public void _tableDropped(ERXMigrationTable table) {
		_tables.removeObject(table);
	}

	public static ERXMigrationDatabase database(EOAdaptorChannel adaptorChannel) {
		return new ERXMigrationDatabase(adaptorChannel);
	}

	@SuppressWarnings("unchecked")
	public static NSArray<String> _stringsForExpressions(NSArray<EOSQLExpression> expressions) {
		return (NSArray<String>) expressions.valueForKey("statement");
	}
}
