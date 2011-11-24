package com.webobjects.jdbcadaptor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.synchronization.EOSchemaGenerationOptions;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationFactory;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation._NSStringUtilities;

public class _MySQLPlugIn extends JDBCPlugIn {

	private static final String DriverClassName = "com.mysql.jdbc.Driver";

	private static final String DriverProductName = "MySQL";
	
	private static final String QUERY_STRING_USE_BUNDLED_JDBC_INFO = "useBundledJdbcInfo";

	public _MySQLPlugIn(JDBCAdaptor adaptor) {
		super(adaptor);
	}

	public static class MySQLExpression extends JDBCExpression {

		private int _fetchLimit;

		
		public MySQLExpression(EOEntity entity) {
			super(entity);
		}

		@Override
        public char sqlEscapeChar(){
			return '|';
		}

		/* (non-Javadoc)
		 *
		 * Overriding super here so we can grab a fetch limit if specified in the EOFetchSpecification.
		 *
		 * @see com.webobjects.jdbcadaptor.JDBCExpression#prepareSelectExpressionWithAttributes(NSArray, boolean, EOFetchSpecification)
		 */
		@Override
		public void prepareSelectExpressionWithAttributes(NSArray<EOAttribute> attributes, boolean lock,
				EOFetchSpecification fetchSpec) {
			if (!fetchSpec.promptsAfterFetchLimit()) {
				_fetchLimit = fetchSpec.fetchLimit();
			}
			super.prepareSelectExpressionWithAttributes(attributes, lock, fetchSpec);
		}

		/* (non-Javadoc)
		 *
		 * Overriding to add LIMIT clause if _fetchLimit > 0.
		 * This is same logic as original super with minor additions to support LIMIT clause only.
		 *
		 * @see com.webobjects.eoaccess.EOSQLExpression#assembleSelectStatementWithAttributes(NSArray, boolean, EOQualifier, NSArray, java.lang.String, String, String, String, String, String, String)
		 */
		@Override
		public String assembleSelectStatementWithAttributes(NSArray attributes, boolean lock, EOQualifier qualifier,
				NSArray fetchOrder, String selectString, String columnList, String tableList, String whereClause,
				String joinClause, String orderByClause, String lockClause) {
			String limitClause = null;

			int size = selectString.length() + columnList.length() + tableList.length() + 7;
			if ((lockClause != null) && (lockClause.length() != 0))
				size += lockClause.length() + 1;
			if ((whereClause != null) && (whereClause.length() != 0))
				size += whereClause.length() + 7;
			if ((joinClause != null) && (joinClause.length() != 0))
				size += joinClause.length() + 7;
			if ((orderByClause != null) && (orderByClause.length() != 0)) {
				size += orderByClause.length() + 10;
			}

			// If necessary, create LIMIT clause and add to buffer size
			if (_fetchLimit > 0) {
				limitClause = Integer.toString(_fetchLimit);
				size += 7 + limitClause.length();  // " LIMIT " = 7 chars
			}

			// Use a StringBuilder here since synchronized StringBuffer not needed.
			StringBuilder buffer = new StringBuilder(size);
			buffer.append(selectString);
			buffer.append(columnList);
			buffer.append(" FROM ");
			buffer.append(tableList);

			if ((whereClause != null) && (whereClause.length() != 0)) {
				buffer.append(" WHERE ");
				buffer.append(whereClause);
			}
			if ((joinClause != null) && (joinClause.length() != 0)) {
				if ((whereClause != null) && (whereClause.length() != 0))
					buffer.append(" AND ");
				else
					buffer.append(" WHERE ");
				buffer.append(joinClause);
			}

			if ((orderByClause != null) && (orderByClause.length() != 0)) {
				buffer.append(" ORDER BY ");
				buffer.append(orderByClause);
			}

			// Add limit clause
			if (limitClause != null) {
				buffer.append(" LIMIT ");
				buffer.append(limitClause);
			}

			if ((lockClause != null) && (lockClause.length() != 0)) {
				buffer.append(' ');
				buffer.append(lockClause);
			}

			return buffer.toString();
		}

	}

	public static class MySQLSynchronizationFactory extends EOSchemaSynchronizationFactory {

		public MySQLSynchronizationFactory(EOAdaptor adaptor) {
			super(adaptor);
		}

        @Override
        public String _alterPhraseInsertionClausePrefixAtIndex(int columnIndex) {
            return (columnIndex != 0)?"":" ADD ";
        }

		@Override
        protected String formatTableName(String name) {
			return name;
		}

		@Override
		protected String formatColumnName(String name) {
			return name;
		}
		
		@Override
        public NSArray<EOSQLExpression> statementsToConvertColumnType(String columnName, String tableName, ColumnTypes oldType, ColumnTypes newType, EOSchemaGenerationOptions options) {
		    String columnTypeString = statementToCreateDataTypeClause(newType);
            StringBuilder sb = new StringBuilder();
            sb.append("ALTER TABLE ").append(formatTableName(tableName));
            sb.append(" MODIFY ").append(formatColumnName(columnName));
            sb.append(' ').append(columnTypeString);
            NSArray<EOSQLExpression> statements = new NSArray<EOSQLExpression>(_expressionForString(sb.toString()));
            return statements;
        }

