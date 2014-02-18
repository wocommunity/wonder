package com.webobjects.jdbcadaptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

public class _DerbyPlugIn extends JDBCPlugIn {
	static final boolean USE_NAMED_CONSTRAINTS = true;
	
	protected static String quoteTableName(String s) {
		if (s == null)
			return null;
		int i = s.lastIndexOf(46);
		if (i == -1)
			return "\"" + s + "\"";
		else
			return "\"" + s.substring(0, i) + "\".\"" + s.substring(i + 1, s.length()) + "\"";
	}

	public static class DerbyExpression extends JDBCExpression {
		// more to come
		public DerbyExpression(final EOEntity entity) {
			super(entity);
		}

		@Override
		public void addCreateClauseForAttribute(final EOAttribute attribute) {
			StringBuilder sql = new StringBuilder();
			sql.append(attribute.columnName());
			sql.append(' ');
			sql.append(columnTypeStringForAttribute(attribute));

			NSDictionary userInfo = attribute.userInfo();
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
		public String formatValueForAttribute(final Object obj, final EOAttribute eoattribute) {
			String value;
			if (obj instanceof NSData) {
				value = sqlStringForData((NSData) obj);
			}
			else if ((obj instanceof NSTimestamp) && isTimestampAttribute(eoattribute)) {
				value = "'" + TIMESTAMP_FORMATTER.format(obj) + "'";
			}
			else if ((obj instanceof NSTimestamp) && isDateAttribute(eoattribute)) {
				value = "'" + DATE_FORMATTER.format(obj) + "'";
			}
			else if (obj instanceof String) {
				value = formatStringValue((String) obj);
			}
			else if (obj instanceof Number) {
				if (obj instanceof BigDecimal) {
					value = formatBigDecimal((BigDecimal) obj, eoattribute);
				}
				else {
					Object convertedObj = eoattribute.adaptorValueByConvertingAttributeValue(obj);
					if (convertedObj instanceof Number) {
						String valueType = eoattribute.valueType();
						if (valueType == null || "i".equals(valueType)) {
							value = String.valueOf(((Number) convertedObj).intValue());
						}
						else if ("l".equals(valueType)) {
							value = String.valueOf(((Number) convertedObj).longValue());
						}
						else if ("f".equals(valueType)) {
							value = String.valueOf(((Number) convertedObj).floatValue());
						}
						else if ("d".equals(valueType)) {
							value = String.valueOf(((Number) convertedObj).doubleValue());
						}
						else if ("s".equals(valueType)) {
							value = String.valueOf(((Number) convertedObj).shortValue());
						}
						else {
							value = convertedObj.toString();
						}
					}
					else {
						value = convertedObj.toString();
					}
				}
			}
			else if (obj instanceof Boolean) {
				// GN: when booleans are stored as strings in the db, we need
				// the values quoted
				if (enableBooleanQuoting()) {
					value = "'" + ((Boolean) obj).toString() + "'";
				}
				else {
					value = ((Boolean) obj).toString();
				}
			}
			else if (obj instanceof Timestamp) {
				value = "'" + ((Timestamp) obj).toString() + "'";
			}
			else if (obj == null || obj == NSKeyValueCoding.NullValue) {
				value = "NULL";
			}
			else {
				// AK: I don't really like this, but we might want to prevent
				// infinite recursion
				try {
					Object adaptorValue = eoattribute.adaptorValueByConvertingAttributeValue(obj);
					if (adaptorValue instanceof NSData || adaptorValue instanceof NSTimestamp || adaptorValue instanceof String || adaptorValue instanceof Number || adaptorValue instanceof Boolean) {
						value = formatValueForAttribute(adaptorValue, eoattribute);
					}
					else {
						NSLog.err.appendln(getClass().getName() + ": Can't convert: " + obj + ":" + obj.getClass() + " -> " + adaptorValue + ":" + adaptorValue.getClass());
						value = obj.toString();
					}
				}
				catch (Exception ex) {
					NSLog.err.appendln(getClass().getName() + ": Exception while converting " + obj.getClass().getName());
					NSLog.err.appendln(ex);
					value = obj.toString();
				}
			}
			return value;
		}

		/**
		 * Helper to check for timestamp columns that have a "D" value type.
		 *
		 * @param eoattribute
		 * @return <code>true</code> if date attribute
		 */
		private boolean isDateAttribute(final EOAttribute eoattribute) {
			return "D".equals(eoattribute.valueType());
		}

		/**
		 * Helper to check for timestamp columns that have a "T" value type.
		 *
		 * @param eoattribute
		 * @return <code>true</code> if timestamp attribute
		 */
		private boolean isTimestampAttribute(final EOAttribute eoattribute) {
			return "T".equals(eoattribute.valueType());
		}
	}

