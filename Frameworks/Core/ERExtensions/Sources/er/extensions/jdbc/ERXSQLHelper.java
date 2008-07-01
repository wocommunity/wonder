package er.extensions.jdbc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSUtilities;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXModelGroup;
import er.extensions.eof.qualifiers.ERXFullTextQualifier;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * ERXSQLHelper provides support for additional database-vender-specific
 * operations that JDBCPlugIn does not cover.
 * 
 * By default this will try to load the class
 * er.extensions.ERXSQLHelper$DatabaseVendorSQLHelper. For instance,
 * er.extensions.ERXSQLHelper$FrontBaseSQLHelper. If you want to change the
 * helper that is used for a particular database vendor, then override
 * FrontBase.SQLHelper, Oracle.SQLHelper, etc. Case is important (because the
 * vendor name is prepended to the class name), and should match what your
 * JDBCPlugIn.databaseProductName() returns.
 * 
 * @property databaseProductName.SQLHelper the class name of the SQLHelper for
 *           the database product name
 * 
 * @author mschrag
 */
public class ERXSQLHelper {
	/** logging support */
	public static final Logger log = Logger.getLogger(ERXSQLHelper.class);

	private static Map<String, ERXSQLHelper> _sqlHelperMap = new HashMap<String, ERXSQLHelper>();

	private JDBCPlugIn _plugin;

	public void prepareConnectionForSchemaChange(EOEditingContext ec, EOModel model) {
		// do nothing by default
	}

	public void restoreConnectionSettingsAfterSchemaChange(EOEditingContext ec, EOModel model) {
		// do nothing by default
	}

	public boolean shouldExecute(String sql) {
		return true;
	}

	/**
	 * creates SQL to create tables for the specified Entities. This can be used
	 * with EOUtilities rawRowsForSQL method to create the tables.
	 * 
	 * @param entities
	 *            a NSArray containing the entities for which create table
	 *            statements should be generated or null if all entitites in the
	 *            model should be used.
	 * @param modelName
	 *            the name of the EOModel
	 * @param optionsCreate
	 * 
	 * @return a <code>String</code> containing SQL statements to create
	 *         tables
	 */
	public String createSchemaSQLForEntitiesInModelWithNameAndOptions(NSArray<EOEntity> entities, String modelName, NSDictionary optionsCreate) {
		EOModel m = ERXEOAccessUtilities.modelGroup(null).modelNamed(modelName);
		return createSchemaSQLForEntitiesInModelAndOptions(entities, m, optionsCreate);
	}

	/**
	 * Reimplementation that does not try to the shared objects. You should exit
	 * soon after calling this, as it may or may not leave channels open. It is
	 * simply to geenrate sql.
	 * 
	 * @param model
	 * @param coordinator
	 */
	private EODatabaseContext databaseContextForModel(EOModel model, EOObjectStoreCoordinator coordinator) {
		EODatabaseContext dbc = null;
		NSArray objectStores = coordinator.cooperatingObjectStores();
		int i = 0;
		for (int c = objectStores.count(); i < c; i++) {
			Object objectStore = objectStores.objectAtIndex(i);
			if ((objectStore instanceof EODatabaseContext) && ((EODatabaseContext) objectStore).database().addModelIfCompatible(model)) {
				dbc = (EODatabaseContext) objectStore;
			}
		}

		if (dbc == null) {
			dbc = (EODatabaseContext) _NSUtilities.instantiateObject(EODatabaseContext.contextClassToRegister(), new Class[] { com.webobjects.eoaccess.EODatabase.class }, new Object[] { new EODatabase(model) }, true, false);
			coordinator.addCooperatingObjectStore(dbc);
		}
		return dbc;
	}

