package com.webobjects.jdbcadaptor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

public class _MySQLPlugIn extends JDBCPlugIn {

	private static final String DriverClassName = "com.mysql.jdbc.Driver";

	private static final String DriverProductName = "MySQL";
	
	private static final String QUERY_STRING_USE_BUNDLED_JDBC_INFO = "useBundledJdbcInfo";

	public _MySQLPlugIn(JDBCAdaptor adaptor) {
		super(adaptor);
	}

	public static class MySQLExpression extends JDBCExpression {
		
		public MySQLExpression(EOEntity entity) {
			super(entity);
		}

		@Override
        public char sqlEscapeChar(){
			return '|';
		}

	}

	public static class MySQLSynchronizationFactory extends EOSynchronizationFactory {

		public MySQLSynchronizationFactory(EOAdaptor adaptor) {
			super(adaptor);
		}

        @Override
        public String _alterPhraseInsertionClausePrefixAtIndex(int columnIndex) {
            return (columnIndex != 0) ? "" : " ADD ";
        }

        protected String formatTableName(String name) {
			return name;
		}

		protected String formatColumnName(String name) {
			return name;
		}

		@Override
		public NSArray/*<EOSQLExpression>*/ primaryKeySupportStatementsForEntityGroups(NSArray/*<NSArray<EOEntity>>*/ entityGroups) {
			String pkTable = ((JDBCAdaptor)adaptor()).plugIn().primaryKeyTableName();
			NSMutableArray/*<EOSQLExpression>*/ statements = new NSMutableArray/*<EOSQLExpression>*/();
			statements.addObject(_expressionForString((new StringBuilder()).append("CREATE TABLE ").append(pkTable).append(" (NAME CHAR(40) PRIMARY KEY, PK INT)").toString()));
			return statements;
		}
		
		@SuppressWarnings("rawtypes")
        public EOAttribute attributeInEntityWithColumnName(EOEntity entity, String columnName) {
		    for (Enumeration e = entity.attributes().objectEnumerator(); e.hasMoreElements();) {
		        EOAttribute attribute = (EOAttribute)e.nextElement();
		        if (attribute.columnName().equals(columnName)) {
		            return attribute;
		        }
		    }
		    return null;
		}
		
		@Override
        public NSArray/*<EOSQLExpression>*/ statementsToModifyColumnNullRule(String columnName, String tableName, boolean allowsNull, NSDictionary options) {
		    String nullStatement = allowsNull ? " NULL" : " NOT NULL";
		    EOAttribute attribute = attributeInEntityWithColumnName(entityForTableName(tableName), columnName);
		    return new NSArray/*<EOSQLExpression>*/(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(formatTableName(tableName)).append(" MODIFY ").append(formatColumnName(columnName)).append(" ").append(attribute.externalType()).append(nullStatement).toString()));
		}
		
		@Override
		public boolean supportsDirectColumnNullRuleModification() {
		    return true;
		}
		
