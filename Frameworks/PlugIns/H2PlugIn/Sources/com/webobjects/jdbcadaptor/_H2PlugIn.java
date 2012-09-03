package com.webobjects.jdbcadaptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.synchronization.EOSchemaGenerationOptions;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronization;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSStringUtilities;

/**
 * WO runtime plugin with support for H2.
 * 
 * @see <a href="http://www.h2database.com/">http://www.h2database.com</a>
 */
public class _H2PlugIn extends JDBCPlugIn {
	static final boolean USE_NAMED_CONSTRAINTS = true;

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
		return new StringBuilder("'").append(string).append("'").toString();
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

		public H2Expression(final EOEntity entity) {
			super(entity);
		}

		@Override
		public void addCreateClauseForAttribute(final EOAttribute attribute) {
			StringBuffer sql = new StringBuffer();
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
		public String formatValueForAttribute(final Object value, final EOAttribute eoattribute) {
			String result;
			if (value instanceof NSData) {
				result = sqlStringForData((NSData) value);
			}
			else if (value instanceof NSTimestamp && isTimestampAttribute(eoattribute)) {
				result = singleQuotedString(timestampFormatter().format(value));
			}
			else if (value instanceof NSTimestamp && isDateAttribute(eoattribute)) {
				result = singleQuotedString(dateFormatter().format(value));
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
		 * @return
		 */
		private boolean isDateAttribute(final EOAttribute eoattribute) {
			return eoattribute != null && "D".equals(eoattribute.valueType());
		}

		/**
		 * Helper to check for timestamp columns that have a "T" value type.
		 *
		 * @param eoattribute
		 * @return
		 */
		private boolean isTimestampAttribute(final EOAttribute eoattribute) {
			return eoattribute != null && "T".equals(eoattribute.valueType());
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
		public NSArray<EOSQLExpression> dropPrimaryKeySupportStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
			return new NSArray<EOSQLExpression>(_expressionForString("DROP TABLE " + formatTableName(pkTable)));
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
				StringBuffer sql = new StringBuffer();
				String tableName = formatTableName(relationship.entity().externalName());

				sql.append("ALTER TABLE ");
				sql.append(tableName);
				sql.append(" ADD");

				StringBuilder constraint = new StringBuilder(" CONSTRAINT \"FOREIGN_KEY_");
				constraint.append(formatUpperString(tableName));

				StringBuilder fkSql = new StringBuilder(" FOREIGN KEY (");
				NSArray<EOAttribute> attributes = relationship.sourceAttributes();

				for (int i = 0; i < attributes.count(); i++) {
					constraint.append("_");
					if (i != 0)
						fkSql.append(", ");

					String columnName = formatColumnName(attributes.objectAtIndex(i).columnName());
					fkSql.append(columnName);
					constraint.append(formatUpperString(columnName));
				}

				fkSql.append(") REFERENCES ");
				constraint.append("_");

				String referencedExternalName = formatTableName(relationship.destinationEntity().externalName());
				fkSql.append(referencedExternalName);
				constraint.append(formatUpperString(referencedExternalName));

				fkSql.append(" (");

				attributes = relationship.destinationAttributes();

				for (int i = 0; i < attributes.count(); i++) {
					constraint.append("_");
					if (i != 0)
						fkSql.append(", ");

					String referencedColumnName = formatColumnName(attributes.objectAtIndex(i).columnName());
					fkSql.append(referencedColumnName);
					constraint.append(formatUpperString(referencedColumnName));
				}

				// MS: did i write this code?  sorry about that everything. this is crazy. 
				constraint.append('"');

				fkSql.append(")");
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
		public NSArray<EOSQLExpression> primaryKeySupportStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
			String charField = formatColumnName("name") + " CHAR(40)";
			String pkField = formatColumnName("pk") + " INT";
			return new NSArray<EOSQLExpression>(_expressionForString("CREATE TABLE " + formatTableName(pkTable) + " (" + charField + ", " + pkField + ")"));
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
	}

	private static final String DRIVER_CLASS_NAME = "org.h2.Driver";

	private static final String DRIVER_NAME = "H2";

	/**
	 * formatter to use when handling date columns
	 */
	private static Format dateFormatter() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}

	/**
	 * formatter to use when handling timestamps
	 */
	private static Format timestampFormatter() {
		return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
	}

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
	public NSDictionary jdbcInfo() {
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

		NSDictionary jdbcInfo;
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
				jdbcInfo = (NSDictionary) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to load 'H2JDBCInfo.plist' from this plugin jar: " + e, e);
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
}