	/**
	 * creates SQL to create tables for the specified Entities. This can be used
	 * with EOUtilities rawRowsForSQL method to create the tables.
	 * 
	 * @param entities
	 *            a NSArray containing the entities for which create table
	 *            statements should be generated or null if all entitites in the
	 *            model should be used.
	 * @param model
	 *            the EOModel
	 * @param optionsCreate
	 *            a NSDictionary containing the different options
	 * 
	 * @return a <code>String</code> containing SQL statements to create
	 *         tables
	 */
	@SuppressWarnings("unchecked")
	public String createSchemaSQLForEntitiesInModelAndOptions(NSArray<EOEntity> entities, EOModel model, NSDictionary optionsCreate) {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			EODatabaseContext databaseContext = databaseContextForModel(model, (EOObjectStoreCoordinator) ec.rootObjectStore());
			// AK the default implementation loads the shared objects, and when
			// they don't exist, throw an an error
			// which is not very useful for schema generation
			// But you would probably want to exit soon after calling this....
			// EODatabaseContext databaseContext =
			// EODatabaseContext.registeredDatabaseContextForModel(model, ec);
			if (entities == null) {
				Enumeration<EOEntity> e = model.entities().objectEnumerator();
				NSMutableArray<EOEntity> ar = new NSMutableArray<EOEntity>();
				while (e.hasMoreElements()) {
					EOEntity currentEntity = e.nextElement();
					if (ERXModelGroup.isPrototypeEntity(currentEntity)) {
						// we do not want to add EOXXXPrototypes entities
						continue;
					}
					if (!ERXEOAccessUtilities.entityUsesSeparateTable(currentEntity)) {
						continue;
					}
					ar.addObject(currentEntity);
				}
				entities = ar;
			}
			String result = createSchemaSQLForEntitiesWithOptions(entities, databaseContext, optionsCreate);
			return result;
		}
		finally {
			ec.unlock();
		}
	}

	/**
	 * Creates the schema sql for a set of entities.
	 * 
	 * @param entities
	 *            the entities to create sql for
	 * @param databaseContext
	 *            the database context to use
	 * @param optionsCreate
	 *            the options (@see
	 *            createSchemaSQLForEntitiesInModelWithNameAndOptions)
	 * @return a sql script
	 */
	public String createSchemaSQLForEntitiesWithOptions(NSArray<EOEntity> entities, EODatabaseContext databaseContext, NSDictionary<String, String> optionsCreate) {
		// get the JDBCAdaptor
		EOAdaptorContext ac = databaseContext.adaptorContext();
		EOSynchronizationFactory sf = ((JDBCAdaptor) ac.adaptor()).plugIn().createSynchronizationFactory();
		return sf.schemaCreationScriptForEntities(entities, optionsCreate);
	}

	/**
	 * creates SQL to create tables for the specified Entities. This can be used
	 * with EOUtilities rawRowsForSQL method to create the tables.
	 * 
	 * @param entities
	 *            a NSArray containing the entities for which create table
	 *            statements should be generated or null if all entitites in the
	 *            model should be used.
	 * @param modelName
	 *            the name of the EOModel
	 * @return a <code>String</code> containing SQL statements to create
	 *         tables
	 */
	public String createSchemaSQLForEntitiesInModelWithName(NSArray<EOEntity> entities, String modelName) {
		EOModel model = ERXEOAccessUtilities.modelGroup(null).modelNamed(modelName);
		return createSchemaSQLForEntitiesInModel(entities, model);
	}

	/**
	 * Creates SQL to create tables for the specified Entities. This can be used
	 * with EOUtilities rawRowsForSQL method to create the tables.
	 * 
	 * @param entities
	 *            a NSArray containing the entities for which create table
	 *            statements should be generated or null if all entitites in the
	 *            model should be used.
	 * @param model
	 *            the EOModel
	 * 
	 * @return a <code>String</code> containing SQL statements to create
	 *         tables
	 */
	public String createSchemaSQLForEntitiesInModel(NSArray<EOEntity> entities, EOModel model) {
		return createSchemaSQLForEntitiesInModelAndOptions(entities, model, defaultOptionDictionary(true, true));
	}

	/**
	 * Creates an option dictionary to use with the other methods
	 * 
	 * @param create
	 *            add create statements
	 * @param drop
	 *            add drop statements <br/><br/>This method uses the following
	 *            defaults options:
	 *            <ul>
	 *            <li>EOSchemaGeneration.DropTablesKey=YES if drop</li>
	 *            <li>EOSchemaGeneration.DropPrimaryKeySupportKey=YES if drop</li>
	 *            <li>EOSchemaGeneration.CreateTablesKey=YES if create</li>
	 *            <li>EOSchemaGeneration.CreatePrimaryKeySupportKey=YES if
	 *            create</li>
	 *            <li>EOSchemaGeneration.PrimaryKeyConstraintsKey=YES if create</li>
	 *            <li>EOSchemaGeneration.ForeignKeyConstraintsKey=YES if create</li>
	 *            <li>EOSchemaGeneration.CreateDatabaseKey=NO</li>
	 *            <li>EOSchemaGeneration.DropDatabaseKey=NO</li>
	 *            </ul>
	 *            <br/><br>
	 *            Possible values are <code>YES</code> and <code>NO</code>
	 * 
	 * @return a <code>String</code> containing SQL statements to create
	 *         tables
	 */
	public NSMutableDictionary<String, String> defaultOptionDictionary(boolean create, boolean drop) {
		NSMutableDictionary<String, String> optionsCreate = new NSMutableDictionary<String, String>();
		optionsCreate.setObjectForKey((drop) ? "YES" : "NO", EOSchemaGeneration.DropTablesKey);
		optionsCreate.setObjectForKey((drop) ? "YES" : "NO", EOSchemaGeneration.DropPrimaryKeySupportKey);
		optionsCreate.setObjectForKey((create) ? "YES" : "NO", EOSchemaGeneration.CreateTablesKey);
		optionsCreate.setObjectForKey((create) ? "YES" : "NO", EOSchemaGeneration.CreatePrimaryKeySupportKey);
		optionsCreate.setObjectForKey((create) ? "YES" : "NO", EOSchemaGeneration.PrimaryKeyConstraintsKey);
		optionsCreate.setObjectForKey((create) ? "YES" : "NO", EOSchemaGeneration.ForeignKeyConstraintsKey);
		optionsCreate.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
		optionsCreate.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
		return optionsCreate;
	}

	/**
	 * creates SQL to create tables for the specified Entities. This can be used
	 * with EOUtilities rawRowsForSQL method to create the tables.
	 * 
	 * @param entities
	 *            a NSArray containing the entities for which create table
	 *            statements should be generated or null if all entitites in the
	 *            model should be used.
	 * @param databaseContext
	 *            the databaseContext
	 * 
	 * @param create
	 *            if true, tables and keys are created
	 * @param drop
	 *            if true, tables and keys are dropped
	 * @return a <code>String</code> containing SQL statements to create
	 *         tables
	 */
	public String createSchemaSQLForEntitiesInDatabaseContext(NSArray<EOEntity> entities, EODatabaseContext databaseContext, boolean create, boolean drop) {
		return createSchemaSQLForEntitiesWithOptions(entities, databaseContext, defaultOptionDictionary(create, drop));
	}

	public String createIndexSQLForEntities(NSArray<EOEntity> entities) {
		return createIndexSQLForEntities(entities, null);
	}

	@SuppressWarnings("unchecked")
	public String createIndexSQLForEntities(NSArray<EOEntity> entities, NSArray<String> externalTypesToIgnore) {
		if (externalTypesToIgnore == null) {
			externalTypesToIgnore = NSArray.EmptyArray;
		}
		if (entities == null || entities.count() == 0) {
			return "";
		}
		int i = 0;
		String oldIndexName = null;
		String lineSeparator = System.getProperty("line.separator");
		StringBuffer buf = new StringBuffer();

		EOEntity ent = entities.objectAtIndex(0);
		String modelName = ent.model().name();
		String commandSeparator = commandSeparatorString();

		for (Enumeration entitiesEnum = entities.objectEnumerator(); entitiesEnum.hasMoreElements();) {
			EOEntity entity = (EOEntity) entitiesEnum.nextElement();
			// only use this entity if it has its own table
			if (!ERXEOAccessUtilities.entityUsesSeparateTable(entity)) {
				continue;
			}

			NSDictionary<String, Object> d = entity.userInfo();
			NSMutableArray<String> usedColumns = new NSMutableArray<String>();
			for (Enumeration<String> keys = d.keyEnumerator(); keys.hasMoreElements();) {
				String key = keys.nextElement();
				if (key.startsWith("index")) {
					String numbers = key.substring("index".length());
					if (ERXStringUtilities.isDigitsOnly(numbers)) {
						String attributeNames = (String) d.objectForKey(key);
						if (ERXStringUtilities.stringIsNullOrEmpty(attributeNames)) {
							continue;
						}
						String indexName = "c" + System.currentTimeMillis() + new NSTimestamp().getNanos();
						String newIndexName = i == 0 ? indexName : indexName + "_" + i;
						if (oldIndexName == null) {
							oldIndexName = indexName;
						}
						else if (oldIndexName.equals(newIndexName)) {
							indexName += "_" + ++i;
						}
						else {
							i = 0;
						}
						oldIndexName = indexName;
						StringBuffer localBuf = new StringBuffer();
						StringBuffer columnBuf = new StringBuffer();
						boolean validIndex = false;
						localBuf.append("create index " + indexName + " on " + entity.externalName() + "(");
						for (Enumeration<String> attributes = NSArray.componentsSeparatedByString(attributeNames, ",").objectEnumerator(); attributes.hasMoreElements();) {
							String attributeName = attributes.nextElement();
							attributeName = attributeName.trim();
							EOAttribute attribute = entity.attributeNamed(attributeName);
							if (attribute == null) {
								attribute = ERXEOAccessUtilities.attributeWithColumnNameFromEntity(attributeName, entity);
							}
							if (attribute != null && externalTypesToIgnore.indexOfObject(attribute.externalType()) != NSArray.NotFound) {
								continue;
							}
							validIndex = true;
							String columnName = attribute == null ? attributeName : attribute.columnName();
							columnBuf.append(columnName);
							if (attributes.hasMoreElements()) {
								columnBuf.append(", ");
							}
						}
						if (validIndex) {
							String l = columnBuf.toString();
							if (l.endsWith(", ")) {
								l = l.substring(0, l.length() - 2);
							}
							if (usedColumns.indexOfObject(l) == NSArray.NotFound) {
								buf.append(localBuf).append(l);
								usedColumns.addObject(l);
								buf.append(")").append(commandSeparator).append(lineSeparator);
							}
						}
					}
				}
				else if (key.equals("additionalIndexes")) {
					// this is a space separated list of column or attribute
					// names
					String value = (String) d.objectForKey(key);
					for (Enumeration indexes = NSArray.componentsSeparatedByString(value, " ").objectEnumerator(); indexes.hasMoreElements();) {
						String indexValues = (String) indexes.nextElement();
						if (ERXStringUtilities.stringIsNullOrEmpty(indexValues)) {
							continue;
						}

						// this might be a comma separate list
						String indexName = "c" + System.currentTimeMillis() + new NSTimestamp().getNanos();
						String newIndexName = i == 0 ? indexName : indexName + "_" + i;
						if (oldIndexName == null) {
							oldIndexName = indexName;
						}
						else if (oldIndexName.equals(newIndexName)) {
							indexName += "_" + ++i;
						}
						else {
							i = 0;
						}
						oldIndexName = indexName;

						StringBuffer localBuf = new StringBuffer();
						StringBuffer columnBuf = new StringBuffer();
						boolean validIndex = false;
						localBuf.append("create index " + indexName + " on " + entity.externalName() + "(");
						for (Enumeration e = NSArray.componentsSeparatedByString(indexValues, ",").objectEnumerator(); e.hasMoreElements();) {
							String attributeName = (String) e.nextElement();
							attributeName = attributeName.trim();
							EOAttribute attribute = entity.attributeNamed(attributeName);

							if (attribute == null) {
								attribute = ERXEOAccessUtilities.attributeWithColumnNameFromEntity(attributeName, entity);
							}
							if (attribute != null && externalTypesToIgnore.indexOfObject(attribute.externalType()) != NSArray.NotFound) {
								continue;
							}
							validIndex = true;

							String columnName = attribute == null ? attributeName : attribute.columnName();
							columnBuf.append(columnName);
							if (e.hasMoreElements()) {
								columnBuf.append(", ");
							}
						}
						if (validIndex) {
							String l = columnBuf.toString();
							if (l.endsWith(", ")) {
								l = l.substring(0, l.length() - 2);
							}
							if (usedColumns.indexOfObject(l) == NSArray.NotFound) {
								buf.append(localBuf).append(l);
								usedColumns.addObject(l);
								buf.append(")").append(commandSeparator).append(lineSeparator);
							}
						}
					}
				}
			}
		}
		return buf.toString();
	}

	/**
	 * Returns the last of attributes to fetch for a fetch spec. The entity is
	 * passed in here because it has likely already been looked up for the
	 * particular fetch spec.
	 * 
	 * @param fetchSpec
	 *            the fetch spec
	 * @param entity
	 *            the entity (which should match fetchSpec.entityName())
	 * @return the list of attributes to fetch
	 */
	@SuppressWarnings("unchecked")
	public NSArray<EOAttribute> attributesToFetchForEntity(EOFetchSpecification fetchSpec, EOEntity entity) {
		NSArray<EOAttribute> attributes;
		if (!fetchSpec.fetchesRawRows()) {
			attributes = entity.attributesToFetch();
		}
		else {
			NSMutableArray<EOAttribute> rawRowAttributes = new NSMutableArray<EOAttribute>();
			for (String rawRowKeyPath : (NSArray<String>) fetchSpec.rawRowKeyPaths()) {
				rawRowAttributes.addObject(entity.anyAttributeNamed(rawRowKeyPath));
			}
			attributes = rawRowAttributes.immutableClone();
		}
		return attributes;
	}

	/**
	 * Creates the SQL which is used by the provided EOFetchSpecification,
	 * limited by the given range.
	 * 
	 * @param ec
	 *            the EOEditingContext
	 * @param spec
	 *            the EOFetchSpecification in question
	 * @param start
	 *            start of rows to fetch
	 * @param end
	 *            end of rows to fetch (-1 if not used)
	 * 
	 * @return the EOSQLExpression which the EOFetchSpecification would use
	 */
	public EOSQLExpression sqlExpressionForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec, long start, long end) {
		return sqlExpressionForFetchSpecification(ec, spec, start, end, null);
	}

	/**
	 * Creates the SQL which is used by the provided EOFetchSpecification,
	 * limited by the given range.
	 * 
	 * @param ec
	 *            the EOEditingContext
	 * @param spec
	 *            the EOFetchSpecification in question
	 * @param start
	 *            start of rows to fetch
	 * @param end
	 *            end of rows to fetch (-1 if not used)
	 * @param attributes
	 *            the attributes to fetch from the given entity
	 * 
	 * @return the EOSQLExpression which the EOFetchSpecification would use
	 */
	public EOSQLExpression sqlExpressionForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec, long start, long end, NSArray<EOAttribute> attributes) {
		EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
		EOModel model = entity.model();
		EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, model.name());
		EOAdaptor adaptor = dbc.adaptorContext().adaptor();
		EOSQLExpressionFactory sqlFactory = adaptor.expressionFactory();
		spec = (EOFetchSpecification) spec.clone();

		EOQualifier qualifier = spec.qualifier();
		if (qualifier != null) {
			qualifier = EOQualifierSQLGeneration.Support._schemaBasedQualifierWithRootEntity(qualifier, entity);
		}
		if (qualifier != spec.qualifier()) {
			spec.setQualifier(qualifier);
		}
		if (spec.fetchLimit() > 0) {
			spec.setFetchLimit(0);
			spec.setPromptsAfterFetchLimit(false);
		}
		spec = ERXEOAccessUtilities.localizeFetchSpecification(ec, spec);
		String url = (String) model.connectionDictionary().objectForKey("URL");
		String lowerCaseURL = (url != null ? url.toLowerCase() : "");
		if (attributes == null) {
			attributes = attributesToFetchForEntity(spec, entity);
		}
		EOSQLExpression sqlExpr = sqlFactory.selectStatementForAttributes(attributes, false, spec, entity);
		String sql = sqlExpr.statement();
		if (end >= 0) {
			sql = limitExpressionForSQL(sqlExpr, spec, sql, start, end);
			sqlExpr.setStatement(sql);
		}
		return sqlExpr;
	}

	protected String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
		throw new UnsupportedOperationException("There is no " + getClass().getSimpleName() + " implementation for generating limit expressions.");
	}

	/**
	 * Removes an attribute from the select list.
	 * 
	 * @param attribute
	 *            the attribute to remove from the select list
	 * @param sqlExpression
	 *            the expression to remove from
	 */
	public void removeSelectFromExpression(EOAttribute attribute, EOSQLExpression sqlExpression) {
		// MS: This is a bit brute force, but there's not really a nicer way to
		// do this, unfortunately
		String sql = sqlExpression.statement();
		String attributeSql = sqlExpression.sqlStringForAttribute(attribute);
		String replaceSql = sql.replaceFirst(", " + attributeSql, "");
		if (replaceSql.length() == sql.length()) {
			replaceSql = sql.replaceFirst(attributeSql + ", ", "");
		}
		sqlExpression.setStatement(replaceSql);
	}

	/**
	 * Returns the attribute read format for an aggregate function for a
	 * particular column with a name.
	 * 
	 * @param functionName
	 *            the aggregate function to generate
	 * @param columnName
	 *            the column name to aggregate on
	 * @param aggregateName
	 *            the name to assign to the aggregate result
	 * @return the generated read format
	 */
	public String readFormatForAggregateFunction(String functionName, String columnName, String aggregateName) {
		StringBuffer sb = new StringBuffer();
		sb.append(functionName);
		sb.append("(");
		sb.append(columnName);
		sb.append(")");
		if (aggregateName != null) {
			sb.append(" AS ");
			sb.append(aggregateName);
		}
		return sb.toString();
	}

	/**
	 * Adds itemString to a comma-separated list. If listString already has
	 * entries, this method appends a comma followed by itemString. There is no
	 * good way to hook in and use EOSQLExpression's version of this, so we have
	 * our own copy of it.
	 * 
	 * @param itemString
	 *            the item to append
	 * @param listString
	 *            the list buffer
	 */
	public void appendItemToListString(String itemString, StringBuffer listString) {
		if (listString.length() > 0) {
			listString.append(", ");
		}
		listString.append(itemString);
	}

	/**
	 * Adds a group-by clause to the given SQL Expression based on the list of
	 * attributes defined in the given fetch spec.
	 * 
	 * @param editingContext
	 *            the editing context to lookup entities with
	 * @param fetchSpec
	 *            the fetch spec to retrieve attributes from
	 * @param expression
	 *            the sql expression to add a "group by" clause to
	 */
	public void addGroupByClauseToExpression(EOEditingContext editingContext, EOFetchSpecification fetchSpec, EOSQLExpression expression) {
		EOEntity entity = ERXEOAccessUtilities.entityNamed(editingContext, fetchSpec.entityName());
		addGroupByClauseToExpression(attributesToFetchForEntity(fetchSpec, entity), expression);
	}

	/**
	 * Returns the index in the expression's statement where order by clauses
	 * should be inserted.
	 * 
	 * @param expression
	 *            the expression to look into
	 * @return the index into statement where the order by should be inserted
	 */
	public int _orderByIndex(EOSQLExpression expression) {
		String sql = expression.statement();
		int orderByInsertIndex = sql.lastIndexOf(" LIMIT ");
		if (orderByInsertIndex == -1) {
			orderByInsertIndex = sql.length();
		}
		return orderByInsertIndex;
	}

	/**
	 * Returns the index in the expression's statement where group by and having
	 * clauses should be inserted.
	 * 
	 * @param expression
	 *            the expression to look into
	 * @return the index into statement where the group by should be inserted
	 */
	public int _groupByOrHavingIndex(EOSQLExpression expression) {
		String sql = expression.statement();
		int groupByInsertIndex = sql.lastIndexOf(" ORDER BY ");
		if (groupByInsertIndex == -1) {
			groupByInsertIndex = sql.lastIndexOf(" LIMIT ");
			if (groupByInsertIndex == -1) {
				groupByInsertIndex = sql.length();
			}
		}
		return groupByInsertIndex;
	}

	/**
	 * Adds a group-by clause to the given SQL Expression based on the given
	 * list of attributes.
	 * 
	 * @param attributes
	 *            the list of attributes to group by
	 * @param expression
	 *            the sql expression to add a "group by" clause to
	 */
	public void addGroupByClauseToExpression(NSArray<EOAttribute> attributes, EOSQLExpression expression) {
		StringBuffer groupByBuffer = new StringBuffer();
		for (EOAttribute attribute : attributes) {
			String attributeSqlString = expression.sqlStringForAttribute(attribute);
			attributeSqlString = expression.formatSQLString(attributeSqlString, attribute.readFormat());
			appendItemToListString(attributeSqlString, groupByBuffer);
		}
		groupByBuffer.insert(0, " GROUP BY ");

		StringBuffer sqlBuffer = new StringBuffer(expression.statement());
		sqlBuffer.insert(_groupByOrHavingIndex(expression), groupByBuffer);
		expression.setStatement(sqlBuffer.toString());
	}

	/**
	 * Adds a " having count(*) > x" clause to a group by expression.
	 * 
	 * @param selector
	 *            the comparison selector -- just like EOKeyValueQualifier
	 * @param value
	 *            the value to compare against
	 * @param expression
	 *            the expression to modify
	 */
	public void addHavingCountClauseToExpression(NSSelector selector, int value, EOSQLExpression expression) {
		Integer integerValue = Integer.valueOf(value);
		String operatorString = expression.sqlStringForSelector(selector, integerValue);

		StringBuffer havingBuffer = new StringBuffer();
		havingBuffer.append(" HAVING COUNT(*) ");
		havingBuffer.append(operatorString);
		havingBuffer.append(" ");
		havingBuffer.append(integerValue);

		StringBuffer sqlBuffer = new StringBuffer(expression.statement());
		sqlBuffer.insert(_groupByOrHavingIndex(expression), havingBuffer);
		expression.setStatement(sqlBuffer.toString());
	}

	/**
	 * Returns the SQL expression for a regular expression query.
	 * 
	 * @param key
	 * @param value
	 */
	public String sqlForRegularExpressionQuery(String key, String value) {
		throw new UnsupportedOperationException("There is no " + getClass().getSimpleName() + " implementation for generating regex expressions.");
	}

	/**
	 * Returns the SQL expression for a full text search query.
	 * 
	 * @param qualifier
	 *            the full text qualifier
	 * @param expression
	 *            the EOSQLExpression context
	 * @return a SQL expression
	 */
	public String sqlForFullTextQuery(ERXFullTextQualifier qualifier, EOSQLExpression expression) {
		throw new UnsupportedOperationException("There is no " + getClass().getSimpleName() + " implementation for generating full text expressions.");
	}

	/**
	 * Returns the SQL expression for creating a unique index on the given set
	 * of columns
	 * 
	 * @param indexName
	 *            the name of the index to create
	 * @param expression
	 *            the EOSQLExpression context
	 * @param columnNames
	 *            the list of column names to index on
	 * @return a SQL expression
	 */
	public String sqlForCreateUniqueIndex(String indexName, String tableName, String... columnNames) {
		return sqlForCreateUniqueIndex(indexName, tableName, columnIndexesFromColumnNames(columnNames));
	}
	
	/**
	 * Returns the SQL expression for creating a unique index on the given set
	 * of columns
	 * 
	 * @param indexName
	 *            the name of the index to create
	 * @param expression
	 *            the EOSQLExpression context
	 * @param columnIndexes
	 *            the list of columns to index on
	 * @return a SQL expression
	 */
	public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
		throw new UnsupportedOperationException("There is no " + getClass().getSimpleName() + " implementation for generating unique index expressions.");
	}

	/**
	 * Returns the SQL expression for creating an index on the given set
	 * of columns
	 * 
	 * @param indexName
	 *            the name of the index to create
	 * @param expression
	 *            the EOSQLExpression context
	 * @param columnNames
	 *            the list of column names to index on
	 * @return a SQL expression
	 */
	public String sqlForCreateIndex(String indexName, String tableName, String... columnNames) {
		return sqlForCreateIndex(indexName, tableName, columnIndexesFromColumnNames(columnNames));
	}

	protected ColumnIndex[] columnIndexesFromColumnNames(String... columnNames) {
		NSMutableArray<ColumnIndex> columnIndexes = new NSMutableArray<ColumnIndex>();
		for (String columnName : columnNames) {
			columnIndexes.addObject(new ColumnIndex(columnName));
		}
		return columnIndexes.toArray(new ColumnIndex[columnIndexes.count()]);
	}

	/**
	 * Returns the SQL expression for creating an index on the given set
	 * of columns
	 * 
	 * @param indexName
	 *            the name of the index to create
	 * @param expression
	 *            the EOSQLExpression context
	 * @param columnIndexes
	 *            the list of columns to index on
	 * @return a SQL expression
	 */
	public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
		throw new UnsupportedOperationException("There is no " + getClass().getSimpleName() + " implementation for generating index expressions.");
	}

	/**
	 * IndexLimit represents the reference to a column for use in an index
	 * definition along with an optional limit.
	 * 
	 * @author mschrag
	 */
	public static class ColumnIndex {
		private String _columnName;
		private int _length;

		public ColumnIndex(String columnName) {
			this(columnName, 0);
		}

		public ColumnIndex(String columnName, int length) {
			_columnName = columnName;
			_length = length;
		}

		public String columnName() {
			return _columnName;
		}

		public int length() {
			return _length;
		}

		public boolean hasLength() {
			return _length > 0;
		}

		@Override
		public String toString() {
			return "[ColumnIndex: columnName = " + _columnName + "; length = " + _length + "]";
		}
	}

	/**
	 * Returns the name of the table to use for database migrations.
	 * 
	 * @return the name of the table to use for database migrations
	 */
	public String migrationTableName() {
		return "_dbupdater";
	}

	/**
	 * JDBCAdaptor.externalTypeForJDBCType just returns the first type it finds
	 * instead of trying to find a best match. This can still fail, mind you,
	 * but it should be much better than the EOF default impl.
	 * 
	 * @param adaptor
	 *            the adaptor to retrieve an external type for
	 * @param jdbcType
	 *            the JDBC type number
	 * @return a guess at the external type name to use
	 */
	@SuppressWarnings("unchecked")
	public String externalTypeForJDBCType(JDBCAdaptor adaptor, int jdbcType) {
		String externalType = null;
		NSArray<String> defaultJDBCTypes = null;
		try {
			// MS: This is super dirty, but we can deadlock if we end up trying
			// to request jdbc2Info during a migration. We have to be able to
			// use the adaptor's cached version of this method and so we just
			// have to go in through the backdoor here ...
			Method typeInfoMethod = adaptor.getClass().getDeclaredMethod("typeInfo");
			boolean oldAccessible = typeInfoMethod.isAccessible();
			typeInfoMethod.setAccessible(true);
			try {
				NSDictionary typeInfo = (NSDictionary) typeInfoMethod.invoke(adaptor);

				if (typeInfo != null) {
					String jdbcStringRep = JDBCAdaptor.stringRepresentationForJDBCType(jdbcType);

					String typeInfoStringRep = jdbcStringRep;

					// MS: We need to do a case-insensitive lookup of the type
					// info string representation, because some databases say
					// "VARCHAR" and some "varchar".
					// Awesome.
					for (String possibleTypeInfoStringRep : (NSArray<String>) typeInfo.allKeys()) {
						if (typeInfoStringRep.equalsIgnoreCase(possibleTypeInfoStringRep)) {
							typeInfoStringRep = possibleTypeInfoStringRep;
							break;
						}
					}

					// We're going to guess that the jdbc string rep is a valid
					// type in this adaptor. If it is, then we can use that and
					// it will probably be a better guess than just the first
					// type we run across.
					NSDictionary typeDescription = (NSDictionary) typeInfo.objectForKey(typeInfoStringRep);
					if (typeDescription != null) {
						defaultJDBCTypes = (NSArray<String>) typeDescription.objectForKey("defaultJDBCType");
						if (defaultJDBCTypes != null && defaultJDBCTypes.containsObject(jdbcStringRep)) {
							externalType = typeInfoStringRep;
						}
					}

					if (externalType == null) {
						externalType = adaptor.externalTypeForJDBCType(jdbcType);
					}

					if (externalType == null) {

					}

				}
			}
			finally {
				typeInfoMethod.setAccessible(oldAccessible);
			}
		}
		catch (Exception e) {
			ERXSQLHelper.log.error("Failed to sneakily execute adaptor.typeInfo().", e);
		}

		if (externalType == null) {
			externalType = adaptor.externalTypeForJDBCType(jdbcType);
		}

		// OK .. So we didn't find an exact match, and the superclass
		// basically gave up. So we're going to take what should be a
		// decent guess. If we found a type info that matched the name,
		// but we didn't find a JDBC type name that matched, let's just
		// guess that it's PROBABLY one of the entries from the
		// JDBC type names list.  We're really not any worse off than
		// the complete failure we were 2 lines ago.
		if (externalType == null && defaultJDBCTypes != null) {
			int defaultJDBCTypesCount = defaultJDBCTypes.count();
			if (defaultJDBCTypesCount == 1) {
				externalType = defaultJDBCTypes.objectAtIndex(0);
			}
			else if (defaultJDBCTypesCount == 0) {
				throw new IllegalArgumentException("There is no type that could be found in your database that maps to JDBC Type #" + jdbcType + ".");
			}
			else {
				externalType = defaultJDBCTypes.objectAtIndex(0);
				ERXSQLHelper.log.warn("There was more than one type that in your database that maps to JDBC Type #" + jdbcType + ": " + defaultJDBCTypes + ". We guessed '" + externalType + "'. Cross your fingers.");
			}
		}

		return externalType;
	}

	/**
	 * Returns the number of rows the supplied EOFetchSpecification would
	 * return.
	 * 
	 * @param ec
	 *            the EOEditingContext
	 * @param spec
	 *            the EOFetchSpecification in question
	 * @return the number of rows
	 */
	public int rowCountForFetchSpecification(EOEditingContext ec, EOFetchSpecification spec) {
		int rowCount = -1;
		EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
		EOModel model = entity.model();
		if (spec.fetchLimit() > 0 || spec.sortOrderings() != null) {
			spec = new EOFetchSpecification(spec.entityName(), spec.qualifier(), null);
		}

		EOSQLExpression sql = sqlExpressionForFetchSpecification(ec, spec, 0, -1);
		String statement = sql.statement();
		int index = statement.toLowerCase().indexOf(" from ");
		statement = "select count(*) " + statement.substring(index, statement.length());
		sql.setStatement(statement);
		NSArray result = ERXEOAccessUtilities.rawRowsForSQLExpression(ec, model.name(), sql);

		if (result.count() > 0) {
			NSDictionary dict = (NSDictionary) result.objectAtIndex(0);
			NSArray values = dict.allValues();
			if (values.count() > 0) {
				Object value = values.objectAtIndex(0);
				if (value instanceof Number) {
					return ((Number) value).intValue();
				}
				try {
					int c = Integer.parseInt(value.toString());
					rowCount = c;
				}
				catch (NumberFormatException e) {
					throw new IllegalStateException("sql " + sql + " returned a wrong result, could not convert " + value + " into an int!");
				}
			}
			else {
				throw new IllegalStateException("sql " + sql + " returned no result!");
			}
		}
		else {
			throw new IllegalStateException("sql " + sql + " returned no result!");
		}
		return rowCount;
	}

	/**
	 * Convenience method to get the next unique ID from a sequence.
	 * 
	 * @param ec
	 *            editing context
	 * @param modelName
	 *            name of the model which connects to the database that has the
	 *            sequence in it
	 * @param sequenceName
	 *            name of the sequence
	 * @return next value in the sequence
	 */
	// ENHANCEME: Need a non-oracle specific way of doing this. Should poke
	// around at
	// the adaptor level and see if we can't find something better.
	public Number getNextValFromSequenceNamed(EOEditingContext ec, String modelName, String sequenceName) {
		String sqlString = "select " + sequenceName + ".nextVal from dual";
		NSArray array = EOUtilities.rawRowsForSQL(ec, modelName, sqlString, null);
		if (array.count() == 0) {
			throw new RuntimeException("Unable to generate value from sequence named: " + sequenceName + " in model: " + modelName);
		}
		NSDictionary dictionary = (NSDictionary) array.objectAtIndex(0);
		NSArray valuesArray = dictionary.allValues();
		return (Number) valuesArray.objectAtIndex(0);
	}

	/**
	 * Creates a where clause string " someKey IN ( someValue1,...)". Can
	 * migrate keyPaths.
	 */
	public String sqlWhereClauseStringForKey(EOSQLExpression e, String key, NSArray valueArray) {
		if (valueArray.count() == 0) {
			return "0=1";
		}
		StringBuffer sb = new StringBuffer();
		NSArray attributePath = ERXEOAccessUtilities.attributePathForKeyPath(e.entity(), key);
		EOAttribute attribute = (EOAttribute) attributePath.lastObject();
		String sqlName;
		if (attributePath.count() > 1) {
			sqlName = e.sqlStringForAttributePath(attributePath);
		}
		else {
			sqlName = e.sqlStringForAttribute(attribute);
		}

		int maxPerQuery = 256;

		// Need to wrap this SQL in parens if there are multiple grougps
		if (valueArray.count() > maxPerQuery) {
			sb.append(" ( ");
		}

		for (int j = 0; j < valueArray.count(); j += maxPerQuery) {
			int currentSize = (j + (maxPerQuery - 1) < valueArray.count() ? maxPerQuery : ((valueArray.count() % maxPerQuery)));
			sb.append(sqlName);
			sb.append(" IN ");
			sb.append("(");
			for (int i = j; i < j + currentSize; i++) {
				if (i > j) {
					sb.append(", ");
				}
				Object value = valueArray.objectAtIndex(i);
				// AK : crude hack for queries with number constants.
				// Apparently
				// EOAttribute.adaptorValueByConvertingAttributeValue() doesn't
				// actually return a suitable value
				if (value instanceof ERXConstant.NumberConstant) {
					value = new Long(((Number) value).longValue());
				}
				else {
					value = formatValueForAttribute(e, value, attribute, key);
				}
				sb.append(value);
			}
			sb.append(")");
			if (j < valueArray.count() - maxPerQuery) {
				sb.append(" OR ");
			}
		}

		if (valueArray.count() > maxPerQuery) {
			sb.append(" ) ");
		}
		return sb.toString();
	}

	protected String formatValueForAttribute(EOSQLExpression expression, Object value, EOAttribute attribute, String key) {
		return expression.sqlStringForValue(value, key);
	}
	
	/**
	 * Splits semicolon-separate sql statements into an array of strings
	 * 
	 * @param sql
	 *            a multi-line sql statement
	 * @return an array of sql statements
	 */
	public NSArray<String> splitSQLStatements(String sql) {
		NSMutableArray<String> statements = new NSMutableArray<String>();
		if (sql != null) {
			StringBuffer statementBuffer = new StringBuffer();
			int length = sql.length();
			boolean inQuotes = false;
			for (int i = 0; i < length; i++) {
				char ch = sql.charAt(i);
				if (ch == '\r' || ch == '\n') {
					// ignore
				}
				// MS: Should this use commandSeparatorString?
				else if (!inQuotes && ch == ';') {
					String statement = statementBuffer.toString().trim();
					if (statement.length() > 0) {
						statements.addObject(statement);
					}
					statementBuffer.setLength(0);
				}
				else {
					// Support for escaping apostrophes, e.g. 'Mike\'s Code' 
					if (inQuotes && ch == '\\') {
						statementBuffer.append(ch);
						ch = sql.charAt(++ i);
					}
					else if (ch == '\'') {
						inQuotes = !inQuotes;
					}
					statementBuffer.append(ch);
				}
			}
			String statement = statementBuffer.toString().trim();
			if (statement.length() > 0) {
				statements.addObject(statement);
			}
		}
		return statements;
	}

	/**
	 * Splits the SQL statements from the given input stream
	 * 
	 * @param is
	 *            the input stream to read from
	 * @return an array of SQL statements
	 * @throws IOException
	 *             if there is a problem reading the stream
	 */
	public NSArray<String> splitSQLStatementsFromInputStream(InputStream is) throws IOException {
		return splitSQLStatements(ERXStringUtilities.stringFromInputStream(is));
	}

	/**
	 * Splits the SQL statements from the given file.
	 * 
	 * @param f
	 *            the file to read from
	 * @return an array of SQL statements
	 * @throws IOException
	 *             if there is a problem reading the stream
	 */
	public NSArray<String> splitSQLStatementsFromFile(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		try {
			BufferedInputStream bis = new BufferedInputStream(fis);
			return splitSQLStatementsFromInputStream(bis);
		}
		finally {
			fis.close();
		}
	}

	protected String commandSeparatorString() {
		String lineSeparator = System.getProperty("line.separator");
		return ";" + lineSeparator;
	}

	public NSMutableArray<String> columnNamesFromColumnIndexes(ColumnIndex... columnIndexes) {
		NSMutableArray<String> columnNames = new NSMutableArray<String>();
		for (ColumnIndex columnIndex : columnIndexes) {
			columnNames.addObject(columnIndex.columnName());
		}
		return columnNames;
	}

	public static ERXSQLHelper newSQLHelper(EOSQLExpression expression) {
		// This is REALLY hacky.
		String className = expression.getClass().getName();
		int dotIndex = className.lastIndexOf('$');
		if (dotIndex == -1) {
			dotIndex = className.lastIndexOf('.');
		}
		int expressionIndex = className.lastIndexOf("Expression");
		if (expressionIndex == -1) {
			throw new RuntimeException("Failed to create sql helper for expression " + expression + ".");
		}
		String databaseProductName = className.substring(dotIndex + 1, expressionIndex);
		return ERXSQLHelper.newSQLHelper(databaseProductName);
	}

	public static ERXSQLHelper newSQLHelper(EOEditingContext ec, String modelName) {
		ec.lock();
		try {
			EODatabaseContext databaseContext = EOUtilities.databaseContextForModelNamed(ec, modelName);
			return ERXSQLHelper.newSQLHelper(databaseContext);
		}
		finally {
			ec.unlock();
		}
	}

	public static ERXSQLHelper newSQLHelper(EODatabaseContext databaseContext) {
		JDBCAdaptor adaptor = (JDBCAdaptor) databaseContext.database().adaptor();
		return ERXSQLHelper.newSQLHelper(adaptor);
	}

	public static ERXSQLHelper newSQLHelper(EODatabaseChannel databaseChannel) {
		JDBCAdaptor adaptor = (JDBCAdaptor) databaseChannel.adaptorChannel().adaptorContext().adaptor();
		return ERXSQLHelper.newSQLHelper(adaptor);
	}

	public static ERXSQLHelper newSQLHelper(EOAdaptorChannel adaptorChannel) {
		JDBCAdaptor adaptor = (JDBCAdaptor) adaptorChannel.adaptorContext().adaptor();
		return ERXSQLHelper.newSQLHelper(adaptor);
	}

	public static ERXSQLHelper newSQLHelper(JDBCAdaptor adaptor) {
		JDBCPlugIn plugin = adaptor.plugIn();
		return ERXSQLHelper.newSQLHelper(plugin);
	}

	public static ERXSQLHelper newSQLHelper(JDBCPlugIn plugin) {
		String databaseProductName = plugin.databaseProductName();
		return ERXSQLHelper.newSQLHelper(databaseProductName);
	}

	public static ERXSQLHelper newSQLHelper(EOModel model) {
		ERXSQLHelper helper = null;
		EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);
		if (adaptor instanceof JDBCAdaptor) {
			JDBCAdaptor jdbc = (JDBCAdaptor) adaptor;
			helper = ERXSQLHelper.newSQLHelper(jdbc);
		}
		return helper;
	}

	public static ERXSQLHelper newSQLHelper(String databaseProductName) {
		synchronized (_sqlHelperMap) {
			ERXSQLHelper sqlHelper = _sqlHelperMap.get(databaseProductName);
			if (sqlHelper == null) {
				try {
					String sqlHelperClassName = ERXProperties.stringForKey(databaseProductName + ".SQLHelper");
					if (sqlHelperClassName == null) {
						if (databaseProductName.equalsIgnoreCase("frontbase")) {
							sqlHelper = new FrontBaseSQLHelper();
						}
						else if (databaseProductName.equalsIgnoreCase("mysql")) {
							sqlHelper = new MySQLSQLHelper();
						}
						else if (databaseProductName.equalsIgnoreCase("oracle")) {
							sqlHelper = new OracleSQLHelper();
						}
						else if (databaseProductName.equalsIgnoreCase("postgresql")) {
							sqlHelper = new PostgresqlSQLHelper();
						}
						else if (databaseProductName.equalsIgnoreCase("openbase")) {
							sqlHelper = new OpenBaseSQLHelper();
						}
						else if (databaseProductName.equalsIgnoreCase("derby")) {
							sqlHelper = new DerbySQLHelper();
						}
						else {
							try {
								sqlHelper = (ERXSQLHelper) Class.forName(ERXSQLHelper.class.getName() + "$" + databaseProductName + "SQLHelper").newInstance();
							}
							catch (ClassNotFoundException e) {
								sqlHelper = new ERXSQLHelper();
							}
						}
					}
					else {
						sqlHelper = (ERXSQLHelper) Class.forName(sqlHelperClassName).newInstance();
					}
					_sqlHelperMap.put(databaseProductName, sqlHelper);
				}
				catch (Exception e) {
					throw new NSForwardException(e, "Failed to create sql helper for the database with the product name '" + databaseProductName + "'.");
				}
			}
			return sqlHelper;
		}
	}

	public static class EROracleSQLHelper extends ERXSQLHelper.OracleSQLHelper {
	}

	public static class OracleSQLHelper extends ERXSQLHelper {
		/**
		 * oracle 9 has a maximum length of 30 characters for table names,
		 * column names and constraint names Foreign key constraint names are
		 * defined like this from the plugin:<br/><br/>
		 * 
		 * TABLENAME_FOEREIGNKEYNAME_FK <br/><br/>
		 * 
		 * The whole statement looks like this:<br/><br/>
		 * 
		 * ALTER TABLE [TABLENAME] ADD CONSTRAINT [CONSTRAINTNAME] FOREIGN KEY
		 * ([FK]) REFERENCES [DESTINATION_TABLE] ([PK]) DEFERRABLE INITIALLY
		 * DEFERRED
		 * 
		 * THIS means that the tablename and the columnname together cannot be
		 * longer than 26 characters.<br/><br/>
		 * 
		 * This method checks each foreign key constraint name and if it is
		 * longer than 30 characters its replaced with a unique name.
		 * 
		 * @see createSchemaSQLForEntitiesInModelWithNameAndOptions
		 */
		@Override
		public String createSchemaSQLForEntitiesInModelWithNameAndOptions(NSArray<EOEntity> entities, String modelName, NSDictionary optionsCreate) {
			String oldConstraintName = null;
			int i = 0;
			String s = super.createSchemaSQLForEntitiesInModelWithNameAndOptions(entities, modelName, optionsCreate);
			NSArray a = NSArray.componentsSeparatedByString(s, "/");
			StringBuffer buf = new StringBuffer(s.length());
			Pattern pattern = Pattern.compile(".*ALTER TABLE .* ADD CONSTRAINT (.*) FOREIGN KEY .* REFERENCES .* \\(.*\\) DEFERRABLE INITIALLY DEFERRED.*");
			Pattern pattern2 = Pattern.compile("(.*ALTER TABLE .* ADD CONSTRAINT ).*( FOREIGN KEY .* REFERENCES .* \\(.*\\) DEFERRABLE INITIALLY DEFERRED.*)");
			String lineSeparator = System.getProperty("line.separator");

			for (Enumeration e = a.objectEnumerator(); e.hasMoreElements();) {
				String statementLine = (String) e.nextElement();
				NSArray b = NSArray.componentsSeparatedByString(statementLine, lineSeparator);
				for (Enumeration e1 = b.objectEnumerator(); e1.hasMoreElements();) {
					String statement = (String) e1.nextElement();
					if (!pattern.matcher(statement).matches()) {
						buf.append(statement);
						buf.append(lineSeparator);
						continue;
					}

					String constraintName = pattern.matcher(statement).replaceAll("$1");
					if (constraintName.length() <= 30) {
						buf.append(statement);
						buf.append(lineSeparator);
						continue;
					}

					constraintName = "fk" + System.currentTimeMillis() + new NSTimestamp().getNanos();
					String newConstraintName = i == 0 ? constraintName : constraintName + "_" + i;

					if (oldConstraintName == null) {
						oldConstraintName = constraintName;
					}
					else if (oldConstraintName.equals(newConstraintName)) {
						constraintName += "_" + ++i;
					}
					else {
						i = 0;
					}
					oldConstraintName = constraintName;

					String newConstraint = pattern2.matcher(statement).replaceAll("$1" + constraintName + "$2");
					buf.append(newConstraint);
					buf.append(lineSeparator);
				}
				if (e.hasMoreElements()) {
					buf.append("/");
				}
			}
			System.out.println("finished!");
			return buf.toString();
		}

		@Override
		protected String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
			String limitSQL;
			/*
			 * Oracle can make you puke... These are grabbed from tips all over
			 * the net and I can't test them as it doesn't even install on OSX.
			 * Pick your poison.
			 */
			int debug = ERXProperties.intForKeyWithDefault("OracleBatchMode", 3);
			if (debug == 1) {
				// this only works for the first page
				limitSQL = "select * from (" + sql + ") where rownum between " + (start + 1) + " and " + end;
			}
			else if (debug == 2) {
				// this doesn't work at all when have have *no* order by
				limitSQL = "select * from (" + "select " + expression.listString() + ", row_number() over (" + expression.orderByString() + ") as eo_rownum from (" + sql + ")) where eo_rownum between " + (start + 1) + " and " + end;
			}
			else if (debug == 3) {
				// this works, but breaks with horizontal inheritance
				limitSQL = "select * from (" + "select " + expression.listString().replaceAll("[Tt]\\d\\.", "") + ", rownum eo_rownum from (" + sql + ")) where eo_rownum between " + (start + 1) + " and " + end;
			}
			else {
				// this might work, too, but only if we have an ORDER BY
				limitSQL = "select * from (" + "select " + (fetchSpecification.usesDistinct() ? " distinct " : "") + expression.listString() + ", row_number() over (" + expression.orderByString() + ") eo_rownum" + " from " + expression.joinClauseString() + " where " + expression.whereClauseString() + ") where eo_rownum between " + (start + 1) + " and " + end;
			}
			return limitSQL;
		}

		@Override
		protected String commandSeparatorString() {
			String lineSeparator = System.getProperty("line.separator");
			String commandSeparator = lineSeparator + "/" + lineSeparator;
			return commandSeparator;
		}

		@Override
		public String createIndexSQLForEntities(NSArray<EOEntity> entities, NSArray<String> externalTypesToIgnore) {
			NSMutableArray<String> oracleExternalTypesToIgnore = new NSMutableArray<String>();
			if (externalTypesToIgnore != null) {
				oracleExternalTypesToIgnore.addObjectsFromArray(externalTypesToIgnore);
			}
			oracleExternalTypesToIgnore.addObject("BLOB");
			oracleExternalTypesToIgnore.addObject("CLOB");
			return super.createIndexSQLForEntities(entities, oracleExternalTypesToIgnore);

		}

		@Override
		public String sqlForRegularExpressionQuery(String key, String value) {
			return "REGEXP_LIKE(" + key + ", " + value + ")";
		}
	}

	public static class OpenBaseSQLHelper extends ERXSQLHelper {
		@Override
		protected String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
			// Openbase support for limiting result set
			return sql + " return results " + start + " to " + end;
		}
	}

	public static class DerbySQLHelper extends ERXSQLHelper {
		@Override
		public boolean shouldExecute(String sql) {
			return sql != null && !sql.startsWith("--");
		}

		@Override
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			return "CREATE UNIQUE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}

		@Override
		public String migrationTableName() {
			return "dbupdater";
		}
	}

	public static class FrontBaseSQLHelper extends ERXSQLHelper {
		private static final String PREFIX_ISOLATION_LEVEL = "isolation=";
		private static final String PREFIX_LOCKING = "locking=";

		@Override
		public boolean shouldExecute(String sql) {
			boolean shouldExecute = true;
			if (sql.startsWith("SET TRANSACTION ISOLATION LEVEL")) {
				shouldExecute = false;
			}
			else if (sql.startsWith("COMMIT")) {
				// shouldExecute = false;
			}
			return shouldExecute;
		}

		@Override
		protected String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
			// add TOP(start, (end - start)) after the SELECT word
			int index = sql.indexOf("select");
			if (index == -1) {
				index = sql.indexOf("SELECT");
			}
			index += 6;
			String limitSQL = sql.substring(0, index) + " TOP(" + start + ", " + (end - start) + ") " + sql.substring(index + 1, sql.length());
			return limitSQL;
		}

		@Override
		public String sqlForFullTextQuery(ERXFullTextQualifier qualifier, EOSQLExpression expression) {
			StringBuffer sb = new StringBuffer();
			sb.append("satisfies(");
			sb.append(qualifier.indexName());
			sb.append(", '");
			ERXFullTextQualifier.MatchType matchType = qualifier.matchType();
			NSArray<String> terms = qualifier.terms();
			for (String term : terms) {
				String[] termWords = term.split(" ");
				for (String termWord : termWords) {
					sb.append(termWord);
					if (matchType == ERXFullTextQualifier.MatchType.ALL) {
						sb.append('&');
					}
					else if (matchType == ERXFullTextQualifier.MatchType.ANY) {
						sb.append('|');
					}
				}
			}
			// Lop off the last '&' or '|'
			if (terms.count() > 0) {
				sb.setLength(sb.length() - 1);
			}
			sb.append("')");
			return sb.toString();
		}

		@Override
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = columnNamesFromColumnIndexes(columnIndexes);
			return "ALTER TABLE \"" + tableName + "\" ADD CONSTRAINT \"" + indexName + "\" UNIQUE(\"" + new NSArray<String>(columnNames).componentsJoinedByString("\", \"") + "\") INITIALLY IMMEDIATE NOT DEFERRABLE";
		}

		public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = columnNamesFromColumnIndexes(columnIndexes);
			return "CREATE INDEX \""+indexName+"\" ON \""+tableName+"\" (\""+new NSArray<String>(columnNames).componentsJoinedByString("\", \"")+"\")";
		}

		@Override
		public void prepareConnectionForSchemaChange(EOEditingContext ec, EOModel model) {
			ERXEOAccessUtilities.ChannelAction action = new ERXEOAccessUtilities.ChannelAction() {
				@Override
				protected int doPerform(EOAdaptorChannel channel) {
					try {
						ERXJDBCUtilities.executeUpdate(channel, "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE, LOCKING PESSIMISTIC");
					}
					catch (SQLException e) {
						throw new NSForwardException(e);
					}
					return 0;
				}
			};
			action.perform(ec, model.name());
		}

		@Override
		@SuppressWarnings("unchecked")
		public void restoreConnectionSettingsAfterSchemaChange(EOEditingContext ec, EOModel model) {
			// Default settings
			String transactionIsolationLevel = "SERIALIZABLE";
			String lockingDiscipline = "PESSIMISTIC";

			// Guess settings from looking at the url
			String url = (String) model.connectionDictionary().valueForKey("URL");
			NSArray<String> urlComponents = NSArray.componentsSeparatedByString(url, "/");
			for (String urlComponent : urlComponents) {
				if (urlComponent.toLowerCase().startsWith(PREFIX_LOCKING)) {
					lockingDiscipline = urlComponent.substring(PREFIX_LOCKING.length()).toUpperCase();
				}
				else if (urlComponent.toLowerCase().startsWith(PREFIX_ISOLATION_LEVEL)) {
					transactionIsolationLevel = urlComponent.substring(PREFIX_ISOLATION_LEVEL.length()).toUpperCase().replaceAll("_", " ");
				}
			}
			final String sql = "SET TRANSACTION ISOLATION LEVEL " + transactionIsolationLevel + ", LOCKING " + lockingDiscipline;

			ERXEOAccessUtilities.ChannelAction action = new ERXEOAccessUtilities.ChannelAction() {
				@Override
				protected int doPerform(EOAdaptorChannel channel) {
					try {
						ERXJDBCUtilities.executeUpdate(channel, sql);
					}
					catch (SQLException e) {
						throw new NSForwardException(e);
					}
					return 0;
				}
			};
			action.perform(ec, model.name());
		}
	}

	public static class MySQLSQLHelper extends ERXSQLHelper {
		@Override
		protected String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
			return sql + " LIMIT " + start + ", " + (end - start);
		}

		@Override
		public String sqlForRegularExpressionQuery(String key, String value) {
			return key + " REGEXP " + value + "";
		}

		@Override
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			StringBuffer sql = new StringBuffer();
			sql.append("ALTER TABLE `" + tableName + "` ADD UNIQUE `" + indexName + "` (");
			_appendIndexColNames(sql, columnIndexes);
			sql.append(")");
			return sql.toString();
		}
		
		@Override
		public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			StringBuffer sql = new StringBuffer();
			sql.append("CREATE INDEX `"+ indexName + "` ON `"+tableName+"` (");
			_appendIndexColNames(sql, columnIndexes);
			sql.append(")");
			return sql.toString();
		}

		private void _appendIndexColNames(StringBuffer sql, ColumnIndex... columnIndexes) {
			for (int columnIndexNum = 0; columnIndexNum < columnIndexes.length; columnIndexNum++) {
				ColumnIndex columnIndex = columnIndexes[columnIndexNum];
				sql.append("`" + columnIndex.columnName() + "`");
				if (columnIndex.hasLength()) {
					// index limit of 767 bytes for InnoDB, 999 bytes for MyISAM
					// which maps to up to 255 and 333 utf8 characters
					int length = Math.min(columnIndex.length(), 255);
					sql.append("(" + length + ")");
				}
				if (columnIndexNum < columnIndexes.length - 1) {
					sql.append(", ");
				}
			}
		}
	}

	public static class PostgresqlSQLHelper extends ERXSQLHelper {
		@Override
		protected String formatValueForAttribute(EOSQLExpression expression, Object value, EOAttribute attribute, String key) {
			// The Postgres Expression has a problem using bind variables so we
			// have to get the formatted
			// SQL string for a value instead. All Apple provided plugins must
			// use the bind variables
			// however. Frontbase can go either way
			// MS: is expression always instanceof PostgresExpression for
			// postgres?
			// boolean isPostgres =
			// e.getClass().getName().equals("com.webobjects.jdbcadaptor.PostgresqlExpression");
			return expression.formatValueForAttribute(value, attribute);
		}

		@Override
		protected String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
			return sql + " LIMIT " + (end - start) + " OFFSET " + start;
		}

		@Override
		public String sqlForRegularExpressionQuery(String key, String value) {
			return key + " ~* " + value + "";
		}

		/**
		 * For most types, finding the type in jdbc2Info's typeInfo will provide
		 * us with a correct type mapping. For Postgresql, it has the honor of
		 * not actually having a type named "integer," so EOF goes on a hunt for
		 * a type that MIGHT match (which is just bad, btw) and comes up with
		 * "serial".
		 * 
		 * cug: There seems to be also nothing useful for "BLOB", so we return
		 * bytea for Type.BLOB  
		 * 
		 * We know better than EOF.
		 * 
		 * For any other case, we pass it up to the default impl.
		 * 
		 * @param adaptor
		 *            the adaptor to retrieve an external type for
		 * @param jdbcType
		 *            the JDBC type number
		 * @return a guess at the external type name to use
		 */
		@Override
		public String externalTypeForJDBCType(JDBCAdaptor adaptor, int jdbcType) {
			String externalType;
			if (jdbcType == Types.INTEGER) {
				externalType = "int4";
			} else if (jdbcType == Types.BLOB) {
				externalType = "bytea";
			}
			else {
				externalType = super.externalTypeForJDBCType(adaptor, jdbcType);
			}
			return externalType;
		}
		
		/** 
		 * Creates unique index; stolen from the derby helper
		 * 
		 * @author cug - Jun 24, 2008
		 * @see er.extensions.ERXSQLHelper#sqlForCreateUniqueIndex(java.lang.String, java.lang.String, er.extensions.ERXSQLHelper.ColumnIndex[])
		 */
		@Override
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			return "CREATE UNIQUE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}
	}
}
