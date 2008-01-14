package er.extensions.migration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

/**
 * <p>
 * ERXMigrationDatabase, ERXMigrationTable, and ERXMigrationColumn exist to make
 * navigating the wonderous API of EOSynchronizationFactory not totally suck.
 * Additionally, these simple models provide a way to insulate yourself from a
 * dependency on EOModels during migrations while still taking advantage of the
 * database independence of the SQL generation that EOF provides. The concept is
 * inspired by the original migration by the Rails migrations API. Currently
 * this API is only suitable for SQL migrations, which is why the terminology is
 * based on the relational model vs EOF's more generic concepts like Models,
 * Entities, and Attributes.
 * </p>
 * 
 * <p>
 * Prior to this API, and still fully supported (and required for more
 * complicated operations), all migrations had to be written with SQL. The
 * downside of writing SQL is that you are writing database-specific operations,
 * which you must provide per-database implementations of. EOF already supports
 * an API for database-agnostic SQL generation that via the
 * EOSynchronizationFactory family of interfaces, but that API is overly
 * complicated. ERXMigrationDatabase aims to provide a much simpler API on top
 * of EOSynchronizationFactory that lets you perform common database-agnostic
 * operations like adding and deleting columns, creating and dropping tables,
 * adding primary keys, and adding foreign keys.
 * </p>
 * 
 * <p>
 * ERXMigrationDatabase is conceptually similar to an EOModel, ERXMigrationTable
 * to an EOEntity, and ERXMigrationColumn to an EOAttribute. The names were
 * specifically chosen to make the SQL-specific nature of the API clear
 * (currently most of the API does not expose SQL-ness, but I'm assuming that in
 * the future it may as the complexity of the operations provided increases).
 * All of the API allows you to build an in-memory model of your structural
 * changes along with some "perform now" method calls that actual execute SQL
 * commands against the provided adaptor channel.
 * </p>
 * 
 * <p>
 * Let's take a look at some examples. Take the very common case of a migration
 * that just adds a new column to a table:
 * </p>
 * <code>
 * ERXMigrationDatabase.database(channel).existingTableNamed("Request").newStringColumn("requestedByEmailAddress", 255, true);
 * </code>
 * 
 * <p>
 * Another more complex case is that you are introducing an entirely new table
 * that has a foreign key to some existing table:
 * </p>
 * <code>
 * ERXMigrationDatabase database = ERXMigrationDatabase.database(channel);
 * ERXMigrationTable table = ERXMigrationDatabase.database(channel).newTableNamed("TestPerson");
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
 * In the above examples, database.existingTableNamed and
 * table.existingColumnNamed are called. Calling table/database.existingXxx()
 * does not perform database reverse engineering. It only creates a stub entry
 * that is enough to perform operations like deleting, renaming, foreign keys,
 * etc. Calling table.newXxx does not create the element in the database if the
 * table is new, rather it returns a metadata wrapper (similar to EOAttribute,
 * etc, but with migration-specific API's). However, if the table already
 * exists, calling .newXxxColumn on the table will create the column
 * immediately. You should generally not call .create() on an object you
 * obtained from a call to .existingXxx, because it will only be a stub and
 * generally insufficient to actually create in the database. The call to
 * .existingXxx implies that the corresponding element already exists in the
 * database. If you are creating an entire table, you can use the batching API
 * like the second example where you can call database.newTableNamed(), then
 * .newColumn all the columns in it, followed by a table.create() to create the
 * entire block. For foreign keys, you must have .create()'d both tables (or use
 * existing tables) prior to calling the foreign key methods.
 * </p>
 * 
 * <p>
 * It's important to note that this API relies entirely on
 * EOSynchronizationFactory. If the sync factory for your plugin is wrong, the
 * SQL generation in the ERXMigrationDatabase API's will likewise be wrong.
 * </p>
 * 
 * @author mschrag
 */