	public static class DerbySynchronizationFactory extends EOSynchronizationFactory {
		public DerbySynchronizationFactory(final EOAdaptor adaptor) {
			super(adaptor);
		}

		@Override
		public String _columnCreationClauseForAttribute(EOAttribute attribute) {
			return addCreateClauseForAttribute(attribute).toString();
		}
		
		public StringBuffer addCreateClauseForAttribute(EOAttribute attribute) {
			EOSQLExpression expression = _expressionForEntity(attribute.entity());
			expression.addCreateClauseForAttribute(attribute);
			return new StringBuffer(expression.listString());
		}
		
		@Override
		public NSArray<EOSQLExpression> _statementsToDropPrimaryKeyConstraintsOnTableNamed(final String tableName) {
			return new NSArray<EOSQLExpression>(_expressionForString("alter table " + formatTableName(tableName) + " drop primary key"));
		}

		@Override
		public NSArray<EOSQLExpression> dropPrimaryKeySupportStatementsForEntityGroups(final NSArray entityGroups) {
			String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
			return new NSArray<EOSQLExpression>(_expressionForString("drop table " + formatTableName(pkTable)));
		}

		@Override
		public NSArray<EOSQLExpression> dropTableStatementsForEntityGroup(final NSArray<EOEntity> entityGroup) {
			return new NSArray<EOSQLExpression>(_expressionForString("drop table " + formatTableName(entityGroup.objectAtIndex(0).externalName())));
		}
		