		@Override
		public NSArray/*<EOSQLExpression>*/ statementsToRenameColumnNamed(String columnName, String tableName, String newName, NSDictionary options) {
		    EOAttribute attribute = attributeInEntityWithColumnName(entityForTableName(tableName), newName);
		    String nullStatement = attribute.allowsNull() ? " NULL" : " NOT NULL";
		    return new NSArray/*<EOSQLExpression>*/(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(formatTableName(tableName)).append(" CHANGE ").append(formatColumnName(columnName)).append(" ").append(formatColumnName(newName)).append(" ").append(attribute.externalType()).append(nullStatement).toString()));
		}
		
		@Override
		public boolean supportsDirectColumnRenaming() {
		    return true;
		}
		
		@SuppressWarnings("rawtypes")
        private EOEntity entityForTableName(String tableName) {
		    EOModelGroup modelGroup = EOModelGroup.globalModelGroup();
		    for (Enumeration modelEnumeration = modelGroup.models().objectEnumerator(); modelEnumeration.hasMoreElements();) {
		        EOModel model = (EOModel)modelEnumeration.nextElement();
		        for (Enumeration entityEnumeration = model.entities().objectEnumerator(); entityEnumeration.hasMoreElements();) {
		            EOEntity entity = (EOEntity)entityEnumeration.nextElement();
		            if (entity.externalName().equalsIgnoreCase(tableName)) {
                        return entity;
                    }
	            }
		    }
            return null;
        }
		
		@Override
		public NSArray/*<EOSQLExpression>*/ statementsToDeleteColumnNamed(String columnName, String tableName, NSDictionary options) {
		    return new NSArray/*<EOSQLExpression>*/(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(tableName).append(" DROP COLUMN ").append(columnName).toString()));
		}

		@Override
		public NSArray/*<EOSQLExpression>*/ statementsToInsertColumnForAttribute(EOAttribute attribute, NSDictionary/*<String, String>*/ options) {
		    String columnCreationClause = _columnCreationClauseForAttribute(attribute);
		    return new NSArray/*<EOSQLExpression>*/(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(attribute.entity().externalName()).append(_alterPhraseInsertionClausePrefixAtIndex(0)).append(columnCreationClause).toString()));
		  
		  //return new NSArray/*<EOSQLExpression>*/(_expressionForString((new StringBuilder()).append("ALTER TABLE ").append(attribute.entity().externalName()).append(" ADD ").append(attribute.columnName()).append(" ").append(attribute.externalType()).toString()));
		}
		
		@Override
		public NSArray/*<EOSQLExpression>*/ dropPrimaryKeySupportStatementsForEntityGroups(NSArray/*<NSArray<EOEntity>>*/ entityGroups) {
			return new NSArray/*<EOSQLExpression>*/(_expressionForString((new StringBuilder()).append("DROP TABLE ").append(((JDBCAdaptor)adaptor()).plugIn().primaryKeyTableName()).append(" CASCADE").toString()));
		}

		@Override
		public NSArray/*<EOSQLExpression>*/ _statementsToDropPrimaryKeyConstraintsOnTableNamed(String tableName) {
			return new NSArray/*<EOSQLExpression>*/(_expressionForString((new StringBuilder()).append("alter table ").append(tableName).append(" drop primary key").toString()));
		}

		@Override
		public NSArray/*<EOSQLExpression>*/ foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
			return null;
		}

		@Override
		public NSArray/*<EOSQLExpression>*/ statementsToRenameTableNamed(String tableName, String newName, NSDictionary options) {
			return new NSArray/*<EOSQLExpression>*/(_expressionForString((new StringBuilder()).append("rename table ").append(tableName).append(" to ").append(newName).toString()));
		}

		@Override
		public boolean supportsSchemaSynchronization() {
			return true;
		}

	}

	/**
	 * <p>WebObjects 5.4's version of JDBCAdaptor will use this in order to
	 * assemble the name of the prototype to use when it loads models.</p>
	 * @return Name of the plugin.
	 */
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
	public EOSynchronizationFactory createSynchronizationFactory() {
		return new com.webobjects.jdbcadaptor._MySQLPlugIn.MySQLSynchronizationFactory(_adaptor);
	}

	@Override
	public NSDictionary/*<String, Object>*/ jdbcInfo() {

		NSDictionary/*<String, Object>*/ jdbcInfo;
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
				jdbcInfo = (NSDictionary/*<String, Object>*/) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
			} catch (IOException e) {
				throw new RuntimeException("Failed to load 'JDBCInfo.plist' from this plugin jar.", e);
			}

	    } else {

			NSMutableDictionary/*<String, Object>*/ mutableInfo = super.jdbcInfo().mutableClone();
			NSMutableDictionary/*<String, NSDictionary>*/ typeInfo = ((NSDictionary/*<String, NSDictionary>*/)mutableInfo.objectForKey("typeInfo")).mutableClone();
			NSDictionary textTypeInfo = (NSDictionary) typeInfo.objectForKey("TEXT");
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
			writeJDBCInfo(mutableInfo);
			
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
		try {
			InputStream stream = blob.getBinaryStream();
			int chunkSize = (int)blob.length();
			if(chunkSize == 0) {
				data = NSData.EmptyData;
			} else {
				data = new NSData(stream, chunkSize);
			}
			stream.close();
		} catch(IOException e) {
			throw new JDBCAdaptorException(e.getMessage(), null);
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

	private void writeJDBCInfo(NSDictionary/*<String, Object>*/ jdbcInfo) {
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