public class ERXMigrationDatabase {
	public static final Logger log = Logger.getLogger(ERXMigrationDatabase.class);

	private EOModel _model;
	private EOAdaptorChannel _adaptorChannel;
	private NSMutableArray<ERXMigrationTable> _tables;

	/**
	 * Constructs an ERXMigrationDatabase
	 * 
	 * @param adaptorChannel
	 *            the adaptor channel to connect to
	 */
	private ERXMigrationDatabase(EOAdaptorChannel adaptorChannel, EOModel model) {
		_adaptorChannel = adaptorChannel;
		_model = model;
		_tables = new NSMutableArray<ERXMigrationTable>();
	}

	/**
	 * Returns the synchronization factory for this adaptor.
	 * 
	 * @return the synchronization factory for this adaptor
	 */
	public EOSynchronizationFactory synchronizationFactory() {
		return (EOSynchronizationFactory) adaptor().synchronizationFactory();
	}

	/**
	 * Returns the adaptor for the given channel.
	 * 
	 * @return the adaptor for the given channel
	 */
	public EOAdaptor adaptor() {
		return (JDBCAdaptor) _adaptorChannel.adaptorContext().adaptor();
	}

	/**
	 * Returns the model associated with this migration.
	 * 
	 * @return the model associated with this migration
	 */
	public EOModel model() {
		return _model;
	}

	/**
	 * Returns the adaptor channel.
	 * 
	 * @return the adaptor channel
	 */
	public EOAdaptorChannel adaptorChannel() {
		return _adaptorChannel;
	}

	/**
	 * Returns an ERXMigrationTable with the given table name. This method does
	 * not perform any database reverse engineering. If you ask for an existing
	 * table, it will only return a stub of the table that should be sufficient
	 * for performing column operations and miscellaneous table operations like
	 * dropping. If you call newTableNamed, existingTableNamed will return the
	 * tables you create.
	 * 
	 * @param name
	 *            the name of the table to lookup
	 * 
	 * @return an ERXMigrationTable instance
	 */
	@SuppressWarnings("unchecked")
	public ERXMigrationTable existingTableNamed(String name) {
		NSArray<ERXMigrationTable> existingTables = EOQualifier.filteredArrayWithQualifier(_tables, new EOKeyValueQualifier("name", EOQualifier.QualifierOperatorCaseInsensitiveLike, name));
		ERXMigrationTable table;
		if (existingTables.count() == 0) {
			table = new ERXMigrationTable(this, name);
			table._setNew(false);
			_tables.addObject(table);
		}
		else {
			table = existingTables.objectAtIndex(0);
		}
		return table;
	}

	/**
	 * Creates a new blank ERXMigrationTable. This is essentially the same as
	 * calling existingTableNamed except that it performs some simple validation
	 * to make sure this table hasn't been created in this ERXMigrationDatabase
	 * yet. Note that this check is not checking the actual database -- it is
	 * only verifying that you have not called newTableNamed or
	 * existingTableNamed on the name you provide. After calling newTableNamed,
	 * the instance returned from this method will also be returned from calls
	 * to existingTableNamed. The table will not be created from this call, only
	 * an object model is built.
	 * 
	 * @param name
	 *            the name of the table to create
	 * @return a new ERXMigrationTable
	 */
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

	/**
	 * Returns a blank EOModel with the connection dictionary from the adaptor.
	 * 
	 * @return a blank EOModel
	 */
	public EOModel _blankModel() {
		EOModel blankModel = new EOModel();
		NSDictionary connectionDictionary = null;
		if (_model != null) {
			connectionDictionary = _model.connectionDictionary();
		}
		if (connectionDictionary == null) {
			connectionDictionary = _adaptorChannel.adaptorContext().adaptor().connectionDictionary();
		}
		blankModel.setConnectionDictionary(connectionDictionary);
		blankModel.setAdaptorName(_adaptorChannel.adaptorContext().adaptor().name());
		return blankModel;
	}

