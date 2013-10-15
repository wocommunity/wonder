package er.extensions.jdbc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorOperation;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSUtilities;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXModelGroup;
import er.extensions.eof.qualifiers.ERXFullTextQualifier;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

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
	
	/** custom JDBC types */
	public interface CustomTypes {
		public static final int INET = 9001;
	}
	
	/** logging support */
	public static final Logger log = Logger.getLogger(ERXSQLHelper.class);

	private static Map<String, ERXSQLHelper> _sqlHelperMap = new HashMap<String, ERXSQLHelper>();

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
	 *            statements should be generated or <code>null</code> if all entities
	 *            in the model should be used.
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
	 * simply to generate sql.
	 * 
	 * @param model
	 * @param coordinator
	 * @return the database context for the given model
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
	 *            statements should be generated or null if all entities in the
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
		return createSchemaSQLForEntitiesWithOptions(entities, databaseContext.adaptorContext().adaptor(), optionsCreate);
	}

	/**
	 * Generates table create statements for a set of entities, then finds all the entities that those entities depend on (in other models) and generates
	 * foreign key statements for those, so you can generate sql for cross-model.
	 * 
	 * @param entities the entities to generate for
	 * @param adaptor the adaptor to use
	 * @return the sql script
	 */
	public String createDependentSchemaSQLForEntities(NSArray<EOEntity> entities, EOAdaptor adaptor) {
		NSMutableDictionary<String, String> optionsCreateTables = new NSMutableDictionary<String, String>();
		optionsCreateTables.setObjectForKey("NO", EOSchemaGeneration.DropTablesKey);
		optionsCreateTables.setObjectForKey("NO", EOSchemaGeneration.DropPrimaryKeySupportKey);
		optionsCreateTables.setObjectForKey("YES", EOSchemaGeneration.CreateTablesKey);
		optionsCreateTables.setObjectForKey("YES", EOSchemaGeneration.CreatePrimaryKeySupportKey);
		optionsCreateTables.setObjectForKey("YES", EOSchemaGeneration.PrimaryKeyConstraintsKey);
		optionsCreateTables.setObjectForKey("NO", EOSchemaGeneration.ForeignKeyConstraintsKey);
		optionsCreateTables.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
		optionsCreateTables.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
		StringBuffer sqlBuffer = new StringBuffer();
		EOSynchronizationFactory sf = ((JDBCAdaptor) adaptor).plugIn().synchronizationFactory();
		String creationScript = sf.schemaCreationScriptForEntities(entities, optionsCreateTables);
		sqlBuffer.append(creationScript);
		
		NSMutableArray<EOEntity> foreignKeyEntities = entities.mutableClone();
		for (EOEntity entity : entities) {
			for (EORelationship relationship : entity.relationships()) {
				if (!relationship.isToMany()) {
					EOEntity destinationEntity = relationship.destinationEntity();
					if (destinationEntity.model() != entity.model()) {
						foreignKeyEntities.addObject(destinationEntity);
					}
				}
			}
		}
		
		NSMutableDictionary<String, String> optionsCreateForeignKeys = new NSMutableDictionary<String, String>();
		optionsCreateForeignKeys.setObjectForKey("NO", EOSchemaGeneration.DropTablesKey);
		optionsCreateForeignKeys.setObjectForKey("NO", EOSchemaGeneration.DropPrimaryKeySupportKey);
		optionsCreateForeignKeys.setObjectForKey("NO", EOSchemaGeneration.CreateTablesKey);
		optionsCreateForeignKeys.setObjectForKey("NO", EOSchemaGeneration.CreatePrimaryKeySupportKey);
		optionsCreateForeignKeys.setObjectForKey("NO", EOSchemaGeneration.PrimaryKeyConstraintsKey);
		optionsCreateForeignKeys.setObjectForKey("YES", EOSchemaGeneration.ForeignKeyConstraintsKey);
		optionsCreateForeignKeys.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
		optionsCreateForeignKeys.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
		String foreignKeyScript = sf.schemaCreationScriptForEntities(foreignKeyEntities, optionsCreateForeignKeys);
		sqlBuffer.append(foreignKeyScript);
		
		return sqlBuffer.toString();
	}
	/**
	 * Creates the schema sql for a set of entities.
	 * 
	 * @param entities
	 *            the entities to create sql for
	 * @param adaptor
	 *            the adaptor to use
	 * @param optionsDictionary
	 *            the options (@see
	 *            createSchemaSQLForEntitiesInModelWithNameAndOptions)
	 * @return a sql script
	 */
	public String createSchemaSQLForEntitiesWithOptions(NSArray<EOEntity> entities, EOAdaptor adaptor, NSDictionary<String, String> optionsDictionary) {
		EOSynchronizationFactory sf = ((JDBCAdaptor) adaptor).plugIn().synchronizationFactory();
		String creationScript = sf.schemaCreationScriptForEntities(entities, optionsDictionary);  
		return creationScript;
	}

	/**
	 * creates SQL to create tables for the specified Entities. This can be used
	 * with EOUtilities rawRowsForSQL method to create the tables.
	 * 
	 * @param entities
	 *            a NSArray containing the entities for which create table
	 *            statements should be generated or null if all entities in the
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
	 *            statements should be generated or null if all entities in the
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
	 *            statements should be generated or null if all entities in the
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
	 * Returns the list of attributes to fetch for a fetch spec. The entity is
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
			for (String rawRowKeyPath : fetchSpec.rawRowKeyPaths()) {
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
	 * Returns the custom query expression hint as a String.  At the moment, if it's an EOSQLExpression, it just returns .statement().
	 * 
	 * @param hint the hint to convert to a String 
	 * @return the hint as a String
	 */
	public String customQueryExpressionHintAsString(Object hint) {
		String sql;
		if (hint instanceof String) {
			sql = (String) hint;
		}
		else if (hint instanceof EOSQLExpression) {
			sql = ((EOSQLExpression)hint).statement();
			if (sql == null) {
				throw new IllegalArgumentException("This EOSQLExpression's statement was null (" + hint + ").");
			}
		}
		else {
			sql = null;
		}
		return sql;
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
		if (attributes == null) {
			attributes = attributesToFetchForEntity(spec, entity);
		}
		EOSQLExpression sqlExpr = sqlFactory.selectStatementForAttributes(attributes, false, spec, entity);
		String sql = sqlExpr.statement();
		if (spec.hints() != null && !spec.hints().isEmpty() && spec.hints().valueForKey(EODatabaseContext.CustomQueryExpressionHintKey) != null) {
			Object hint = spec.hints().valueForKey(EODatabaseContext.CustomQueryExpressionHintKey);
			sql = customQueryExpressionHintAsString(hint);
		}
		if (end >= 0) {
			sql = limitExpressionForSQL(sqlExpr, spec, sql, start, end);
			sqlExpr.setStatement(sql);
		}
		return sqlExpr;
	}

	public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
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
		return readFormatForAggregateFunction(functionName, columnName, aggregateName, false);
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
	 * @param usesDistinct
	 *            <code>true</code> if function should be used on distinct values
	 * @return the generated read format
	 */
	public String readFormatForAggregateFunction(String functionName, String columnName, String aggregateName, boolean usesDistinct) {
		StringBuilder sb = new StringBuilder();
		sb.append(functionName);
		sb.append('(');
		if (usesDistinct) {
			sb.append("distinct ");
		}
		sb.append(columnName);
		sb.append(')');
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
	 * @return the regex SQL
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
	 * @param tableName the name of the containing table
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
	 * @param tableName the name of the containing table
	 * @param columnIndexes
	 *            the list of columns to index on
	 * @return a SQL expression
	 */
	public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
		NSMutableArray<String> columnNames = columnNamesFromColumnIndexes(columnIndexes);
		return "ALTER TABLE \"" + tableName + "\" ADD CONSTRAINT \"" + indexName + "\" UNIQUE(\"" + new NSArray<String>(columnNames).componentsJoinedByString("\", \"") + "\")";
	}

	/**
	 * Returns the SQL expression for creating an index on the given set
	 * of columns
	 * 
	 * @param indexName
	 *            the name of the index to create
	 * @param tableName the name of the containing table
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
	 * @param tableName the name of the containing table
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
	 * Returns the JDBCType that should be used for a varcharLarge column in migrations.
	 * 
	 * @return the JDBCType that should be used for a varcharLarge column in migrations
	 */
	public int varcharLargeJDBCType() {
		return Types.VARCHAR;
	}
	
	/**
	 * Returns the width that should be used for a varcharLarge column in migrations.
	 * 
	 * @return the width that should be used for a varcharLarge column in migrations
	 */
	public int varcharLargeColumnWidth() {
		return 10000000;
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
	 * Returns the JDBC type to use for a given ERXSQLHelper custom type
	 * 
	 * @param jdbcType
	 * 				the ERXSQLHelper custom type
	 * @return the JDBC type to use
	 */
	public int jdbcTypeForCustomType(int jdbcType) {
		int result = jdbcType;

		if (jdbcType == CustomTypes.INET) {
			result = Types.VARCHAR;
		}
		return result;
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
		jdbcType = jdbcTypeForCustomType(jdbcType);
		
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
		NSArray result = null;
		String sql;
		if (spec.hints() == null || spec.hints().isEmpty() || spec.hints().valueForKey(EODatabaseContext.CustomQueryExpressionHintKey) == null) {
			// no hints
			if (spec.fetchLimit() > 0 || spec.sortOrderings() != null) {
				boolean usesDistinct = spec.usesDistinct();
				spec = new EOFetchSpecification(spec.entityName(), spec.qualifier(), null);
				spec.setUsesDistinct(usesDistinct);
			}

			EOSQLExpression sqlExpression = sqlExpressionForFetchSpecification(ec, spec, 0, -1);
			String statement = sqlExpression.statement();
			String listString = sqlExpression.listString();

			String countExpression;
			if (spec.usesDistinct()) {
				countExpression = sqlForCountDistinct(entity);
			} else {
				countExpression = "count(*) ";
			}
			statement = statement.replace(listString, countExpression);
			sqlExpression.setStatement(statement);
			sql = statement;
			result = ERXEOAccessUtilities.rawRowsForSQLExpression(ec, model.name(), sqlExpression);
		}
		else {
			// we have hints
			Object hint = spec.hints().valueForKey(EODatabaseContext.CustomQueryExpressionHintKey);
			sql = ERXSQLHelper.newSQLHelper(model).customQueryExpressionHintAsString(hint);
			// MS: This looks super sketchy ...
			if (sql.endsWith(";")) {
				sql = sql.substring(0, sql.length() - 1);
			}
			sql = "select count(*) from " + sqlForSubquery(sql, "result_count_temp_table");
			result = EOUtilities.rawRowsForSQL(ec, model.name(), sql, null);
		}

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
	 * Returns the SQL to count the distinct number of rows. The general implementation doesn't
	 * support composite primary keys and chooses only one primary key column when formatting the
	 * SQL expression.
	 * <p>
	 * Concrete classes may override this implementation to add support for composite
	 * primary keys according to their database specific SQL syntax.
	 *
	 * @param entity the base entity used in this query
	 * @return the formatted SQL count using distinct for the given entity
	 */
	protected String sqlForCountDistinct(EOEntity entity) {
		NSArray<String> primaryKeyAttributeNames = entity.primaryKeyAttributeNames();

		if (primaryKeyAttributeNames.count() > 1) {
			log.warn("Composite primary keys are currently unsupported in rowCountForFetchSpecification, when the spec uses distinct");
		}

		String pkAttributeName = primaryKeyAttributeNames.lastObject();
		String pkColumnName = entity.attributeNamed(pkAttributeName).columnName();

		return "count(distinct " + quoteColumnName("t0." + pkColumnName) + ") ";
	}

	/**
	 * Returns the syntax for using the given query as an aliased subquery in a from-clause.
	 * 
	 * @param subquery the subquery to wrap
	 * @param alias the alias to use
	 * @return the formatted subquery expression
	 */
	protected String sqlForSubquery(String subquery, String alias) {
		return "(" + subquery + ") as " + alias;
	}

	/**
	 * Returns the SQL required to select the next value from the given sequence.  This should
	 * return a single row with a single column.
	 * 
	 * @param sequenceName the name of the sequence
	 * @return the next sequence value
	 */
	protected String sqlForGetNextValFromSequencedNamed(String sequenceName) {
		throw new UnsupportedOperationException("There is no " + getClass().getSimpleName() + " implementation for sequences.");
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
		NSArray array = EOUtilities.rawRowsForSQL(ec, modelName, sqlForGetNextValFromSequencedNamed(sequenceName), null);
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
	 * 
	 * @param e the SQL expression
	 * @param key the name of the key
	 * @param valueArray an array of values to generate an "in" clause for
	 * @return the where clause for the given key
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

		int maxPerQuery = maximumElementPerInClause(e.entity());

		// Need to wrap this SQL in parenthesis if there are multiple groups
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
					value = Long.valueOf(((Number) value).longValue());
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

	/**
	 * The database specific limit, or or most efficient number, of elements in an IN clause in a statement.  If there
	 * are more that this number of elements, additional IN clauses will be generated, ORed to the others.
	 * 
	 * @param entity EOEntity that can be used to fine-tune the result
	 * @return database specific limit, or or most efficient number, of elements in an IN clause in a statement
	 */
	protected int maximumElementPerInClause(EOEntity entity) {
		return 256;
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
			char commandSeparatorChar = commandSeparatorChar();
			Pattern commentPattern = commentPattern();
			StringBuilder statementBuffer = new StringBuilder();
			BufferedReader reader = new BufferedReader(new StringReader(sql));
			boolean inQuotes = false;

			try {
				String nextLine = reader.readLine();
				while (nextLine != null) {
					if(!inQuotes) {
						nextLine = nextLine.trim(); // trim only if we not inQuotes
					} else {
						statementBuffer.append('\n'); // we are in Quotes but got a new Line
					}
					
					// Skip blank lines and new lines starting with the comment pattern
					if (nextLine.length() == 0 ||
							(statementBuffer.length() == 0 && commentPattern.matcher(nextLine).find())) {
						nextLine = reader.readLine();
						continue;
					}

					// Determine if the line ends inside a single quoted string
					int length = nextLine.length();
					char ch = 0;
					char prev = 0;
					for (int i = 0; i < length; i++) {
						ch = nextLine.charAt(i);
						// Determine if we are in a quoted string, but ignore escaped apostrophes, e.g. 'Mike\'s Code' 
						if (inQuotes && ch == '\\') {
							i++;
						}
						else if (ch == '\'') {
							inQuotes = !inQuotes;
						}
						else if (ch == '-' && prev == '-' && !inQuotes) {
							statementBuffer.deleteCharAt(statementBuffer.length() - 1);
							break;
						}
						if (inQuotes || ch != commandSeparatorChar) {
							statementBuffer.append(ch);
						}
						prev = ch;
					}

					// If we are not in a quoted string, either this is the end of the command or we need to 
					// add some whitespace before the continuation of this command
					if (!inQuotes) {
						if (ch == commandSeparatorChar) {
							statements.addObject(statementBuffer.toString().trim());
							statementBuffer.setLength(0);
						}
						else {
							statementBuffer.append(' ');
						}
					}
					
					nextLine = reader.readLine();
				}
				String finalStatement = statementBuffer.toString().trim();
				if (finalStatement.length() > 0) {
					statements.addObject(finalStatement);
				}
			}
			catch (IOException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
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
		String encoding = System.getProperty("file.encoding");
		return splitSQLStatements(ERXStringUtilities.stringIsNullOrEmpty(encoding) ? ERXStringUtilities.stringFromInputStream(is) : ERXStringUtilities.stringFromInputStream(is, encoding));
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

	/**
	 * This is totally cheating ... But I just need the separator character for now.  We
	 * can rewrite the script parser later.  Actually, somewhere on earth there is already
	 * a sql parser or two.  Probably worth getting that one.
	 * 
	 * @return the separator character used by this database
	 */
	protected char commandSeparatorChar() {
		return ';';
	}
	
	protected String commandSeparatorString() {
		String lineSeparator = System.getProperty("line.separator");
		return ";" + lineSeparator;
	}

	/**
	 * Returns a pattern than matches only blank lines.  Subclasses should implement this to return a pattern
	 * matching the vendor specific comment indicator(s).
	 * 
	 * @return regex pattern that indicates this line is an SQL comment
	 */
	protected Pattern commentPattern() {
		return Pattern.compile("^$");
	}
	
	public NSMutableArray<String> columnNamesFromColumnIndexes(ColumnIndex... columnIndexes) {
		NSMutableArray<String> columnNames = new NSMutableArray<String>();
		for (ColumnIndex columnIndex : columnIndexes) {
			columnNames.addObject(columnIndex.columnName());
		}
		return columnNames;
	}
	
	public boolean reassignExternalTypeForValueTypeOverride(EOAttribute attribute) {
		return true;
	}
	
	public String quoteColumnName(String columnName){
		// just pass through by default
		return columnName;
	}

	/**
	 * Returns whether or not this database can always perform the a distinct operation
	 * when sort orderings are applied. Oracle, for instance, will fail if you try to
	 * sort on a key that isn't in the list of fetched keys.
	 * 
	 * @return whether or not this database can always perform the a distinct operation
	 * when sort orderings are applied
	 */
	protected boolean canReliablyPerformDistinctWithSortOrderings() {
		return true;
	}
	
	/**
	 * Returns whether or not this database should perform the distinct portion of the
	 * given fetch spec in memory or not.
	 * 
	 * @param fetchSpecification the fetch spec to check
	 * @return whether or not this database should perform the distinct portion of the
	 * given fetch spec in memory or not
	 */
	public boolean shouldPerformDistinctInMemory(EOFetchSpecification fetchSpecification) {
		boolean shouldPerformDistinctInMemory = false;
		if (!canReliablyPerformDistinctWithSortOrderings()) {
			NSArray<EOSortOrdering> sortOrderings = fetchSpecification.sortOrderings();
	        if (fetchSpecification.usesDistinct() && sortOrderings != null && sortOrderings.count() > 0) {
	        	shouldPerformDistinctInMemory = true;
	        	// MS: We might be able to restrict this check further at some point ...
//	        	for (EOSortOrdering sortOrdering : sortOrderings) {
//	        		sortOrdering.key();
//	        	}
	        }
		}
        return shouldPerformDistinctInMemory;
	}

	/**
	 * Returns true if the SQL helper can handle the exception. Typical uses are
	 * morphing unique constraints to NSValidation exceptions.
	 * 
	 * @param databaseContext
	 * @param throwable
	 * @return whether or not the SQL helper can handl this exception
	 */
	public boolean handleDatabaseException(EODatabaseContext databaseContext, Throwable throwable) {
		return false;
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
		return ERXSQLHelper.newSQLHelper(EOUtilities.databaseContextForModelNamed(ec, modelName));
	}

	public static ERXSQLHelper newSQLHelper(EOEditingContext ec, EOEntity entity) {
		return ERXSQLHelper.newSQLHelper(EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec));
	}

	public static ERXSQLHelper newSQLHelper(EOEditingContext ec, EOModel model) {
		return ERXSQLHelper.newSQLHelper(EODatabaseContext.registeredDatabaseContextForModel(model, ec));
	}

	public static ERXSQLHelper newSQLHelper(EODatabaseContext databaseContext) {
		EOAdaptor adaptor = databaseContext.database().adaptor();
		return ERXSQLHelper.newSQLHelper(adaptor);
	}

	public static ERXSQLHelper newSQLHelper(EODatabaseChannel databaseChannel) {
		EOAdaptor adaptor = databaseChannel.adaptorChannel().adaptorContext().adaptor();
		return ERXSQLHelper.newSQLHelper(adaptor);
	}
	
	public static ERXSQLHelper newSQLHelper(EOAdaptor adaptor) {
		if (adaptor instanceof JDBCAdaptor) {
			return ERXSQLHelper.newSQLHelper((JDBCAdaptor)adaptor);
		}
			
		// MS: Hack to support non JDBC adaptor migrations
		return new NoSQLHelper();
	}

	public static ERXSQLHelper newSQLHelper(EOAdaptorChannel adaptorChannel) {
		EOAdaptor adaptor = adaptorChannel.adaptorContext().adaptor();
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

	public static ERXSQLHelper newSQLHelper(EOEntity entity) {
		return ERXSQLHelper.newSQLHelper(entity.model());
	}

	public static ERXSQLHelper newSQLHelper(EOModel model) {
		EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);
		return ERXSQLHelper.newSQLHelper(adaptor);
	}

	public static ERXSQLHelper newSQLHelper(String databaseProductName) {
		synchronized (_sqlHelperMap) {
			ERXSQLHelper sqlHelper = _sqlHelperMap.get(databaseProductName);
			if (sqlHelper == null) {
				try {
					String sqlHelperClassName = ERXProperties.stringForKey(databaseProductName + ".SQLHelper");
					if (sqlHelperClassName == null) {
						if (databaseProductName == null) {
							// If there is no plugin then product name will be null
							sqlHelper = new ERXSQLHelper();
						}
						else if (databaseProductName.equalsIgnoreCase("frontbase")) {
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
						else if (databaseProductName.equalsIgnoreCase("microsoft")) {
							sqlHelper = new MicrosoftSQLHelper();
						}
						else if (databaseProductName.equalsIgnoreCase("h2")) {
							log.warn("H2Helper");
							sqlHelper = new H2SQLHelper();
							
						} 
						else if (databaseProductName.equalsIgnoreCase("db2")) {
								sqlHelper = new DB2SQLHelper();
						}
						else if (databaseProductName.equalsIgnoreCase("firebird")) {
							sqlHelper = new FirebirdSQLHelper();
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
		@Override
		protected String sqlForSubquery(String subquery, String alias) {
			return "(" + subquery + ") " + alias;
		}

		@Override
		protected String sqlForGetNextValFromSequencedNamed(String sequenceName) {
			String sqlString = "select " + sequenceName + ".nextVal from dual";
			return sqlString;
		}

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
		 * @see ERXSQLHelper#createSchemaSQLForEntitiesInModelWithNameAndOptions(NSArray, String, NSDictionary)
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
			return buf.toString();
		}

		@Override
		public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
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
				if (expression != null) {
					limitSQL = "select * from (" + "select " + expression.listString().replaceAll("[Tt]\\d\\.", "") + ", rownum eo_rownum from (" + sql + ")) where eo_rownum between " + (start + 1) + " and " + end;
				}
				else {
					limitSQL = "select * from (select a.*, rownum eo_rownum from (" + sql + ") a where rownum <= " + end + ") where eo_rownum >= " + (start + 1);
				}
			}
			else {
				// this might work, too, but only if we have an ORDER BY
				limitSQL = "select * from (" + "select " + (fetchSpecification.usesDistinct() ? " distinct " : "") + expression.listString() + ", row_number() over (" + expression.orderByString() + ") eo_rownum" + " from " + expression.joinClauseString() + " where " + expression.whereClauseString() + ") where eo_rownum between " + (start + 1) + " and " + end;
			}
			return limitSQL;
		}

		@Override
		protected char commandSeparatorChar() {
			return '/';
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
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			return "CREATE UNIQUE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}
		
		@Override
		public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			return "CREATE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}

		@Override
		public String sqlForRegularExpressionQuery(String key, String value) {
			return "REGEXP_LIKE(" + key + ", " + value + ")";
		}

		@Override
		public String migrationTableName() {
			return "dbupdater";
		}
		
		@Override
		public String externalTypeForJDBCType(JDBCAdaptor adaptor, int jdbcType) {
			String externalType;
			if (jdbcType == Types.TIMESTAMP) {
				externalType = "DATE";
			}
			else {
				externalType = super.externalTypeForJDBCType(adaptor, jdbcType);
			}
			return externalType;
		}
		
		@Override
		public boolean reassignExternalTypeForValueTypeOverride(EOAttribute attribute) {
			return false;
		}
		
		@Override
		protected boolean canReliablyPerformDistinctWithSortOrderings() {
			return false;
		}
		
		/**
		 * For Oracle, it seems the right thing to do for varcharLarge is to use a CLOB column.
		 * CLOB is limited to 8TB where as VARCHAR is limited to 4000 bytes.
		 */
		@Override
		public int varcharLargeJDBCType() {
			return Types.CLOB;
		}
		@Override
		public int varcharLargeColumnWidth() {
			return -1;
		}
	}

	public static class OpenBaseSQLHelper extends ERXSQLHelper {
		@Override
		public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
			// Openbase support for limiting result set
			return sql + " return results " + start + " to " + end;
		}
	}
	
	public static class H2SQLHelper extends ERXSQLHelper {
		
		@Override
		public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
			return sql + " LIMIT " + (end - start) + " OFFSET " + start;
		}
		
		@Override
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = columnNamesFromColumnIndexes(columnIndexes);
			return "ALTER TABLE " + tableName + " ADD CONSTRAINT \"" + indexName + "\" UNIQUE(" + new NSArray<String>(columnNames).componentsJoinedByString(", ") + ")";
		}
		
		@Override
		public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = columnNamesFromColumnIndexes(columnIndexes);
			return "CREATE INDEX \""+indexName+"\" ON "+tableName+" ("+new NSArray<String>(columnNames).componentsJoinedByString(", ")+")";
		}

		/**
		 * @see er.extensions.jdbc.ERXSQLHelper#sqlForGetNextValFromSequencedNamed(java.lang.String)
		 */
		@Override
		protected String sqlForGetNextValFromSequencedNamed(String sequenceName) {
			return "select NEXTVAL('" + sequenceName + "') as key"; 
		}

		@Override
		public String sqlForRegularExpressionQuery(String key, String value) {
			return key + " REGEXP " + value + "";
		}
		
		@Override
		public int varcharLargeJDBCType() {
			return Types.LONGVARCHAR;
		}
		
		@Override
		public int varcharLargeColumnWidth() {
			return -1;
		}
	}

	public static class DerbySQLHelper extends ERXSQLHelper {
		@Override
		public boolean shouldExecute(String sql) {
			return sql != null && !sql.startsWith("--");
		}

		@Override
		public int varcharLargeJDBCType() {
			return Types.CLOB;
		}

		@Override
		public int varcharLargeColumnWidth() {
			return 0;
		}

		@Override
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			return "CREATE UNIQUE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}
		
		/**
		 * @see er.extensions.jdbc.ERXSQLHelper#sqlForGetNextValFromSequencedNamed(java.lang.String)
		 */
		@Override
		protected String sqlForGetNextValFromSequencedNamed(String sequenceName) {
			return "VALUES (NEXT VALUE FOR " + sequenceName + ")"; 
		}

		@Override
		public String migrationTableName() {
			return "dbupdater";
		}
		
		@Override
		public String externalTypeForJDBCType(JDBCAdaptor adaptor, int jdbcType) {
			String externalType;
			if (jdbcType == Types.TIMESTAMP) {
				externalType = "DATE";
			}
			else {
				externalType = super.externalTypeForJDBCType(adaptor, jdbcType);
			}
			return externalType;
		}
	}

	public static class FrontBaseSQLHelper extends ERXSQLHelper {
		private static final String PREFIX_ISOLATION_LEVEL = "isolation=";
		private static final String PREFIX_LOCKING = "locking=";

		@Override
		public boolean reassignExternalTypeForValueTypeOverride(EOAttribute attribute) {
			boolean reassignExternalTypeForValueTypeOverride = super.reassignExternalTypeForValueTypeOverride(attribute);
			if ("DATE".equalsIgnoreCase(attribute.externalType()) && attribute.valueType() == null) {
				reassignExternalTypeForValueTypeOverride = false;
			}
			return reassignExternalTypeForValueTypeOverride;
		}
		
		@Override
		protected String sqlForGetNextValFromSequencedNamed(String sequenceName) {
			return "select unique from " + sequenceName;
		}
		
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
		public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
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
			return "ALTER TABLE \"" + tableName + "\" ADD CONSTRAINT \"" + indexName + "\" UNIQUE(\"" + new NSArray<String>(columnNames).componentsJoinedByString("\", \"") + "\") DEFERRABLE INITIALLY DEFERRED";
		}

		@Override
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
		

		/**
		 * Returns a pattern than matches lines that start with "--".
		 * 
		 * @return regex pattern that indicates this line is an SQL comment
		 */
		@Override
		protected Pattern commentPattern() {
			return Pattern.compile("^--");
		}
		
		@Override
		public String quoteColumnName(String columnName){
			if (columnName == null)
				return null;

			int i = columnName.lastIndexOf(46);
			
			if (i == -1)
				return "\"" + columnName + "\"";

			return "\"" + columnName.substring(0, i) + "\".\"" + columnName.substring(i + 1, columnName.length()) + "\"";
		}
		
		/**
		 * FrontBase is exceedingly inefficient in processing OR clauses.   A query like this:<br/>
		 * SELECT * FROM "Foo" t0 WHERE ( t0."oid" IN (431, 437, ...) OR t0."oid" IN (1479, 1480, 1481,...)...<br/>
		 * Completely KILLS FrontBase (30+ seconds of 100%+ CPU usage). The same query rendered as:<br/>
		 * SELECT * FROM "Foo" t0 WHERE t0."oid" IN (431, 437, ...) UNION SELECT * FROM "Foo" t0 WHERE t0."oid" IN (1479, 1480, 1481, ...)...
		 * executes in less than a tenth of the time with less high CPU load.  Collapse all the ORs and INs into one and it is faster
		 * still.  This has been tested with over 17,000 elements, so 15,000 seemed like a safe maximum.  I don't know what the actual
		 * theoretical maximum is.
		 * 
		 * But... It looks to like the query optimizer will choose to NOT use an index if the number of elements in the IN gets close to, 
		 * or exceeds, the number of rows (as in the case of a select based on FK with a large number of keys that don't match any rows).  In 
		 * this case it seems to fall back to table scanning (or something dreadfully slow).  This only seems to have an impact when the number
		 * of elements in the IN is greater than 1,000.  For larger sizes, the correct number for this method to return seems to depend on the
		 * number of rows in the tables.  1/5th of the table size may be a good place to start looking for the upper bound.
		 * 
		 * @see ERXSQLHelper#maximumElementPerInClause(EOEntity)
		 * 
		 * @param entity EOEntity that can be used to fine-tune the result
		 * @return database specific limit, or or most efficient number, of elements in an IN clause in a statement
		 */
		@Override
		protected int maximumElementPerInClause(EOEntity entity) {
			return 15000;
		}
		
		/**
		 * For BOOLEAN we take 'boolean' as external type. For any other
		 * case, we pass it up to the default impl.
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
			if (jdbcType == Types.BOOLEAN) {
				externalType = "boolean";
			}
			else {
				externalType = super.externalTypeForJDBCType(adaptor, jdbcType);
			}
			return externalType;
		}
	}

	public static class MySQLSQLHelper extends ERXSQLHelper {
		
		/**
		 * Returns a pattern than matches lines that start with "--".
		 * 
		 * @return regex pattern that indicates this line is an SQL comment
		 */
		@Override
		protected Pattern commentPattern() {
			return Pattern.compile("^--");
		}

		/** 
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
			if (jdbcType == Types.LONGVARCHAR || jdbcType == Types.CLOB) {
				externalType = "longtext";
			}
			else {
				externalType = super.externalTypeForJDBCType(adaptor, jdbcType);
			}
			return externalType;
		}
		
		@Override
		public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
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
		
		@Override
		public int varcharLargeJDBCType() {
			return Types.LONGVARCHAR;
		}
		
		@Override
		public int varcharLargeColumnWidth() {
			return -1;
		}
	}

	public static class PostgresqlSQLHelper extends ERXSQLHelper {
		/**
		 * The exception state string for unique constraint exceptions.
		 * 
		 * @see <a href="http://www.postgresql.org/docs/9.1/static/errcodes-appendix.html">Error codes</a>
		 */
		public static final String UNIQUE_CONSTRAINT_EXCEPTION_STATE = "23505";

		public static final String UNIQUE_CONSTRAINT_MESSAGE_FORMAT = "ERROR: duplicate key value violates unique constraint \"{0}\"\n  Detail: Key ({1})=({2}) already exists.";
		/**
		 * Overriden to prevent the external time types set in 
		 * {@link #externalTypeForJDBCType(JDBCAdaptor, int)} from being reset.
		 */
		@Override
		public boolean reassignExternalTypeForValueTypeOverride(EOAttribute attr) {
			if(attr != null && attr.adaptorValueType() == EOAttribute.AdaptorDateType) {
				return false;
			}
			return super.reassignExternalTypeForValueTypeOverride(attr);
		}
		
		@Override
		protected String sqlForGetNextValFromSequencedNamed(String sequenceName) {
			return "select NEXTVAL('" + sequenceName + "') as key"; 
		}
		
		@Override
		public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
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
		 * bytea for Type.BLOB; int8 for BIGINT; numeric for DECIMAL; bool for
		 * BOOLEAN
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
			} 
			else if (jdbcType == Types.BIGINT) {
				externalType = "int8";
			} 
			else if (jdbcType == Types.FLOAT) {
				externalType = "float4";
			}
			else if (jdbcType == Types.DOUBLE) {
				externalType = "float8";
			}
			else if (jdbcType == Types.BLOB) {
				externalType = "bytea";
			}
			else if (jdbcType == Types.BOOLEAN) {
				externalType = "bool";
			}
			else if (jdbcType == Types.DECIMAL) {
				externalType = "numeric";
			}
			else if (jdbcType == CustomTypes.INET) {
				externalType = "inet";
			}
			else if (jdbcType == Types.DATE) {
				externalType = "date";
			}
			else if (jdbcType == Types.TIME) {
				externalType = "time";
			}
			else if (jdbcType == Types.LONGVARCHAR || jdbcType == Types.CLOB) {
				externalType = "text";
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
		 * @see ERXSQLHelper#sqlForCreateUniqueIndex(String, String, ColumnIndex...)
		 */
		@Override
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			indexName = indexName.replace('.', '_');
			return "CREATE UNIQUE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}

		@Override
		public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			return "CREATE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}

		@Override
		public int varcharLargeJDBCType() {
			return Types.LONGVARCHAR;
		}
		
		@Override
		public int varcharLargeColumnWidth() {
			return -1;
		}
		
		@Override
		public boolean handleDatabaseException(EODatabaseContext databaseContext, Throwable throwable) {
			if (throwable instanceof EOGeneralAdaptorException) {
				EOGeneralAdaptorException gae = (EOGeneralAdaptorException) throwable;
				if (gae.userInfo() != null) {
					EOAdaptorOperation failedOperation = (EOAdaptorOperation) gae.userInfo().objectForKey(EOAdaptorChannel.FailedAdaptorOperationKey);
					if (failedOperation != null) {
						Throwable t = failedOperation.exception();
						if (t instanceof JDBCAdaptorException) {
							JDBCAdaptorException jdbcEx = (JDBCAdaptorException) t;
							SQLException sqlEx = jdbcEx.sqlException();
							if (sqlEx != null && UNIQUE_CONSTRAINT_EXCEPTION_STATE.equals(sqlEx.getSQLState())) {
								String message = sqlEx.getMessage();
								MessageFormat format = new MessageFormat(UNIQUE_CONSTRAINT_MESSAGE_FORMAT);
								try {
									Object[] objs = format.parse(message);
									String idx = (String) objs[0];
									ERXValidationFactory factory = ERXValidationFactory.defaultFactory();
									String method = "UniqueConstraintException." + idx;
									ERXValidationException ex = factory.createCustomException(null, method);
									databaseContext.rollbackChanges();
									throw ex;
								} catch (ParseException e) {
									log.warn("Error parsing unique constraint exception message: " + message);
								}
							}
						}
					}
				}
			}
			return false;
		}

		@Override
		protected String sqlForCountDistinct(EOEntity entity) {
			NSArray<String> primaryKeyAttributeNames = entity.primaryKeyAttributeNames();
			NSMutableArray<String> pkColumnNames = new NSMutableArray<String>(primaryKeyAttributeNames.size());

			for (String pkAttributeName : primaryKeyAttributeNames) {
				pkColumnNames.add(quoteColumnName("t0." + entity.attributeNamed(pkAttributeName).columnName()));
			}

			return "count(distinct (" + StringUtils.join(pkColumnNames, ", ") + ")) ";
		}
	}
	
	public static class FirebirdSQLHelper extends ERXSQLHelper {
		
		@Override
		public String externalTypeForJDBCType(JDBCAdaptor adaptor, int jdbcType) {
			String externalType;
			if (jdbcType == Types.BOOLEAN) {
				externalType = "SMALLINT";
			} else {
				externalType = super.externalTypeForJDBCType(adaptor, jdbcType);
			}
			return externalType;
		}
		
		@Override
		protected String sqlForGetNextValFromSequencedNamed(String sequenceName) {
			return "select Gen_ID(" + sequenceName + ", 1) FROM RDB$Database"; 
		}
		
		@Override
		protected int maximumElementPerInClause(EOEntity entity) {
			return 1500;
		}
		
		@Override
		public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
			return sql + " ROWS " + start + " TO " + end;
		}
		
		private static final NSSet reservedWords = new NSSet(new String[] {
			"active",
			"password"
		});
		
		@Override
		public String quoteColumnName(String columnName){
			if (columnName == null)
				return null;
			if (columnName.startsWith("\""))
				return columnName;
			if (!reservedWords.contains(columnName))
				return columnName;
			
			int i = columnName.lastIndexOf(".");
			
			if (i == -1)
				return "\"" + columnName + "\"";

			return "\"" + columnName.substring(0, i) + "\".\"" + columnName.substring(i + 1, columnName.length()) + "\"";
		}
		
		/** 
		 * Creates unique index; stolen from the derby helper
		 * @see ERXSQLHelper#sqlForCreateUniqueIndex(String, String, ColumnIndex...)
		 */
		@Override
		public String sqlForCreateUniqueIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			indexName = indexName.replace('.', '_');
			return "CREATE UNIQUE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}

		@Override
		public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			return "CREATE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}
		
	}
	
	public static class MicrosoftSQLHelper extends ERXSQLHelper {

		/**
		 * Returns a pattern than matches lines that start with "--".
		 * 
		 * @return regex pattern that indicates this line is an SQL comment
		 */
		@Override
		protected Pattern commentPattern() {
			return Pattern.compile("^--");
		}
		
		@Override
		public String externalTypeForJDBCType( JDBCAdaptor adaptor, int type ) {
			if( type == Types.BLOB ) {
				return "binary";
			}

			return super.externalTypeForJDBCType( adaptor, type );
		}
		
		@Override
		public String limitExpressionForSQL( EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end ) {
			if( sql == null || "".equals( sql ) )
			{
				return sql;
			}

			String originalSql = sql.toLowerCase();

			String orderBy;

			int indexOfOrderByClause = originalSql.indexOf( " order by " );

			if( indexOfOrderByClause > 0)
			{
				orderBy = originalSql.substring( indexOfOrderByClause + 1, originalSql.length() );

				originalSql = originalSql.substring( 0, indexOfOrderByClause );
			}
			else
			{
				String columns = originalSql.substring( originalSql.indexOf(  "select " ) + 7, originalSql.indexOf( " from " ) );

				orderBy = "order by " + columns.split( "," )[0];
			}

			StringBuilder limitSqlBuilder = new StringBuilder( originalSql );

			limitSqlBuilder.insert( 0, "select * from (" );

			String rowNumberClause = ", row_number() over (" + orderBy + ") eo_rownum";

			limitSqlBuilder.insert( limitSqlBuilder.lastIndexOf( " from " ), rowNumberClause );
			limitSqlBuilder.append( ") as temp_row_number where eo_rownum >= " );
			limitSqlBuilder.append( start + 1 );
			limitSqlBuilder.append( " and eo_rownum < " );
			limitSqlBuilder.append( end + 1 );
			limitSqlBuilder.append( " order by eo_rownum" );

			return limitSqlBuilder.toString();
		}
	}

	public static class NoSQLHelper extends ERXSQLHelper {
		@Override
		public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			return null;
		}
	}
	
	public static class DB2SQLHelper extends ERXSQLHelper {
		@Override
		protected String sqlForSubquery(String subquery, String alias) {
			return "(" + subquery + ") " + alias;
		}

		@Override
		protected String sqlForGetNextValFromSequencedNamed(String sequenceName) {
			String sqlString = "select next value for " + sequenceName + " from SYSIBM.SYSDUMMY1";
			return sqlString;
		}


		@Override
		public String limitExpressionForSQL(EOSQLExpression expression, EOFetchSpecification fetchSpecification, String sql, long start, long end) {
				// this might work, too, but only if we have an ORDER BY
			
			
			// remove order by clause 
			String orderBy = expression.orderByString();
			String innerSql = sql.replace(orderBy, " ");
			innerSql = innerSql.replace(" ORDER BY ", " ");
			String rownum = ", row_number() over ( order by " + orderBy + ") eo_rownum" + " FROM ";
			innerSql = innerSql.replace(" FROM ", rownum);
			innerSql = innerSql.replaceAll("FETCH FIRST\\W+[0-9]+\\W+ROWS ONLY", " ");  // this removes any limit that may be on  may want to keep it it does work
			String limitSQL = "select * from (" +  innerSql +  ") as inner_select where eo_rownum between " + (start + 1) + " and " + end;
			
			return limitSQL;
		}

		@Override
		protected char commandSeparatorChar() {
			return ';';
		}

		@Override
		protected String commandSeparatorString() {
			String lineSeparator = System.getProperty("line.separator");
			String commandSeparator = lineSeparator + ";" + lineSeparator;
			return commandSeparator;
		}

		@Override
		public String createIndexSQLForEntities(NSArray<EOEntity> entities, NSArray<String> externalTypesToIgnore) {
			NSMutableArray<String> db2ExternalTypesToIgnore = new NSMutableArray<String>();
			if (externalTypesToIgnore != null) {
				db2ExternalTypesToIgnore.addObjectsFromArray(externalTypesToIgnore);
			}
			db2ExternalTypesToIgnore.addObject("BLOB");
			db2ExternalTypesToIgnore.addObject("CLOB");
			db2ExternalTypesToIgnore.addObject("DBCLOB");
			db2ExternalTypesToIgnore.addObject("LONG VARCHAR");
			db2ExternalTypesToIgnore.addObject("LONG VARGRAPHIC");
			return super.createIndexSQLForEntities(entities, db2ExternalTypesToIgnore);
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
		public String sqlForCreateIndex(String indexName, String tableName, ColumnIndex... columnIndexes) {
			NSMutableArray<String> columnNames = new NSMutableArray<String>();
			for (ColumnIndex columnIndex : columnIndexes) {
				columnNames.addObject(columnIndex.columnName());
			}
			return "CREATE INDEX " + indexName + " ON " + tableName + "(" + columnNames.componentsJoinedByString(",") + ")";
		}

		@Override
		public String migrationTableName() {
			return "dbupdater";
		}
		
		@Override
		public boolean reassignExternalTypeForValueTypeOverride(EOAttribute attribute) {
			return false;
		}
		
		@Override
		protected boolean canReliablyPerformDistinctWithSortOrderings() {
			return false;
		}
		
		/**
		 * For DB2, it seems the right thing to do for varcharLarge is to use a Clob column.
		 * CLOB is limited to 2GB where as VARCHAR is limited to 32672 bytes and a LONG VARCHAR to 32700
		 */
		@Override
		public int varcharLargeJDBCType() {
			return Types.CLOB;
		}

	}

}