		@Override
		public NSArray<EOSQLExpression> statementsToModifyColumnNullRule(String columnName, String tableName, boolean allowsNull, NSDictionary nsdictionary) {
			NSArray<EOSQLExpression> statements;
			if (allowsNull) {
				statements = new NSArray<EOSQLExpression>(_expressionForString("alter table " + formatTableName(tableName) + " alter column " + formatColumnName(columnName) + " null"));
			} else {
				statements = new NSArray<EOSQLExpression>(_expressionForString("alter table " + formatTableName(tableName) + " alter column " + formatColumnName(columnName) + " not null"));
			}
			return statements;
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
			if (!relationship.isToMany() && isPrimaryKeyAttributes(relationship.destinationEntity(), relationship.destinationAttributes())) {
				StringBuilder sql = new StringBuilder();
				String tableName = relationship.entity().externalName();

				sql.append("ALTER TABLE ");
				sql.append(quoteTableName(tableName.toUpperCase()));
				sql.append(" ADD");

				StringBuilder constraint = new StringBuilder(" CONSTRAINT \"FOREIGN_KEY_");
				constraint.append(tableName);

				StringBuilder fkSql = new StringBuilder(" FOREIGN KEY (");
				NSArray<EOAttribute> attributes = relationship.sourceAttributes();

				for (int i = 0; i < attributes.count(); i++) {
					constraint.append('_');
					if (i != 0)
						fkSql.append(", ");

					fkSql.append("\"");
					String columnName = attributes.objectAtIndex(i).columnName();
					fkSql.append(columnName.toUpperCase());
					constraint.append(columnName);
					fkSql.append("\"");
				}

				fkSql.append(") REFERENCES ");
				constraint.append('_');

				String referencedExternalName = relationship.destinationEntity().externalName();
				fkSql.append(quoteTableName(referencedExternalName.toUpperCase()));
				constraint.append(referencedExternalName);

				fkSql.append(" (");

				attributes = relationship.destinationAttributes();

				for (int i = 0; i < attributes.count(); i++) {
					constraint.append('_');
					if (i != 0)
						fkSql.append(", ");

					fkSql.append("\"");
					String referencedColumnName = attributes.objectAtIndex(i).columnName();
					fkSql.append(referencedColumnName.toUpperCase());
					constraint.append(referencedColumnName);
					fkSql.append("\"");
				}
				
				// MS: did i write this code?  sorry about that everything. this is crazy. 
				constraint.append("\"");

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
		public NSArray<EOSQLExpression> primaryKeySupportStatementsForEntityGroups(final NSArray entityGroups) {
			String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
			return new NSArray<EOSQLExpression>(_expressionForString("create table " + formatTableName(pkTable) + " (name char(40) primary key, pk INT)"));
		}
		
		@Override
		public NSArray<EOSQLExpression> statementsToRenameColumnNamed(String columnName, String tableName, String newName, NSDictionary nsdictionary) {
			return new NSArray<EOSQLExpression>(_expressionForString("rename column " + formatTableName(tableName) + "." + formatColumnName(columnName) + " to " + formatColumnName(newName)));
		}

		@Override
		public NSArray<EOSQLExpression> statementsToInsertColumnForAttribute(final EOAttribute attribute, final NSDictionary options) {
			String clause = _columnCreationClauseForAttribute(attribute);
			return new NSArray(_expressionForString("alter table " + formatTableName(attribute.entity().externalName()) + " add column " + clause));
		}
		
		@Override
	    public NSArray<EOSQLExpression> statementsToRenameTableNamed(String tableName, String newName, NSDictionary options) {
	    	return new NSArray<EOSQLExpression>(_expressionForString("rename table " + formatTableName(tableName) + " to " + formatTableName(newName)));
	    }

		@Override
		public boolean supportsSchemaSynchronization() {
			return true;
		}
	}

	private static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";

	private static final String DRIVER_NAME = "Derby";

	/**
	 * formatter to use when handling date columns
	 */
	private static final NSTimestampFormatter DATE_FORMATTER = new NSTimestampFormatter("%Y-%m-%d");

	/**
	 * formatter to use when handling timestamps
	 */
	private static final NSTimestampFormatter TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");

	/**
	 * Method to get the string value from a BigDecimals from.
	 */
	private static Method _bigDecimalToString = null;

	public _DerbyPlugIn(final JDBCAdaptor adaptor) {
		super(adaptor);
	}

	@Override
	public EOSynchronizationFactory createSynchronizationFactory() {
		return new DerbySynchronizationFactory(adaptor());
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
		return DerbyExpression.class;
	}

	/**
	 * This is usually extracted from the the database using JDBC, but this is
	 * really inconvenient for users who are trying to generate SQL at some. A
	 * specific version of the data has been written into the property list of
	 * the framework and this can be used as a hard-coded equivalent.
	 * 
	 * @return JDBC info
	 */
	@Override
	public NSDictionary jdbcInfo() {
		// you can swap this code out to write the property list out in order
		// to get a fresh copy of the JDBCInfo.plist.
		// try {
		// String jdbcInfoS =
		// NSPropertyListSerialization.stringFromPropertyList(super.jdbcInfo());
		// FileOutputStream fos = new FileOutputStream("/tmp/JDBCInfo.plist");
		// fos.write(jdbcInfoS.getBytes());
		// fos.close();
		// }
		// catch(Exception e) {
		// throw new IllegalStateException("problem writing JDBCInfo.plist",e);
		// }

		NSDictionary jdbcInfo;
		// have a look at the JDBC connection URL to see if the flag has been
		// set to
		// specify that the hard-coded jdbcInfo information should be used.
		if (shouldUseBundledJdbcInfo()) {
			if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed)) {
				NSLog.debug.appendln("Loading jdbcInfo from JDBCInfo.plist as opposed to using the JDBCPlugIn default implementation.");
			}

			InputStream jdbcInfoStream = NSBundle.bundleForClass(getClass()).inputStreamForResourcePath("JDBCInfo.plist");
			if (jdbcInfoStream == null) {
				throw new IllegalStateException("Unable to find 'JDBCInfo.plist' in this plugin jar.");
			}

			try {
				jdbcInfo = (NSDictionary) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to load 'JDBCInfo.plist' from this plugin jar: " + e, e);
			} finally {
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
	 * This method returns <code>true</code> if the connection URL for the database has
	 * <code>useBundledJdbcInfo=true</code> on it which indicates to the system
	 * that the jdbcInfo which has been bundled into the plugin is acceptable to
	 * use in place of actually going to the database and getting it.
	 * 
	 * @return <code>true</code> if bundled JDBC info should be used
	 */
	protected boolean shouldUseBundledJdbcInfo() {
		boolean shouldUseBundledJdbcInfo = false;
		String url = connectionURL();
		if (url != null) {
			shouldUseBundledJdbcInfo = url.toLowerCase().matches(".*(\\?|\\?.*&)useBundledJdbcInfo=(true|yes)(\\&|$)".toLowerCase());
		}
		return shouldUseBundledJdbcInfo;
	}
}