	/**
	 * Notification callback to tell the database that the user dropped the
	 * given table.
	 * 
	 * @param table
	 *            the table that was dropped
	 */
	public void _tableDropped(ERXMigrationTable table) {
		_tables.removeObject(table);
	}

	/**
	 * Returns an ERXMigrationDatabase for the given EOAdaptorChannel. This will
	 * return a new ERXMigrationDatabase for every call, so if you need to
	 * perform multiple operations within a single database instance (for
	 * instance, adding foreign keys that talk to two tables), you should
	 * operate within a single ERXMigrationDatabase instance. If you have a
	 * model, you should use database(adaptorChannel, model) instead of this
	 * variant so that migrations can use the connection dictionary that is
	 * closest to being correct.
	 * 
	 * @param adaptorChannel
	 *            the adaptor channel to operate within
	 * @return an ERXMigrationDatabase
	 */
	public static ERXMigrationDatabase database(EOAdaptorChannel adaptorChannel) {
		return new ERXMigrationDatabase(adaptorChannel, null);
	}

	/**
	 * Returns an ERXMigrationDatabase for the given EOAdaptorChannel. This will
	 * return a new ERXMigrationDatabase for every call, so if you need to
	 * perform multiple operations within a single database instance (for
	 * instance, adding foreign keys that talk to two tables), you should
	 * operate within a single ERXMigrationDatabase instance.
	 * 
	 * @param adaptorChannel
	 *            the adaptor channel to operate within
	 * @param model
	 *            the model that corresponds to this table
	 * @return an ERXMigrationDatabase
	 */
	public static ERXMigrationDatabase database(EOAdaptorChannel adaptorChannel, EOModel model) {
		return new ERXMigrationDatabase(adaptorChannel, model);
	}

	/**
	 * Throws an ERXMigrationFailedException if the array of expressions is
	 * empty. Not all sync factories support all the listed operations, so this
	 * makes sure that the requested operation doesn't silently fail.
	 * 
	 * @param expressions
	 *            the expressions to check
	 */
	public static void _ensureNotEmpty(NSArray<EOSQLExpression> expressions) {
		if (expressions == null || expressions.count() == 0) {
			throw new ERXMigrationFailedException("Your EOSynchronizationFactory does not support this operation.");
		}
	}

	/**
	 * Returns an NSArray of SQL strings that correspond to the NSArray of
	 * EOSQLExpressions that were passed in.
	 * 
	 * @param expressions
	 *            the expressions to retrieve SQL for
	 * @return an NSArray of SQL strings
	 */
	@SuppressWarnings("unchecked")
	public static NSArray<String> _stringsForExpressions(NSArray<EOSQLExpression> expressions) {
		return (NSArray<String>) expressions.valueForKey("statement");
	}

	/**
	 * A convenience implementation of IERXMigration that passes in an
	 * ERXMigrationDatabase instead of channel + model.
	 * 
	 * @author mschrag
	 */
	public static abstract class Migration implements IERXMigration {
		public void downgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {
			downgrade(editingContext, ERXMigrationDatabase.database(channel, model));
		}

		/**
		 * @see IERXMigration.downgrade
		 * @param editingContext
		 *            the editing context
		 * @param database
		 *            the migration database
		 * @throws Throwable
		 *             if anything goes wrong
		 */
		public abstract void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable;

		/**
		 * Overridden to return null by default
		 */
		public NSArray<ERXModelVersion> modelDependencies() {
			return null;
		}

		public void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {
			upgrade(editingContext, ERXMigrationDatabase.database(channel, model));
		}

		/**
		 * @see IERXMigration.upgrade
		 * @param editingContext
		 *            the editing context
		 * @param database
		 *            the migration database
		 * @throws Throwable
		 *             if anything goes wrong
		 */
		public abstract void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable;
	}
}