		@Override
		public NSArray<EOSQLExpression> primaryKeySupportStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			String pkTable = ((JDBCAdaptor)adaptor()).plugIn().primaryKeyTableName();
			NSMutableArray<EOSQLExpression> statements = new NSMutableArray<EOSQLExpression>();
			statements.addObject(_expressionForString((new StringBuilder()).append("CREATE TABLE ").append(pkTable).append(" (NAME CHAR(40) PRIMARY KEY, PK INT)").toString()));
			return statements;
		}
		
		@Override
        public NSArray<EOSQLExpression> statementsToDeleteColumnNamed(String columnName, String tableName, EOSchemaGenerationOptions options) {
            return new NSArray<EOSQLExpression>(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(tableName).append(" DROP COLUMN ").append(columnName).toString()));
        }
		
		@Override
        public NSArray<EOSQLExpression> statementsToInsertColumnForAttribute(EOAttribute attribute, EOSchemaGenerationOptions options) {
            String columnCreationClause = _columnCreationClauseForAttribute(attribute);
            return new NSArray<EOSQLExpression>(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(attribute.entity().externalName()).append(_alterPhraseInsertionClausePrefixAtIndex(0)).append(columnCreationClause).toString()));
        }
		
		@Override
    public NSArray<EOSQLExpression> statementsToModifyColumnNullRule(String columnName, String tableName, boolean allowsNull, EOSchemaGenerationOptions options) {
		    String nullStatement = allowsNull ? " NULL" : " NOT NULL";
		    EOAttribute attribute = attributeInEntityWithColumnName(entityForTableName(tableName), columnName);
		    String externalType = columnTypeStringForAttribute(attribute);
		    return new NSArray<EOSQLExpression>(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(formatTableName(tableName)).append(" MODIFY ").append(formatColumnName(columnName)).append(" ").append(externalType).append(nullStatement).toString()));
		}
		
		@Override
		public boolean supportsDirectColumnNullRuleModification() {
		    return true;
		}
		
		@Override
		public NSArray<EOSQLExpression> statementsToRenameColumnNamed(String columnName, String tableName, String newName, EOSchemaGenerationOptions options) {
		    EOAttribute attribute = attributeInEntityWithColumnName(entityForTableName(tableName), newName);
		    String nullStatement = attribute.allowsNull() ? " NULL" : " NOT NULL";
		    String externalType = columnTypeStringForAttribute(attribute);
		    return new NSArray<EOSQLExpression>(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(formatTableName(tableName)).append(" CHANGE ").append(formatColumnName(columnName)).append(" ").append(formatColumnName(newName)).append(" ").append(externalType).append(nullStatement).toString()));
		}
		
		@Override
		public boolean supportsDirectColumnRenaming() {
		    return true;
		}
		
		private EOEntity entityForTableName(String tableName) {
		    EOModelGroup modelGroup = EOModelGroup.globalModelGroup();
            for (EOModel model : modelGroup.models()) {
                for (EOEntity entity : model.entities()) {
                    if (entity.externalName() != null && entity.externalName().equalsIgnoreCase(tableName)) {
                        return entity;
                    }
                }
            }
            return null;
        }
		
		@Override
		public NSArray<EOSQLExpression> dropPrimaryKeySupportStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			return new NSArray<EOSQLExpression>(_expressionForString((new StringBuilder()).append("DROP TABLE ").append(((JDBCAdaptor)adaptor()).plugIn().primaryKeyTableName()).append(" CASCADE").toString()));
		}

		@Override
		public NSArray<EOSQLExpression> _statementsToDropPrimaryKeyConstraintsOnTableNamed(String tableName) {
			return new NSArray<EOSQLExpression>(_expressionForString((new StringBuilder()).append("alter table ").append(tableName).append(" drop primary key").toString()));
		}

		@Override
		public NSArray<EOSQLExpression> foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
			return null;
		}

		@Override
		public NSArray<EOSQLExpression> statementsToRenameTableNamed(String tableName, String newName, EOSchemaGenerationOptions options) {
			return new NSArray<EOSQLExpression>(_expressionForString((new StringBuilder()).append("rename table ").append(tableName).append(" to ").append(newName).toString()));
		}

		@Override
		public boolean supportsSchemaSynchronization() {
			return true;
		}
		
		// Shameless stolen from PostresqlSynchronizationFactory - davidleber
		//
    // I blame statementstToConvertColumnType for not taking a damn EOAttribute for
    // having to steal this from EOSQLExpression
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
    
		private String statementToCreateDataTypeClause(ColumnTypes columntypes) {
			int size = columntypes.precision();
			if (size == 0) {
				size = columntypes.width();
			}

			if (size == 0) {
				return columntypes.name();
			}

			int scale = columntypes.scale();
			if (scale == 0) {
				return columntypes.name() + "(" + size + ")";
			}

			return columntypes.name() + "(" + size + "," + scale + ")";
		}

	}

	/**
	 * <p>WebObjects 5.4's version of JDBCAdaptor will use this in order to
	 * assemble the name of the prototype to use when it loads models.</p>
	 * @return Name of the plugin.
	 */
	@Override
    public String name() {
		return DriverProductName;
	}

	@Override
	public String defaultDriverName() {
		return DriverClassName;
	}

	@Override
	public String databaseProductName() {
		return DriverProductName;
	}

	@Override
	public Class<com.webobjects.jdbcadaptor._MySQLPlugIn.MySQLExpression> defaultExpressionClass() {
		return com.webobjects.jdbcadaptor._MySQLPlugIn.MySQLExpression.class;
	}

	@Override
	public EOSchemaSynchronizationFactory createSchemaSynchronizationFactory() {
		return new com.webobjects.jdbcadaptor._MySQLPlugIn.MySQLSynchronizationFactory(_adaptor);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public NSDictionary<String, Object> jdbcInfo() {

		NSDictionary<String, Object> jdbcInfo;
		// have a look at the JDBC connection URL to see if the flag has been set to
		// specify that the hard-coded jdbcInfo information should be used.
		if(shouldUseBundledJdbcInfo()) {
			if(NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed)) {
				NSLog.debug.appendln("Loading jdbcInfo from JDBCInfo.plist as opposed to using the JDBCPlugIn default implementation.");
			}

			InputStream jdbcInfoStream = NSBundle.bundleForClass(getClass()).inputStreamForResourcePath("JDBCInfo.plist");
			if (jdbcInfoStream == null) {
				throw new IllegalStateException("Unable to find 'JDBCInfo.plist' in this plugin jar.");
			}

			try {
				jdbcInfo = (NSDictionary<String, Object>) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
			} catch (IOException e) {
				throw new RuntimeException("Failed to load 'JDBCInfo.plist' from this plugin jar.", e);
			}

	    } else {

			NSMutableDictionary<String, Object> mutableInfo = super.jdbcInfo().mutableClone();
			NSMutableDictionary<String, NSDictionary> typeInfo = ((NSDictionary<String, NSDictionary>)mutableInfo.objectForKey("typeInfo")).mutableClone();
			NSDictionary textTypeInfo = typeInfo.objectForKey("TEXT");
			if(textTypeInfo != null) {
				Object rawCreateParams = textTypeInfo.objectForKey("createParams");
				if(!rawCreateParams.equals("1")) {
					NSMutableDictionary newRawTypeInfo = textTypeInfo.mutableClone();
					newRawTypeInfo.setObjectForKey("1", "createParams");
					typeInfo.setObjectForKey(newRawTypeInfo, "RAW");
				}
			}
			JDBCPlugIn._takeValueForKeyPath(typeInfo, "0", "BLOB", "createParams");
			JDBCPlugIn._takeValueForKeyPath(typeInfo, "0", "LONGBLOB", "createParams");
			JDBCPlugIn._takeValueForKeyPath(typeInfo, "0", "MEDIUMBLOB", "createParams");
			JDBCPlugIn._takeValueForKeyPath(typeInfo, "0", "TINYBLOB", "createParams");
			mutableInfo.setObjectForKey(typeInfo, "typeInfo");
			
			NSLog.debug.appendln(
					new StringBuilder("fetched MySQL (")
					.append(databaseProductName())
					.append(") JDBC Info = ")
					.append(mutableInfo)
					.toString()
					);
			
			// Write a fresh copy of JDBCInfo.plist to /tmp
			//writeJDBCInfo(mutableInfo);
			
			jdbcInfo = mutableInfo.immutableClone();
	    }
		return jdbcInfo;
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
	
	/**
	 * <P>This method returns true if the connection URL for the 
	 * database has a special flag on it which indicates to the 
	 * system that the jdbcInfo which has been bundled into the 
	 * plugin is acceptable to use in place of actually going to 
	 * the database and getting it. Default is false.
	 * @return the flag set on the jdbc url with 'useBundledJdbcInfo'
	 */
	protected boolean shouldUseBundledJdbcInfo() {
		boolean shouldUseBundledJdbcInfo = false;
		String url = connectionURL();
		if (url != null) {
			shouldUseBundledJdbcInfo = url.toLowerCase().matches(".*(\\?|\\?.*&)" + _MySQLPlugIn.QUERY_STRING_USE_BUNDLED_JDBC_INFO.toLowerCase() + "=(true|yes)(\\&|$)");
		}
		return shouldUseBundledJdbcInfo;
	}

	protected void writeJDBCInfo(NSDictionary<String, Object> jdbcInfo) {
		try {
			String jdbcInfoS = NSPropertyListSerialization.stringFromPropertyList(jdbcInfo);
			FileOutputStream fos = new FileOutputStream("/tmp/JDBCInfo.plist");
			fos.write(jdbcInfoS.getBytes());
			fos.close();
		} catch(Exception e) {
			throw new IllegalStateException("problem writing JDBCInfo.plist",e);
		}
	}

}
