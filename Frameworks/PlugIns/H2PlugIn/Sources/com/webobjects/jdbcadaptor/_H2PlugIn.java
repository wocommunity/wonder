package com.webobjects.jdbcadaptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.synchronization.EOSchemaGenerationOptions;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronization;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationFactory;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSStringUtilities;

/**
 * WO runtime plugin with support for H2.
 * 
 * @see <a href="http://www.h2database.com/">http://www.h2database.com</a>
 */
public class _H2PlugIn extends JDBCPlugIn {
	static final boolean USE_NAMED_CONSTRAINTS = true;
	protected static NSMutableDictionary<String, String> sequenceNameOverrides = new NSMutableDictionary<String, String>();

	/**
	 * Formatter to use when handling date columns. Each thread has its own
	 * copy.
	 */
	private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	/**
	 * Formatter to use when handling timestamp columns. Each thread has its own
	 * copy.
	 */
	private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		}
	};

	protected static String quoteTableName(String name) {
		String result = null;
		if (name != null) {
			int i = name.lastIndexOf(46);
			if (i < 0) {
				result = new StringBuilder('"').append(name).append('"').toString();
			} else {
				result =
					new StringBuilder(name.substring(0, i))
				.append("\".\"")
				.append(name.substring(i + 1, name.length()))
				.append('"')
				.toString();
			}
		}
		return result;
	}

	static String singleQuotedString(Object value) {
		return value == null ? null : singleQuotedString(value.toString());
	}

	static String singleQuotedString(String string) {
		if (string == null) {
			return null;
		}
		return new StringBuilder('\'').append(string).append('\'').toString();
	}
	
	/**
	* Utility method that returns the name of the sequence associated
	* with the given <code>entity</code>.
	*
	* @param entity the entity
	* @return the name of the sequence
	*/
	protected static String _sequenceNameForEntity(EOEntity entity) {
		String sequenceName = entity.primaryKeyRootName() + "_seq";
		synchronized (sequenceNameOverrides) {
			if (sequenceNameOverrides.containsKey(sequenceName)) {
				sequenceName = sequenceNameOverrides.get(sequenceName);
			}
		}
		return sequenceName;
	}
	
	/**
	 * Sets the sequence name to be used in H2 instead of the default WO sequence name.
	 * This is needed if the sequence has been created outside of WO and its name
	 * is differing from the default WO schema as H2 does not yet support renaming
	 * of sequences.
	 * 
	 * @param defaultName WO default sequence name
	 * @param h2Name sequence name in H2
	 */
	protected static void setSequenceNameOverride(String defaultName, String h2Name) {
		synchronized (sequenceNameOverrides) {
			sequenceNameOverrides.put(defaultName, h2Name);
		}
	}

	@Override
	public Object fetchBLOB(ResultSet rs, int column, EOAttribute attribute, boolean materialize) throws SQLException {
		NSData data = null;
		Blob blob = rs.getBlob(column);
		if(blob == null) { return null; }
		if(!materialize) { return blob; }
		InputStream stream = blob.getBinaryStream();
		try {
			int chunkSize = (int)blob.length();
			if(chunkSize == 0) {
				data = NSData.EmptyData;
			} else {
				data = new NSData(stream, chunkSize);
			}
		} catch(IOException e) {
			throw new JDBCAdaptorException(e.getMessage(), null);
		} finally {
			try {if(stream != null) stream.close(); } catch(IOException e) { /* Nothing we can do */ };
		}
		return data;
	}
	
	@Override
	public Object fetchCLOB(ResultSet rs, int column, EOAttribute attribute, boolean materialize) throws SQLException {
		Clob clob = rs.getClob(column);
		if (clob == null) {
			return null;
		}
		if (!materialize) {
			return clob;
		} else {
			return clob.getSubString(1L, (int) clob.length());
		}
	}

	public static class H2Expression extends JDBCExpression {
		/**
		 * Fetch spec limit ivar
		 */
		private int _fetchLimit;
		
		/**
		 * Fetch spec range ivar
		 */
		private NSRange _fetchRange;
		private final NSSelector<NSRange> _fetchRangeSelector = new NSSelector<NSRange>("fetchRange");

		public H2Expression(final EOEntity entity) {
			super(entity);
		}

		@Override
		public void addCreateClauseForAttribute(final EOAttribute attribute) {
			StringBuilder sql = new StringBuilder();
			sql.append(attribute.columnName());
			sql.append(' ');
			sql.append(columnTypeStringForAttribute(attribute));

			NSDictionary<String, Object> userInfo = attribute.userInfo();
			if (userInfo != null) {
				Object defaultValue = userInfo.valueForKey("er.extensions.eoattribute.default"); // deprecated key
		        if (defaultValue == null) {
		            defaultValue = userInfo.valueForKey("default");
		        }
				if (defaultValue != null) {
					sql.append(" DEFAULT ");
					sql.append(formatValueForAttribute(defaultValue, attribute));
				}
			}

			sql.append(' ');
			sql.append(allowsNullClauseForConstraint(attribute.allowsNull()));

			appendItemToListString(sql.toString(), _listString());
		}

		protected boolean enableBooleanQuoting() {
			return false;
		}

		/**
		 * @param value
		 * @param eoattribute
		 * @return the plain string representation of the given value
		 */
		private String formatBigDecimal(final BigDecimal value, final EOAttribute eoattribute) {
			return value.toPlainString();
		}

		@Override
		public String formatValueForAttribute(final Object value, final EOAttribute eoattribute) {
			String result;
			if (value instanceof NSData) {
				result = sqlStringForData((NSData) value);
			}
			else if (value instanceof NSTimestamp && isTimestampAttribute(eoattribute)) {
				result = singleQuotedString(TIMESTAMP_FORMATTER.get().format(value));
			}
			else if (value instanceof NSTimestamp && isDateAttribute(eoattribute)) {
				result = singleQuotedString(DATE_FORMATTER.get().format(value));
			}
			else if (value instanceof String) {
				result = formatStringValue((String) value);
			}
			else if (value instanceof Number) {
				if (value instanceof BigDecimal) {
					result = formatBigDecimal((BigDecimal) value, eoattribute);
				}
				else {
					Object convertedValue = eoattribute.adaptorValueByConvertingAttributeValue(value);
					if (convertedValue instanceof Number) {
						Number convertedNumberValue = (Number) convertedValue;
						String valueType = eoattribute.valueType();
						if (valueType == null || "i".equals(valueType)) {
							result = String.valueOf(convertedNumberValue.intValue());
						}
						else if ("l".equals(valueType)) {
							result = String.valueOf(convertedNumberValue.longValue());
						}
						else if ("f".equals(valueType)) {
							result = String.valueOf(convertedNumberValue.floatValue());
						}
						else if ("d".equals(valueType)) {
							result = String.valueOf(convertedNumberValue.doubleValue());
						}
						else if ("s".equals(valueType)) {
							result = String.valueOf(convertedNumberValue.shortValue());
						}
						else {
							result = convertedNumberValue.toString();
						}
					}
					else {
						result = convertedValue.toString();
					}
				}
			}
			else if (value instanceof Boolean) {
				// GN: when booleans are stored as strings in the db, we need
				// the values quoted
				if (enableBooleanQuoting()) {
					result = singleQuotedString(value);
				}
				else {
					result = value.toString();
				}
			}
			else if (value instanceof Timestamp) {
				result = singleQuotedString(value);
			}
			else if (value == null || value == NSKeyValueCoding.NullValue) {
				result = "NULL";
			}
			else {
				// AK: I don't really like this, but we might want to prevent
				// infinite recursion
				try {
					Object adaptorValue = eoattribute.adaptorValueByConvertingAttributeValue(value);
					if (adaptorValue instanceof NSData
							|| adaptorValue instanceof NSTimestamp
							|| adaptorValue instanceof String
							|| adaptorValue instanceof Number
							|| adaptorValue instanceof Boolean)
					{
						result = formatValueForAttribute(adaptorValue, eoattribute);
					}
					else {
						StringBuilder buff = new StringBuilder(getClass().getName())
						.append(": Can't convert: ")
						.append(value)
						.append(':')
						.append(value.getClass().getName())
						.append(" -> ")
						.append(adaptorValue)
						.append(':')
						.append(adaptorValue.getClass().getName());

						NSLog.err.appendln(buff.toString());

						result = value.toString();
					}
				}
				catch (Exception ex) {
					StringBuilder buff = new StringBuilder(getClass().getName())
					.append(": Exception while converting ")
					.append(value.getClass().getName());

					NSLog.err.appendln(buff.toString());
					NSLog.err.appendln(ex);

					result = value.toString();
				}
			}
			return result;
		}

		/**
		 * Helper to check for timestamp columns that have a "D" value type.
		 *
		 * @param eoattribute
		 */
		private boolean isDateAttribute(final EOAttribute eoattribute) {
			return eoattribute != null && "D".equals(eoattribute.valueType());
		}

		/**
		 * Helper to check for timestamp columns that have a "T" value type.
		 *
		 * @param eoattribute
		 */
		private boolean isTimestampAttribute(final EOAttribute eoattribute) {
			return eoattribute != null && "T".equals(eoattribute.valueType());
		}

		/**
		 * Overridden so we can get the fetch limit from the fetchSpec.
		 *
		 * @param attributes the array of attributes
		 * @param lock locking flag
		 * @param fetchSpec the fetch specification
		 */
		@Override
		public void prepareSelectExpressionWithAttributes(NSArray<EOAttribute> attributes, boolean lock, EOFetchSpecification fetchSpec) {
			try {
				_fetchRange = _fetchRangeSelector.invoke(fetchSpec);
				// We will get an error when not using our custom ERXFetchSpecification subclass
				// We could have added ERExtensions to the classpath and checked for instanceof, but I thought
				// this is a little cleaner since people may be using this PlugIn and not Wonder in some legacy apps.
			} catch (IllegalArgumentException e) {
				// ignore
			} catch (IllegalAccessException e) {
				// ignore
			} catch (InvocationTargetException e) {
				// ignore
			} catch (NoSuchMethodException e) {
				// ignore
			}
			// Only check for fetchLimit if fetchRange is not provided.
			if (_fetchRange == null && !fetchSpec.promptsAfterFetchLimit()) {
				_fetchLimit = fetchSpec.fetchLimit();
			}
			if (_fetchRange != null) {
				// if we have a fetch range disable the limit
				fetchSpec.setFetchLimit(0);
			}
			super.prepareSelectExpressionWithAttributes(attributes, lock, fetchSpec);
		}

		@Override
		public String assembleSelectStatementWithAttributes(NSArray attributes, boolean lock, EOQualifier qualifier, NSArray fetchOrder, String selectString, String columnList, String tableList, String whereClause, String joinClause, String orderByClause, String lockClause) {
			int size = selectString.length() + columnList.length() + tableList.length() + 7;
			if (lockClause != null && lockClause.length() != 0) {
				size += lockClause.length() + 1;
			}
			if (whereClause != null && whereClause.length() != 0) {
				size += whereClause.length() + 7;
			}
			if (joinClause != null && joinClause.length() != 0) {
				size += joinClause.length() + 7;
			}
			if (orderByClause != null && orderByClause.length() != 0) {
				size += orderByClause.length() + 10;
			}
			StringBuilder sb = new StringBuilder(size);
			sb.append(selectString);
			sb.append(columnList);
			sb.append(" FROM ");
			sb.append(tableList);
			
			if (whereClause != null && whereClause.length() != 0) {
				sb.append(" WHERE ");
				sb.append(whereClause);
			}
			if (joinClause != null && joinClause.length() != 0) {
				if (whereClause != null && whereClause.length() != 0) {
					sb.append(" AND ");
				} else {
					sb.append(" WHERE ");
				}
				sb.append(joinClause);
			}
			
			if (orderByClause != null && orderByClause.length() != 0) {
				sb.append(" ORDER BY ");
				sb.append(orderByClause);
			}
			
			// fetchRange overrides fetchLimit
			if (_fetchRange != null) {
				sb.append(" LIMIT ");
				sb.append(_fetchRange.length());
				sb.append(" OFFSET ");
				sb.append(_fetchRange.location());
			} else if (_fetchLimit != 0) {
				sb.append(" LIMIT ");
				sb.append(_fetchLimit);
			}
			
			if (lockClause != null && lockClause.length() != 0) {
				sb.append(' ');
				sb.append(lockClause);
			}
			
			return sb.toString();
		}
	}

	public static class H2SynchronizationFactory extends EOSchemaSynchronizationFactory {

		public H2SynchronizationFactory(final EOAdaptor adaptor) {
			super(adaptor);
		}

		@Override
		public NSArray<EOSQLExpression> _statementsToDropPrimaryKeyConstraintsOnTableNamed(String tableName) {
			return new NSArray<EOSQLExpression>(_expressionForString("ALTER TABLE " + formatTableName(tableName) + " DROP PRIMARY KEY"));
		}

		public String columnTypeStringForAttribute(EOAttribute attribute) {
			if (attribute.precision() != 0) {
				String precision = String.valueOf(attribute.precision());
				String scale = String.valueOf(attribute.scale());
				return _NSStringUtilities.concat(attribute.externalType(), "(", precision, ",", scale, ")");
			}
			if (attribute.width() != 0) {
				String width = String.valueOf(attribute.width());
				return _NSStringUtilities.concat(attribute.externalType(), "(", width, ")");
			}
			return attribute.externalType();
		}

		@Override
		public NSArray<EOSQLExpression> dropPrimaryKeySupportStatementsForEntityGroup(NSArray<EOEntity> entityGroup) {
			NSMutableSet<String> sequenceNames = new NSMutableSet<String>(entityGroup.count());
	        NSMutableArray<EOSQLExpression> results = new NSMutableArray<EOSQLExpression>();
	        for (EOEntity entity : entityGroup) {
	            String sequenceName = H2PlugIn._sequenceNameForEntity(entity);
	            if (!sequenceNames.containsObject(sequenceName)) {
	                sequenceNames.addObject(sequenceName);
	                String sql = "DROP SEQUENCE " + sequenceName;
	                results.addObject(createExpression(entity, sql));
	            }
	        }
	        return results;
		}

		@Override
		public NSArray<EOSQLExpression> dropTableStatementsForEntityGroup(NSArray<EOEntity> entityGroup) {
			String tableName = entityGroup.objectAtIndex(0).externalName();
			return new NSArray<EOSQLExpression>(_expressionForString("DROP TABLE " + formatTableName(tableName)));
		}

		public String formatUpperString(String string) {
			return string.toUpperCase();
		}

		boolean isPrimaryKeyAttributes(EOEntity entity, NSArray<EOAttribute> attributes) {
			NSArray<String> keys = entity.primaryKeyAttributeNames();
			boolean result = attributes.count() == keys.count();

			if (result) {
				for (int i = 0; i < keys.count(); i++) {
					if (!(result = keys.indexOfObject(attributes.objectAtIndex(i).name()) != NSArray.NotFound))
						break;
				}
			}
			return result;
		}

		@Override
		public NSArray<EOSQLExpression> foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
			if (relationship != null
					&& !relationship.isToMany()
					&& isPrimaryKeyAttributes(relationship.destinationEntity(), relationship.destinationAttributes()))
			{
				StringBuilder sql = new StringBuilder();
				String tableName = formatTableName(relationship.entity().externalName());

				sql.append("ALTER TABLE ");
				sql.append(tableName);
				sql.append(" ADD");

				StringBuilder constraint = new StringBuilder(" CONSTRAINT \"FOREIGN_KEY_");
				constraint.append(formatUpperString(tableName));

				StringBuilder fkSql = new StringBuilder(" FOREIGN KEY (");
				NSArray<EOAttribute> attributes = relationship.sourceAttributes();

				for (int i = 0; i < attributes.count(); i++) {
					constraint.append('_');
					if (i != 0)
						fkSql.append(", ");

					String columnName = formatColumnName(attributes.objectAtIndex(i).columnName());
					fkSql.append(columnName);
					constraint.append(formatUpperString(columnName));
				}

				fkSql.append(") REFERENCES ");
				constraint.append('_');

				String referencedExternalName = formatTableName(relationship.destinationEntity().externalName());
				fkSql.append(referencedExternalName);
				constraint.append(formatUpperString(referencedExternalName));

				fkSql.append(" (");

				attributes = relationship.destinationAttributes();

				for (int i = 0; i < attributes.count(); i++) {
					constraint.append('_');
					if (i != 0)
						fkSql.append(", ");

					String referencedColumnName = formatColumnName(attributes.objectAtIndex(i).columnName());
					fkSql.append(referencedColumnName);
					constraint.append(formatUpperString(referencedColumnName));
				}

				// MS: did i write this code?  sorry about that everything. this is crazy. 
				constraint.append('"');

				fkSql.append(')');
				// BOO
				//fkSql.append(") DEFERRABLE INITIALLY DEFERRED");

				if (USE_NAMED_CONSTRAINTS)
					sql.append(constraint);
				sql.append(fkSql);

				return new NSArray<EOSQLExpression>(_expressionForString(sql.toString()));
			}
			return NSArray.EmptyArray;
		}

		@Override
		public NSArray<EOSQLExpression> primaryKeySupportStatementsForEntityGroup(NSArray<EOEntity> entityGroup) {
	        NSMutableSet<String> sequenceNames = new NSMutableSet<String>();
	        NSMutableArray<EOSQLExpression> results = new NSMutableArray<EOSQLExpression>();
	        for (EOEntity entity : entityGroup) {
	        	if (isPrimaryKeyGenerationNotSupported(entity)) {
	        		continue;
	        	}
            	EOAttribute priKeyAttribute = entity.primaryKeyAttributes().objectAtIndex(0);
                
                String sql;
                String sequenceName = H2PlugIn._sequenceNameForEntity(entity);
                if (!sequenceNames.containsObject(sequenceName)) {
                    sequenceNames.addObject(sequenceName);
                    // timc 2006-11-06 create result here so we can check for
                    // enableIdentifierQuoting while building the statement
                    H2Expression result = new H2Expression(entity);
                    String attributeName = result.sqlStringForAttribute(priKeyAttribute);
                    String tableName = result.sqlStringForSchemaObjectName(entity.externalName());

                    sql = "CREATE SEQUENCE " + sequenceName + " START WITH (SELECT MAX(" + attributeName + ") FROM " + tableName + ")";
                    results.addObject(createExpression(entity, sql));

                    sql = "ALTER TABLE " + tableName + " ALTER COLUMN " + attributeName + " SET DEFAULT nextval('" + sequenceName + "')";
                    results.addObject(createExpression(entity, sql));
                }
	        }
	        return results;
		}

		@Override
		public NSArray<EOSQLExpression> statementsToConvertColumnType(String columnName, String tableName, EOSchemaSynchronization.ColumnTypes oldType, EOSchemaSynchronization.ColumnTypes newType, EOSchemaGenerationOptions options) {
			EOAttribute attr = new EOAttribute();
			attr.setName(columnName);
			attr.setColumnName(columnName);
			attr.setExternalType(newType.name());
			attr.setScale(newType.scale());
			attr.setPrecision(newType.precision());
			attr.setWidth(newType.width());

			String columnTypeString = columnTypeStringForAttribute(attr);
			return new NSArray<EOSQLExpression>(_expressionForString("ALTER TABLE " + formatTableName(tableName) + " ALTER COLUMN " + formatColumnName(columnName) + " " + columnTypeString));
		}

		@Override
		public NSArray<EOSQLExpression> statementsToDeleteColumnNamed(String columnName, String tableName, EOSchemaGenerationOptions options) {
			return new NSArray<EOSQLExpression>(_expressionForString("ALTER TABLE " + formatTableName(tableName) + " DROP COLUMN " + formatTableName(columnName)));
		}

		@Override
		public NSArray<EOSQLExpression> statementsToInsertColumnForAttribute(EOAttribute attribute, EOSchemaGenerationOptions options) {
			String clause = _columnCreationClauseForAttribute(attribute);
			return new NSArray<EOSQLExpression>(_expressionForString("ALTER TABLE " + formatTableName(attribute.entity().externalName()) + " ADD COLUMN " + clause));
		}

		@Override
		public NSArray<EOSQLExpression> statementsToModifyColumnNullRule(String columnName, String tableName, boolean allowsNull, EOSchemaGenerationOptions options) {
			NSArray<EOSQLExpression> statements;
			if (allowsNull) {
				statements = new NSArray<EOSQLExpression>(_expressionForString("ALTER TABLE " + formatTableName(tableName) + " ALTER COLUMN " + formatColumnName(columnName) + " SET NULL"));
			} else {
				statements = new NSArray<EOSQLExpression>(_expressionForString("ALTER TABLE " + formatTableName(tableName) + " ALTER COLUMN " + formatColumnName(columnName) + " SET NOT NULL"));
			}
			return statements;
		}

		@Override
		public NSArray<EOSQLExpression> statementsToRenameColumnNamed(String columnName, String tableName, String newName, EOSchemaGenerationOptions options) {
			return new NSArray<EOSQLExpression>(_expressionForString("ALTER TABLE " + formatTableName(tableName) + " ALTER COLUMN " + formatColumnName(columnName) + " RENAME TO " + formatColumnName(newName)));
		}

		@Override
		public NSArray<EOSQLExpression> statementsToRenameTableNamed(String oldTableName, String newTableName, EOSchemaGenerationOptions options) {
			return new NSArray<EOSQLExpression>(_expressionForString("ALTER TABLE " + formatTableName(oldTableName) + " RENAME TO " + formatTableName(newTableName)));
		}

		@Override
		public boolean supportsSchemaSynchronization() {
			return true;
		}
		
		/**
	     * <code>H2Expression</code> factory method.
	     * 
	     * @param entity
	     *            the entity to which <code>H2Expression</code> is to
	     *            be rooted
	     * @param statement
	     *            the SQL statement
	     * @return a <code>H2Expression</code> rooted to <code>entity</code>
	     */
	    private static H2Expression createExpression(EOEntity entity, String statement) {
	    	H2Expression result = new H2Expression(entity);
	    	result.setStatement(statement);
	    	return result;
	    }
	}

	private static final String DRIVER_CLASS_NAME = "org.h2.Driver";

	private static final String DRIVER_NAME = "H2";

	/**
	 * flag for whether jdbcInfo should be written out has been tested.
	 */
	private volatile boolean testedJdbcInfo;

	public _H2PlugIn(final JDBCAdaptor adaptor) {
		super(adaptor);
	}

	@Override
	public EOSchemaSynchronizationFactory createSchemaSynchronizationFactory() {
		return new H2SynchronizationFactory(adaptor());
	}

	@Override
	public String databaseProductName() {
		return DRIVER_NAME;
	}

	@Override
	public String defaultDriverName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	public Class defaultExpressionClass() {
		return H2Expression.class;
	}

	/**
	 * <p>
	 * This is usually extracted from the the database using JDBC, but this is
	 * really inconvenient for users who are trying to generate SQL at some. A
	 * specific version of the data has been written into the property list of
	 * the framework and this can be used as a hard-coded equivalent.
	 * </p>
	 * <p>
	 * Provide system property <code>h2.updateJDBCInfo=true</code> to
	 * cause H2JDBCInfo.plist to be written out to the platform temp dir.
	 * </p>
	 */
	@Override
	public NSDictionary<String, Object> jdbcInfo() {
		// optionally write out a fresh copy of the H2JDBCInfo.plist file.
		if (!testedJdbcInfo) {
			testedJdbcInfo = true;
			String property = System.getProperty("h2.updateJDBCInfo");
			if (NSPropertyListSerialization.booleanForString(property)) {
				NSLog.out.appendln("Updating H2JDBCInfo.plist enabled:" + property);
				try {
					String jdbcInfoContent = NSPropertyListSerialization.stringFromPropertyList(super.jdbcInfo());
					File tmpDir = new File(System.getProperty("java.io.tmpdir"));
					File jdbcInfoFile = new File(tmpDir, "H2JDBCInfo.plist");

					NSLog.out.appendln("Writing H2JDBCInfo.plist to " + tmpDir.getAbsolutePath());

					FileOutputStream fos = new FileOutputStream(jdbcInfoFile);
					fos.write(jdbcInfoContent.getBytes());
					fos.close();
				}
				catch (Exception e) {
					throw new IllegalStateException("problem writing H2JDBCInfo.plist", e);
				}
			}
		}

		NSDictionary<String, Object> jdbcInfo;
		// have a look at the JDBC connection URL to see if the flag has been
		// set to
		// specify that the hard-coded jdbcInfo information should be used.
		if (shouldUseBundledJdbcInfo()) {
			if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed)) {
				NSLog.debug.appendln("Loading jdbcInfo from H2JDBCInfo.plist as opposed to using the JDBCPlugIn default implementation.");
			}

			InputStream jdbcInfoStream = NSBundle.bundleForClass(getClass()).inputStreamForResourcePath("H2JDBCInfo.plist");
			if (jdbcInfoStream == null) {
				throw new IllegalStateException("Unable to find 'H2JDBCInfo.plist' in this plugin jar.");
			}

			try {
				jdbcInfo = (NSDictionary<String, Object>) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to load 'H2JDBCInfo.plist' from this plugin jar: " + e, e);
			}
			finally {
				try { jdbcInfoStream.close(); } catch (IOException e) {}
			}
		}
		else {
			jdbcInfo = super.jdbcInfo();
		}
		return jdbcInfo;
	}

	@Override
	public String name() {
		return DRIVER_NAME;
	}

	/**
	 * This method returns <code>true</code> by default unless the connection URL for the database has
	 * <code>useBundledJdbcInfo=false</code> on it which indicates to the system
	 * that the jdbcInfo which has been bundled into the plugin is not acceptable to
	 * use and instead it should fetch a fresh copy from the database.
	 * 
	 * @return <code>true</code> if bundled jdbcInfo should be used
	 */
	protected boolean shouldUseBundledJdbcInfo() {
		boolean shouldUseBundledJdbcInfo = true;
		String url = connectionURL();
		if (url != null && url.toLowerCase().matches(".*(\\?|\\?.*&)useBundledJdbcInfo=(false|no)(\\&|$)".toLowerCase())) {
			shouldUseBundledJdbcInfo = false;
		}
		return shouldUseBundledJdbcInfo;
	}
	
	/**
	 * Overrides the parent implementation to provide a more efficient mechanism
	 * for generating primary keys, while generating the primary key support on
	 * the fly.
	 * 
	 * @param count
	 *            the batch size
	 * @param entity
	 *            the entity requesting primary keys
	 * @param channel
	 *            open JDBCChannel
	 * @return NSArray of NSDictionary where each dictionary corresponds to a
	 *         unique primary key value
	 */
	@Override
	public NSArray<NSDictionary<String, Object>> newPrimaryKeys(int count, EOEntity entity, JDBCChannel channel) {
		if (isPrimaryKeyGenerationNotSupported(entity)) {
			return null;
		}

		EOAttribute attribute = entity.primaryKeyAttributes().lastObject();
		String attrName = attribute.name();
		boolean isIntType = "i".equals(attribute.valueType());

		NSMutableArray<NSDictionary<String, Object>> results = new NSMutableArray<NSDictionary<String, Object>>(count);
		String sequenceName = _sequenceNameForEntity(entity);
		H2Expression expression = new H2Expression(entity);

		int keysPerBatch = 20;
		boolean succeeded = false;
		for (int tries = 0; !succeeded && tries < 2; tries++) {
			while (results.count() < count) {
				try {
					StringBuilder sql = new StringBuilder();
					sql.append("SELECT ");
					for (int keyBatchNum = Math.min(keysPerBatch, count - results.count()) - 1; keyBatchNum >= 0; keyBatchNum--) {
						sql.append("NEXTVAL('" + sequenceName + "') AS KEY" + keyBatchNum);
						if (keyBatchNum > 0) {
							sql.append(", ");
						}
					}
					expression.setStatement(sql.toString());
					channel.evaluateExpression(expression);
					try {
						NSDictionary<String, Object> row;
						while ((row = channel.fetchRow()) != null) {
							Enumeration pksEnum = row.allValues().objectEnumerator();
							while (pksEnum.hasMoreElements()) {
								Number pkObj = (Number) pksEnum.nextElement();
								Number pk;
								if (isIntType) {
									pk = Integer.valueOf(pkObj.intValue());
								} else {
									pk = Long.valueOf(pkObj.longValue());
								}
								results.addObject(new NSDictionary<String, Object>(pk, attrName));
							}
						}
					} finally {
						channel.cancelFetch();
					}
					succeeded = true;
				} catch (JDBCAdaptorException e) {
					// jw check if H2 has already a sequence with a different name
					String tableName = entity.externalName().toUpperCase();
					String columnName = attribute.columnName().toUpperCase();
					int dotIndex = tableName.indexOf(".");
					if (dotIndex == -1) {
						expression.setStatement("select SEQUENCE_NAME, COLUMN_DEFAULT from INFORMATION_SCHEMA.COLUMNS where UPPER(TABLE_NAME) = '"
								+ tableName + "' and UPPER(COLUMN_NAME) = '" + columnName + "'");
					} else {
						String schemaName = tableName.substring(0, dotIndex);
						String tableNameOnly = tableName.substring(dotIndex + 1);
						expression.setStatement("select SEQUENCE_NAME, COLUMN_DEFAULT from INFORMATION_SCHEMA.COLUMNS where UPPER(TABLE_NAME) = '"
								+ tableNameOnly + "' and UPPER(COLUMN_NAME) = '" + columnName + "' and UPPER(TABLE_SCHEMA) = '"
								+ schemaName + "'");
					}
					channel.evaluateExpression(expression);
					NSDictionary<String, Object> row;
					try {
						row = channel.fetchRow();
					} finally {
						channel.cancelFetch();
					}
					if (row != null) {
						Object obj = row.objectForKey("SEQUENCE_NAME");
						String h2SequenceName = obj == NSKeyValueCoding.NullValue ? null : (String) obj;
						if (h2SequenceName == null) {
							obj = row.objectForKey("COLUMN_DEFAULT");
							String defaultValue = obj == NSKeyValueCoding.NullValue ? null : (String) obj;
							if (defaultValue != null) {
								final String NEXT_VAL = "NEXTVAL('";
								int startPos = defaultValue.indexOf(NEXT_VAL);
								if (startPos != -1) {
									int endPos = defaultValue.indexOf("')");
									h2SequenceName = defaultValue.substring(startPos + NEXT_VAL.length(), endPos);
								} else {
									final String NEXT_FOR = "NEXT VALUE FOR ";
									startPos = defaultValue.indexOf(NEXT_FOR);
									if (startPos != -1) {
										int dotPos = defaultValue.indexOf(".", startPos) + NEXT_FOR.length();
										if (dotPos != -1) {
											startPos = dotPos;
										}
										h2SequenceName = defaultValue.substring(startPos + 1, defaultValue.length() - 1);
									}
								}
							}
						}
						if (h2SequenceName != null) {
							// store sequence name mapping as H2 does not yet support renaming of sequences
							setSequenceNameOverride(sequenceName, h2SequenceName);
							sequenceName = h2SequenceName;
							continue;
						}
					}
					//timc 2006-11-06 Check if sequence name contains schema name
					dotIndex = sequenceName.indexOf(".");
					if (dotIndex == -1) {
						expression.setStatement("select count(*) as COUNT from INFORMATION_SCHEMA.SEQUENCES where SEQUENCE_NAME = '"
								+ sequenceName.toUpperCase() + "'");
					} else {
						String schemaName = sequenceName.substring(0, dotIndex);
						String sequenceNameOnly = sequenceName.toLowerCase().substring(dotIndex + 1);
						expression
								.setStatement("select count(*) as COUNT from INFORMATION_SCHEMA.SEQUENCES where SEQUENCE_SCHEMA = '"
										+ schemaName.toUpperCase() + "' AND SEQUENCE_NAME = '" + sequenceNameOnly.toUpperCase() + "'");
					}
					channel.evaluateExpression(expression);
					try {
						row = channel.fetchRow();
					} finally {
						channel.cancelFetch();
					}
					// timc 2006-11-06 row.objectForKey("COUNT") returns BigDecimal not Long
					Number numCount = (Number) row.objectForKey("COUNT");
					if (numCount != null && numCount.longValue() == 0L) {
						EOSchemaSynchronizationFactory f = createSchemaSynchronizationFactory();
						NSArray<EOSQLExpression> statements = f.primaryKeySupportStatementsForEntityGroup(new NSArray<EOEntity>(entity));
						int stmCount = statements.count();
						for (int i = 0; i < stmCount; i++) {
							channel.evaluateExpression(statements.objectAtIndex(i));
						}
					} else if (numCount == null) {
						throw new IllegalStateException("Couldn't call sequence " + sequenceName
								+ " and couldn't get sequence information from pg_class: " + e);
					} else {
						throw new IllegalStateException("Caught exception, but sequence did already exist: " + e);
					}
				}
			}
		}

		if (results.count() != count) {
			throw new IllegalStateException("Unable to generate primary keys from the sequence for " + entity + ".");
		}

		return results;
	}
	
	/**
	* Checks whether primary key generation can be supported for <code>entity</code>
	*
	* @param entity the entity to be checked
	* @return yes/no
	*/
	private static boolean isPrimaryKeyGenerationNotSupported(EOEntity entity) {
		return entity.primaryKeyAttributes().count() > 1 || entity.primaryKeyAttributes().lastObject().adaptorValueType() != EOAttribute.AdaptorNumberType;
	}
}
