package com.webobjects.jdbcadaptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaSynchronization;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVariable;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSUtilities;

/**
 * <span class="en">
 * This is the wo5 java runtime plugin for FrontBase.
 * </span>
 * 
 * <span class="ja">
 * FrontBase の WO5 Java ランタイム・プラグイン
 * </span>
 *
 * @author Cail Borrell
 */
public class _FrontBasePlugIn extends JDBCPlugIn {
	
	private static final String QUERY_STRING_USE_BUNDLED_JDBC_INFO = "useBundledJdbcInfo";

	static final boolean USE_NAMED_CONSTRAINTS = true;

	static final String _frontbaseIncludeSynonyms = System.getProperty("jdbcadaptor.frontbase.includeSynonyms", null);
	static final String _frontbaseWildcardPatternForAttributes = System.getProperty("jdbcadaptor.frontbase.wildcardPatternForAttributes", null);
	static final String _frontbaseWildcardPatternForTables = System.getProperty("jdbcadaptor.frontbase.wildcardPatternForTables", "%");
	static final String _frontbaseWildcardPatternForSchema = System.getProperty("jdbcadaptor.frontbase.wildcardPatternForSchema", null);
	static final String _frontbaseSqlStatementForGettingProcedureNames = System.getProperty("jdbcadaptor.frontbase.sqlStatementForGettingProcedureNames", null);
	static final String _frontbaseStoredProcedureCatalogPattern = System.getProperty("jdbcadaptor.frontbase.storedProcedureCatalogPattern", null);
	static final String _frontbaseStoredProcedureSchemaPattern = System.getProperty("jdbcadaptor.frontbase.storedProcedureSchemaPattern", null);
	static final String _frontbaseSqlStatementForGettingTableNames = System.getProperty("jdbcadaptor.frontbase.sqlStatementForGettingTableNames", null);
	static final String _frontbaseContainsOperatorFix = System.getProperty("jdbcadaptor.frontbase.frontbaseContainsOperatorFix", null);

	/**
	 * Formatter to use when handling date columns. Each thread has its own copy.
	 */
	private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	/**
	 * Formatter to use when handling timestamp columns. Each thread has its own copy.
	 */
	private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		}
	};

	/**
	 * Formatter to use when handling time only columns. Each thread has its own copy.
	 */
	private static final ThreadLocal<SimpleDateFormat> TIME_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("HH:mm:ss.SSS");
		}
	};

	public _FrontBasePlugIn(JDBCAdaptor jdbcadaptor) {
		super(jdbcadaptor);
	}

	public static String getPlugInVersion() {
		return "2.6.4";
	}

	@Override
	public boolean canDescribeStoredProcedure(String s) {
		return true;
	}

	@Override
	public EOSynchronizationFactory createSynchronizationFactory() {
		return new _FrontBasePlugIn.FrontbaseSynchronizationFactory(_adaptor);
	}

	@Override
	public String defaultDriverName() {
		return "com.frontbase.jdbc.FBJDriver";
	}

	@Override
	public String databaseProductName() {
		return "FrontBase";
	}

	/**
	 * <P>
	 * WebObjects 5.4's version of JDBCAdaptor will use this in order to assemble the name of the prototype to use when
	 * it loads models.
	 * </P>
	 *
	 * @return the name of the plugin.
	 */
	@Override
	public String name() {
		return "FrontBase";
	}

	/**
	 * <P>
	 * This method returns true if the connection URL for the database has a special flag on it which indicates to the
	 * system that the jdbcInfo which has been bundled into the plugin is acceptable to use in place of actually going
	 * to the database and getting it.
	 *
	 * @return <code>true</code> jdbcInfo which has been bundled into the plugin is acceptable to use
	 */
	protected boolean shouldUseBundledJdbcInfo() {
		boolean shouldUseBundledJdbcInfo = false;
		String url = connectionURL();
		if (url != null) {
			shouldUseBundledJdbcInfo = url.toLowerCase().matches(".*/" + _FrontBasePlugIn.QUERY_STRING_USE_BUNDLED_JDBC_INFO.toLowerCase() + "=(true|yes)(\\/|$)");
		}
		return shouldUseBundledJdbcInfo;
	}

	@Override
	public Class defaultExpressionClass() {
		return FrontbaseExpression.class;
	}

	@Override
	public String wildcardPatternForSchema() {
		if (_frontbaseWildcardPatternForSchema != null)
			return _frontbaseWildcardPatternForSchema;
		else {
			String schema = (String) adaptor().connectionDictionary().objectForKey("schema");
			return (schema != null) ? schema.toUpperCase() : "CURRENT_SCHEMA";
		}
	}

	@Override
	public String schemaNameForEntity(EOEntity eoentity) {
		String s = super.schemaNameForEntity(eoentity);

		if (s == null) {
			s = (String) adaptor().connectionDictionary().objectForKey("schema");
			return (s != null) ? s.toUpperCase() : "CURRENT_SCHEMA";
		}
		else
			return s;
	}

	@Override
	public String storedProcedureSchemaPattern() {
		if (_frontbaseStoredProcedureSchemaPattern != null)
			return _frontbaseStoredProcedureSchemaPattern;
		else
			return "CURRENT_SCHEMA";
	}

	@Override
	public Properties connectionPropertiesForConnectionDictionary(NSDictionary connectionDictionary) {
		Properties properties = super.connectionPropertiesForConnectionDictionary(connectionDictionary);

		// Check for dbPasswd in connection Dictionary
		Object temp = connectionDictionary.objectForKey("dbpasswd");
		if (temp != null) {
			properties.put("dbpasswd", temp);
		}
		// Check for session in connection Dictionary
		temp = connectionDictionary.objectForKey("session");
		if (temp != null) {
			properties.put("session", temp);
		}
		// Check for session in connection Dictionary
		temp = connectionDictionary.objectForKey("system");
		if (temp != null) {
			properties.put("system", temp);
		}
		// Check for session in connection Dictionary
		temp = connectionDictionary.objectForKey("isolation");
		if (temp != null) {
			properties.put("isolation", temp);
		}
		// Check for session in connection Dictionary
		temp = connectionDictionary.objectForKey("locking");
		if (temp != null) {
			properties.put("locking", temp);
		}
		// Check for session in connection Dictionary
		temp = connectionDictionary.objectForKey("readOnly");
		if (temp != null) {
			properties.put("readOnly", temp);
		}
		return properties;
	}

	/**
	 * <P>
	 * This is usually extracted from the the database using JDBC, but this is really inconvenient for users who are
	 * trying to generate SQL at some. A specific version of the data has been written into the property list of the
	 * framework and this can be used as a hard-coded equivalent.
	 * </P>
	 */
	@Override
	public NSDictionary<String, Object> jdbcInfo() {
		// you can swap this code out to write the property list out in order // to get a fresh copy of the
		// JDBCInfo.plist
//		try {
//			String jdbcInfoS = NSPropertyListSerialization.stringFromPropertyList(super.jdbcInfo());
//			FileOutputStream fos = new FileOutputStream("/tmp/JDBCInfo.plist");
//			fos.write(jdbcInfoS.getBytes());
//			fos.close();
//		}
//		catch (Exception e) {
//			throw new IllegalStateException("problem writing JDBCInfo.plist", e);
//		}

		boolean shouldUseBundledJdbcInfo = shouldUseBundledJdbcInfo();
		NSDictionary<String, Object> jdbcInfo;
		// have a look at the JDBC connection URL to see if the flag has been set to
		// specify that the hard-coded jdbcInfo information should be used.
		if (shouldUseBundledJdbcInfo) {
			if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed)) {
				NSLog.debug.appendln("Loading jdbcInfo from JDBCInfo.plist as opposed to using the JDBCPlugIn default implementation.");
			}

			// MS: Note that the name is not just /JDBCInfo.plist like it used to be.  Because we're loading
			// resources from the classpath instead of the Resources folder (so that it loads properly in EM),
			// if you have two plugins loaded, their resource names will overlap.
			InputStream jdbcInfoStream = getClass().getResourceAsStream("/FrontBaseJDBCInfo.plist");
			if (jdbcInfoStream == null) {
				throw new IllegalStateException("Unable to find 'FrontBaseJDBCInfo.plist' in this plugin jar.");
			}

			try {
				jdbcInfo = (NSDictionary<String, Object>) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to load 'FrontBaseJDBCInfo.plist' from this plugin jar.", e);
			} finally {
				try { jdbcInfoStream.close(); } catch (IOException e) {}
			}
		}
		else {
			jdbcInfo = super.jdbcInfo();
		}

		NSMutableDictionary<String, Object> mutableJdbcInfo = new NSMutableDictionary<String, Object>(jdbcInfo);
		NSMutableDictionary typeInfoDict = new NSMutableDictionary((NSDictionary) mutableJdbcInfo.objectForKey("typeInfo"));
		NSDictionary typeDict = (NSDictionary) typeInfoDict.objectForKey("CHARACTER");
		typeInfoDict.setObjectForKey(typeDict, "CHAR");
		typeDict = (NSDictionary) typeInfoDict.objectForKey("CHARACTER VARYING");
		typeInfoDict.setObjectForKey(typeDict, "VARCHAR");
		typeInfoDict.setObjectForKey(typeDict, "CHAR VARYING");
		typeDict = (NSDictionary) typeInfoDict.objectForKey("BIT");
		typeInfoDict.setObjectForKey(typeDict, "BYTE");
		typeDict = (NSDictionary) typeInfoDict.objectForKey("BIT VARYING");
		typeInfoDict.setObjectForKey(typeDict, "BYTE VARYING");

		mutableJdbcInfo.setObjectForKey(typeInfoDict, "typeInfo");
		if (!shouldUseBundledJdbcInfo) {
			JDBCContext jdbccontext = adaptor()._cachedAdaptorContext();
			try {
				jdbccontext.connection().commit();
			}
			catch (SQLException sqlexception) {
				if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 0x000010000L))
					NSLog.debug.appendln(sqlexception);
			}
		}
		return mutableJdbcInfo;
	}

	EOQualifier primaryKeyQualifier(EOQualifier eoqualifier, EOEntity eoentity) {
		if (eoqualifier instanceof EOAndQualifier) {
			NSArray<EOQualifier> qualifiers = ((EOAndQualifier) eoqualifier).qualifiers();
			NSArray<String> attributeNames = eoentity.primaryKeyAttributeNames();
			NSMutableArray nsmutablearray = new NSMutableArray();

			for (int i = 0; i < qualifiers.count(); i++) {
				EOQualifier eoqualifier1 = qualifiers.objectAtIndex(i);

				if (eoqualifier1 instanceof EOKeyValueQualifier) {
					EOKeyValueQualifier eokeyvaluequalifier = (EOKeyValueQualifier) eoqualifier1;

					if (attributeNames.containsObject(eokeyvaluequalifier.key()))
						nsmutablearray.addObject(eokeyvaluequalifier);
				}
			}

			if (nsmutablearray.count() == 1)
				return (EOQualifier) nsmutablearray.objectAtIndex(0);
			else
				return new EOAndQualifier(qualifiers);
		}
		else {
			return eoqualifier;
		}
	}

	@Override
	public void updateLOBs(JDBCChannel channel, JDBCExpression expression, NSDictionary<String, Object> row, EOEntity entity) {
		FrontbaseExpression frontbaseexpression = (FrontbaseExpression) expression;
		if (!frontbaseexpression.hasLOBsToUpdate())
			return;

		NSArray array = frontbaseexpression.lobList();

		try {
			Connection con = ((JDBCContext) channel.adaptorContext()).connection();

			NSMutableDictionary<String, Object> d = new NSMutableDictionary<String, Object>();

			for (int i = 0; i < array.count(); i += 2) {
				d.setObjectForKey(getLobHandle(con, array.objectAtIndex(i), array.objectAtIndex(i + 1)), ((EOAttribute) array.objectAtIndex(i)).name());
			}

			EOQualifier qualifier = frontbaseexpression.qualifier();
			if (qualifier == null)
				qualifier = entity.qualifierForPrimaryKey(row);
			else
				qualifier = primaryKeyQualifier(qualifier, entity);

			frontbaseexpression.resetlobList();
			channel.updateValuesInRowsDescribedByQualifier(d, qualifier, entity);
		}
		catch (SQLException e) {
			System.err.print(e.getMessage());
		}
	}

	// When using BLOB as an external type NSData is expected as the internal type.
	// When using CLOB as an external type String is expected as the internal type.
	String getLobHandle(Connection con, Object attribute, Object value) throws SQLException {
		// MS: This is weird, but to allow for people to build FrontBasePlugIn without actually
		// having the FrontBase JDBC driver installed, I've switched these two calls to be reflection.
		try {
			switch (FrontBaseTypes.internalTypeForExternal(((EOAttribute) attribute).externalType())) {
			case FrontBaseTypes.FB_BLOB:
				Method writeBLOBBytes = con.getClass().getMethod("writeBLOB", new Class[] { byte[].class });
				return (String) writeBLOBBytes.invoke(con, new Object[] { ((NSData) value).bytes() });
			case FrontBaseTypes.FB_CLOB:
				Method writeCLOBString = con.getClass().getMethod("writeCLOB", new Class[] { String.class });
				return (String) writeCLOBString.invoke(con, new Object[] { (String) value });
			default:
				return "NULL";
			}
		}
		catch (Throwable e) {
			if (e instanceof SQLException) {
				throw (SQLException) e;
			}
			throw new RuntimeException("Failed to get LOB handle.", e);
		}
	}

	@Override
	public Object fetchBLOB(ResultSet resultset, int i, EOAttribute attribute, boolean flag) throws SQLException {
		Blob blob = resultset.getBlob(i);
		if (blob == null)
			return null;
		if (!flag)
			return blob;
		else {
			try {
				byte[] bytes = blob.getBytes(1, (int) blob.length());
				return new NSData(bytes, new NSRange(0, bytes.length), true);
			}
			catch (Exception ioexception) {
				throw new JDBCAdaptorException(ioexception.getMessage(), null);
			}
		}
	}

	@Override
	public Object fetchCLOB(ResultSet resultset, int i, EOAttribute attribute, boolean flag) throws SQLException {
		Clob clob = resultset.getClob(i);
		if (clob == null)
			return null;
		if (!flag)
			return clob;
		else
			return clob.getSubString(1L, (int) clob.length());
	}

	@Override
	public NSArray<NSDictionary<String, Object>> newPrimaryKeys(int numberOfKeys, EOEntity eoentity, JDBCChannel jdbcchannel) {
		NSMutableArray<NSDictionary<String, Object>> pkDicts = new NSMutableArray<NSDictionary<String, Object>>();

		boolean pksGenerated = true;
		int numberOfKeysLeft = numberOfKeys;
		int keyBatchSize = 10;
		while (pksGenerated && numberOfKeysLeft > 0) {
			int thisKeyBatchSize = Math.min(keyBatchSize, numberOfKeysLeft);
			pksGenerated = _newPrimaryKeys(thisKeyBatchSize, eoentity, jdbcchannel, pkDicts);
			numberOfKeysLeft -= thisKeyBatchSize;
		}

		if (!pksGenerated) {
			pkDicts = null;
		}

		return pkDicts;
	}

	private boolean _newPrimaryKeys(int keyBatchSize, EOEntity eoentity, JDBCChannel jdbcchannel, NSMutableArray<NSDictionary<String, Object>> pkDicts) {
		if (keyBatchSize == 0) {
			return true;
		}

		NSArray<EOAttribute> primaryKeyAttributes = eoentity.primaryKeyAttributes();
		if (primaryKeyAttributes == null) {
			return false;
		}

		EOAttribute firstPrimaryKeyAttribute = primaryKeyAttributes.lastObject();
		boolean isNSData = firstPrimaryKeyAttribute.className().endsWith("NSData");

		NSMutableArray<EOAttribute> attributesToFetch = new NSMutableArray<EOAttribute>();
		StringBuilder sql = new StringBuilder();
		sql.append("VALUES (");
		for (int keyNum = 0; keyNum < keyBatchSize; keyNum++) {
			if (isNSData) {
				if (firstPrimaryKeyAttribute.externalType().startsWith("BIT")) {
					sql.append("NEW_UID(" + (firstPrimaryKeyAttribute.width() >> 3) + ")");
				}
				else {
					sql.append("NEW_UID(" + firstPrimaryKeyAttribute.width() + ")");
				}
			}
			else {
				sql.append("SELECT UNIQUE FROM " + quoteTableName(eoentity.primaryKeyRootName()));
			}
			if (keyNum < keyBatchSize - 1) {
				sql.append(", ");
			}

			EOAttribute generatedPrimaryKeyAttribute = new EOAttribute();
			generatedPrimaryKeyAttribute.setName("Unique" + keyNum);
			generatedPrimaryKeyAttribute.setColumnName(firstPrimaryKeyAttribute.columnName());
			generatedPrimaryKeyAttribute.setExternalType(firstPrimaryKeyAttribute.externalType());
			generatedPrimaryKeyAttribute.setClassName(firstPrimaryKeyAttribute.className());
			generatedPrimaryKeyAttribute.setValueType(firstPrimaryKeyAttribute.valueType());
			generatedPrimaryKeyAttribute.setPrecision(firstPrimaryKeyAttribute.precision());
			generatedPrimaryKeyAttribute.setScale(firstPrimaryKeyAttribute.scale());
			generatedPrimaryKeyAttribute.setWidth(firstPrimaryKeyAttribute.width());
			generatedPrimaryKeyAttribute.setAllowsNull(firstPrimaryKeyAttribute.allowsNull());
			adaptor().plugIn().assignTypeForAttribute(generatedPrimaryKeyAttribute);
			attributesToFetch.addObject(generatedPrimaryKeyAttribute);
		}

		sql.append(')');

		boolean pksGenerated = false;
		EOSQLExpression eosqlexpression = expressionFactory().expressionForString(sql.toString());
		EOAdaptorContext adaptorContext = jdbcchannel.adaptorContext();
		adaptorContext.transactionDidBegin();
		jdbcchannel.evaluateExpression(eosqlexpression);
		if (jdbcchannel._errorEvaluateExpression()) {
			adaptorContext.transactionDidRollback();
			jdbcchannel._setErrorEvaluateExpression(false);
		}
		else {
			jdbcchannel.setAttributesToFetch(attributesToFetch);
			NSMutableDictionary<String, Object> row = jdbcchannel.fetchRow();
			jdbcchannel.cancelFetch();
			adaptorContext.transactionDidCommit();
			if (row != null && row.count() > 0) {
				NSArray pkValues = row.allValues();
				if (pkValues.count() == keyBatchSize) {
					Enumeration keysEnum = pkValues.objectEnumerator();
					while (keysEnum.hasMoreElements()) {
						Object obj = keysEnum.nextElement();
						NSMutableDictionary pkDict = new NSMutableDictionary();
						Enumeration pkAttributeEnum = primaryKeyAttributes.objectEnumerator();
						while (pkAttributeEnum.hasMoreElements()) {
							EOAttribute pkAttribute = (EOAttribute) pkAttributeEnum.nextElement();
							pkDict.setObjectForKey(obj, pkAttribute.name());
						}
						pkDicts.addObject(pkDict);
					}
					pksGenerated = true;
				}
			}
		}

		return pksGenerated;
	}

	@Deprecated
	protected static final int FB_Boolean = 1;
	@Deprecated
	protected static final int FB_Integer = 2;
	@Deprecated
	protected static final int FB_SmallInteger = 3;
	@Deprecated
	protected static final int FB_Float = 4;
	@Deprecated
	protected static final int FB_Real = 5;
	@Deprecated
	protected static final int FB_Double = 6;
	@Deprecated
	protected static final int FB_Numeric = 7;
	@Deprecated
	protected static final int FB_Decimal = 8;
	@Deprecated
	protected static final int FB_Character = 9;
	@Deprecated
	protected static final int FB_VCharacter = 10;
	@Deprecated
	protected static final int FB_Bit = 11;
	@Deprecated
	protected static final int FB_VBit = 12;
	@Deprecated
	protected static final int FB_Date = 13;
	@Deprecated
	protected static final int FB_Time = 14;
	@Deprecated
	protected static final int FB_TimeTZ = 15;
	@Deprecated
	protected static final int FB_Timestamp = 16;
	@Deprecated
	protected static final int FB_TimestampTZ = 17;
	@Deprecated
	protected static final int FB_YearMonth = 18;
	@Deprecated
	protected static final int FB_DayTime = 19;
	@Deprecated
	protected static final int FB_CLOB = 20;
	@Deprecated
	protected static final int FB_BLOB = 21;
	@Deprecated
	protected static final int FB_TinyInteger = 22;
	@Deprecated
	protected static final int FB_LongInteger = 23;

	protected static String notNullConstraintName(EOAttribute attribute) {
		return notNullConstraintName(attribute.entity().externalName(), attribute.columnName());
	}

	protected static String notNullConstraintName(String tableName, String columnName) {
		StringBuilder constraintBuffer = new StringBuilder();
		constraintBuffer.append("NOT_NULL_");
		constraintBuffer.append(tableName);
		constraintBuffer.append("__");
		constraintBuffer.append(columnName);
		return constraintBuffer.toString();
	}

	protected static String quoteTableName(String s) {
		if (s == null)
			return null;
		int i = s.lastIndexOf(46);
		if (i == -1)
			return "\"" + s + "\"";
		else
			return "\"" + s.substring(0, i) + "\".\"" + s.substring(i + 1, s.length()) + "\"";
	}

	/**
	 * @deprecated user {@link FrontBaseTypes#internalTypeForExternal(String)} instead
	 */
	@Deprecated
	protected static int internalTypeForExternal(String externalType) {
		String upperExternalType = externalType.toUpperCase();
		if (upperExternalType.equals("BOOLEAN"))
			return FB_Boolean;
		else if (upperExternalType.equals("INTEGER") || upperExternalType.equals("INT"))
			return FB_Integer;
		else if (upperExternalType.equals("SMALLINT"))
			return FB_SmallInteger;
		else if (upperExternalType.equals("LONGINT"))
			return FB_LongInteger;
		else if (upperExternalType.equals("TINYINT"))
			return FB_TinyInteger;
		else if (upperExternalType.equals("FLOAT"))
			return FB_Float;
		else if (upperExternalType.equals("REAL"))
			return FB_Real;
		else if (upperExternalType.equals("DOUBLE PRECISION"))
			return FB_Double;
		else if (upperExternalType.equals("NUMERIC"))
			return FB_Numeric;
		else if (upperExternalType.equals("DECIMAL"))
			return FB_Decimal;
		else if (upperExternalType.equals("CHAR") || upperExternalType.equals("CHARACTER"))
			return FB_Character;
		else if (upperExternalType.equals("VARCHAR") || upperExternalType.equals("CHARACTER VARYING") || upperExternalType.equals("CHAR VARYING"))
			return FB_VCharacter;
		else if (upperExternalType.equals("BIT") || upperExternalType.equals("BYTE"))
			return FB_Bit;
		else if (upperExternalType.equals("BIT VARYING") || upperExternalType.equals("BYTE VARYING"))
			return FB_VBit;
		else if (upperExternalType.equals("DATE"))
			return FB_Date;
		else if (upperExternalType.equals("TIME"))
			return FB_Time;
		else if (upperExternalType.equals("TIME WITH TIME ZONE"))
			return FB_TimeTZ;
		else if (upperExternalType.equals("TIMESTAMP"))
			return FB_Timestamp;
		else if (upperExternalType.equals("TIMESTAMP WITH TIME ZONE"))
			return FB_TimestampTZ;
		else if (upperExternalType.equals("BLOB"))
			return FB_BLOB;
		else if (upperExternalType.equals("CLOB"))
			return FB_CLOB;
		else
			return -1;
	}

	public static class FrontbaseSynchronizationFactory extends EOSynchronizationFactory {

		public FrontbaseSynchronizationFactory(EOAdaptor eoadaptor) {
			super(eoadaptor);
		}

		@Override
		public boolean supportsSchemaSynchronization() {
			return true;
		}

		public static boolean boolValueForKeyDefault(NSDictionary nsdictionary, String s, boolean flag) {
			String s1 = (String) nsdictionary.objectForKey(s);
			if (s1 == null)
				return flag;
			else
				return s1.equals("YES");
		}

		@Override
		public String schemaCreationScriptForEntities(NSArray<EOEntity> allEntities, NSDictionary<String, String> options) {
			StringBuffer result = new StringBuffer();
			if (options == null) {
				options = NSDictionary.emptyDictionary();
			}
			NSArray<EOSQLExpression> statements = schemaCreationStatementsForEntities(allEntities, options);
			int i = 0;
			for (int count = statements.count(); i < count; i++) {
				appendExpressionToScript(statements.objectAtIndex(i), result);
			}

			return result.toString();
		}

		/**
		 * <span class="ja">
		 * Eclipse の EntityModeler でエンティティを作成時に使用されるメソッド。
		 * SQL 生成をクリックするとここで呼び出される
		 * </span>
		 */
		@Override
		public NSArray<EOSQLExpression> schemaCreationStatementsForEntities(NSArray<EOEntity> entities, NSDictionary<String, String> options) {
			NSMutableArray<EOSQLExpression> result = new NSMutableArray<EOSQLExpression>();

			if (entities == null || entities.count() == 0)
				return result;

			// データベース・ストラクチャに変更する時にはこの行を実行しないとエラーになる可能性があります。
			result.addObject(_expressionForString("-- SQL creation time : " + new NSTimestamp().toString()));
			result.addObject(_expressionForString("-- PlugIn version : " + getPlugInVersion()));
			result.addObject(_expressionForString("-- To change any Structure Information this Command is must have"));
			result.addObject(_expressionForString("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE, LOCKING PESSIMISTIC"));

			NSDictionary<String, Object> connectionDict = entities.lastObject().model().connectionDictionary();
			if (boolValueForKeyDefault(options, "dropDatabase", false)) {
				result.addObjectsFromArray(dropDatabaseStatementsForConnectionDictionary(connectionDict, null));
			}
			if (boolValueForKeyDefault(options, "createDatabase", false)) {
				result.addObjectsFromArray(createDatabaseStatementsForConnectionDictionary(connectionDict, null));
			}
			if (boolValueForKeyDefault(options, "dropPrimaryKeySupport", true)) {
				NSArray<NSArray<EOEntity>> entityGroups = primaryKeyEntityGroupsForEntities(entities);
				result.addObjectsFromArray(dropPrimaryKeySupportStatementsForEntityGroups(entityGroups));
			}
			if (boolValueForKeyDefault(options, "dropTables", true)) {
				NSArray<NSArray<EOEntity>> entityGroups = tableEntityGroupsForEntities(entities);
				result.addObjectsFromArray(dropTableStatementsForEntityGroups(entityGroups));
			}
			if (boolValueForKeyDefault(options, "createTables", true)) {
				NSArray<NSArray<EOEntity>> entityGroups = tableEntityGroupsForEntities(entities);
				result.addObjectsFromArray(createTableStatementsForEntityGroups(entityGroups));
				result.addObjectsFromArray(createIndexStatementsForEntityGroups(entityGroups));
			}
			if (boolValueForKeyDefault(options, "createPrimaryKeySupport", true)) {
				NSArray<NSArray<EOEntity>> entityGroups = primaryKeyEntityGroupsForEntities(entities);
				result.addObjectsFromArray(primaryKeySupportStatementsForEntityGroups(entityGroups));
			}
			if (boolValueForKeyDefault(options, "primaryKeyConstraints", true)) {
				NSArray<NSArray<EOEntity>> entityGroups = tableEntityGroupsForEntities(entities);
				result.addObjectsFromArray(primaryKeyConstraintStatementsForEntityGroups(entityGroups));
			}
			if (boolValueForKeyDefault(options, "foreignKeyConstraints", false)) {
				NSArray<NSArray<EOEntity>> entityGroups = tableEntityGroupsForEntities(entities);
				for (NSArray<EOEntity> entityGroup : entityGroups) {
					result.addObjectsFromArray(_foreignKeyConstraintStatementsForEntityGroup(entityGroup));
				}
			}
			result.addObject(_expressionForString("COMMIT"));
			return result;
		}

		@Override
		public NSArray<EOSQLExpression> dropPrimaryKeySupportStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			return new NSArray<EOSQLExpression>(_expressionForString("-- The 'Drop Primary Key Support' option is unavailable."));
		}

		@Override
		public NSArray<EOSQLExpression> dropDatabaseStatementsForConnectionDictionary(NSDictionary<String, Object> connectionDictionary, NSDictionary<String, Object> administrativeConnectionDictionary) {
			return new NSArray<EOSQLExpression>(_expressionForString("-- The 'Drop Database' option is unavailable."));
		}

		@Override
		public NSArray<EOSQLExpression> createDatabaseStatementsForConnectionDictionary(NSDictionary<String, Object> connectionDictionary, NSDictionary<String, Object> administrativeConnectionDictionary) {
			return new NSArray<EOSQLExpression>(_expressionForString("-- The 'Create Database' option is unavailable."));
		}

		@Override
		public NSArray<EOSQLExpression> dropTableStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			NSLog.debug.appendln("In dropTableStatementsForEntityGroups");
			return super.dropTableStatementsForEntityGroups(entityGroups);
		}

		@Override
		public NSArray<EOSQLExpression> dropTableStatementsForEntityGroup(NSArray<EOEntity> entityGroup) {
			NSLog.debug.appendln("In dropTableStatementsForEntityGroup (no s)");
			EOEntity entity = entityGroup.objectAtIndex(0);
			String dropType = " CASCADE";

			if (entity.userInfo() != null) {
				NSDictionary dictionary = entity.userInfo();
				if (dictionary.valueForKey("Restrict") != null && ((String) dictionary.valueForKey("Restrict")).equals("true"))
					dropType = " RESTRICT";
			}

			EOSQLExpression expression = _expressionForString("DROP TABLE " + quoteTableName(entity.externalName()) + dropType);

			return new NSArray<EOSQLExpression>(expression);
		}

		@Override
		public NSArray<EOSQLExpression> primaryKeySupportStatementsForEntityGroup(NSArray<EOEntity> entityGroup) {
			if (entityGroup == null)
				return NSArray.emptyArray();

			NSMutableArray<EOSQLExpression> result = new NSMutableArray<EOSQLExpression>();

			for (int i = entityGroup.count() - 1; i >= 0; i--) {
				EOEntity eoentity = entityGroup.objectAtIndex(i);
				String externalName = eoentity.externalName();
				NSArray<EOAttribute> priKeyAttributes = eoentity.primaryKeyAttributes();

				if (priKeyAttributes.count() == 1 && externalName != null && externalName.length() > 0) {
					EOAttribute priKeyAttribute = priKeyAttributes.objectAtIndex(0);
					
					// pk counter not needed for non number primary key
					if (priKeyAttribute.adaptorValueType() != EOAttribute.AdaptorNumberType) {
						continue;
					}

					String unique = null;
					if (eoentity.model() != null) {
						unique = System.getProperty("com.frontbase.unique." + eoentity.model().name() + "." + eoentity.name());
						if (unique == null) {
							unique = System.getProperty("com.frontbase.unique." + eoentity.model().name());
						}
					}
					if (unique == null) {
						unique = System.getProperty("com.frontbase.unique");
					}
					if (unique == null) {
						unique = "1000000";
					}
					result.addObject(_expressionForString("SET UNIQUE = " + unique + " FOR " + quoteTableName(externalName)));
					result.addObject(_expressionForString("ALTER TABLE " + quoteTableName(externalName) + " ALTER "
							+ quoteTableName(priKeyAttribute.name()) + " SET DEFAULT UNIQUE"));
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

				fkSql.append(") DEFERRABLE INITIALLY DEFERRED");

				if (USE_NAMED_CONSTRAINTS)
					sql.append(constraint);
				sql.append(fkSql);

				return new NSArray<EOSQLExpression>(_expressionForString(sql.toString()));
			}
			return NSArray.emptyArray();
		}

		/** 
		 * <span class="ja">複数のエンティティ・グループス作成 SQL を生成します。</span> 
		 */
		@Override
		public NSArray<EOSQLExpression> createTableStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			NSMutableArray<EOSQLExpression> nsmutablearray = new NSMutableArray<EOSQLExpression>();

			for (int i = 0; i < entityGroups.count(); i++) {
				nsmutablearray.addObjectsFromArray(createTableStatementsForEntityGroup(entityGroups.objectAtIndex(i)));
			}

			return nsmutablearray;
		}

		/** 
		 * <span class="ja">エンティティ・グループの SQL を生成します</span>
		 */
		@Override
		public NSArray<EOSQLExpression> createTableStatementsForEntityGroup(NSArray<EOEntity> entityGroup) {
			EOSQLExpression eosqlexpression = null;
			EOEntity eoentity = null;
			NSMutableArray<String> nsmutablearray = new NSMutableArray<String>();
			int j = entityGroup != null ? entityGroup.count() : 0;

			if (j == 0)
				return NSArray.emptyArray();

			// 出力バッファーを準備
			StringBuilder columns = new StringBuilder();
			
			// エンティティの出力開始
			eosqlexpression = _expressionForEntity(entityGroup.objectAtIndex(0));

			// 各エンティティをループで回す
			for (int i = 0; i < j; i++) {
				eoentity = entityGroup.objectAtIndex(i);
				NSArray nsarray1 = eoentity.attributes();
				int l = nsarray1 != null ? nsarray1.count() : 0;

				for (int k = 0; k < l; k++) {
					EOAttribute eoattribute = (EOAttribute) nsarray1.objectAtIndex(k);
					String column = eoattribute.columnName();

					if (!eoattribute.isDerived() && !eoattribute.isFlattened() && column != null && column.length() > 0 && nsmutablearray.indexOfObject(column) == NSArray.NotFound) {

						if (columns.length() > 0) {
							columns.append(',');
							columns.append('\n');
							columns.append('\t');
						}

						columns.append(addCreateClauseForAttribute(eoattribute));
						nsmutablearray.addObject(column);
					}
				}
			}

			StringBuilder sql = new StringBuilder();
			sql.append("CREATE TABLE ");
			sql.append(quoteTableName(eoentity.externalName()));
			sql.append(" (\n\t");
			sql.append(columns.toString());
			sql.append("\n)");

			eosqlexpression.setStatement(sql.toString());

			return new NSArray<EOSQLExpression>(eosqlexpression);
		}

		@Override
		public NSArray<EOSQLExpression> createIndexStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			NSMutableArray<EOSQLExpression> statements = new NSMutableArray<EOSQLExpression>();

			for (int i = 0; i < entityGroups.count(); i++) {
				statements.addObjectsFromArray(createIndexStatementsForEntityGroup(entityGroups.objectAtIndex(i)));
			}

			return statements;
		}

		@Override
		public NSArray<EOSQLExpression> createIndexStatementsForEntityGroup(NSArray<EOEntity> entityGroup) {
			NSMutableArray<EOSQLExpression> result = new NSMutableArray<EOSQLExpression>();
			EOSQLExpression eosqlexpression = null;
			EOEntity eoentity = null;
			int j = entityGroup != null ? entityGroup.count() : 0;

			if (j == 0)
				return NSArray.emptyArray();

			eosqlexpression = _expressionForEntity(entityGroup.objectAtIndex(0));

			for (int i = 0; i < j; i++) {
				eoentity = entityGroup.objectAtIndex(i);
				NSDictionary dictionary = eoentity.userInfo();

				if (dictionary != null && dictionary.valueForKey("Index") != null) {
					dictionary = (NSDictionary) dictionary.valueForKey("Index");
					java.util.Enumeration e = dictionary.keyEnumerator();

					while (e.hasMoreElements()) {
						eosqlexpression.setStatement((String) dictionary.objectForKey(e.nextElement()));
						result.addObject(eosqlexpression);
					}
				}
			}
			return result;
		}

		/** 
		 * <span class="ja">1つのアトリビュートの SQL を生成します </span>
		 */
		public StringBuilder addCreateClauseForAttribute(EOAttribute eoattribute) {
			EOSQLExpression expression = _expressionForEntity(eoattribute.entity());
			expression.addCreateClauseForAttribute(eoattribute);
			return new StringBuilder(expression.listString());
		}

		public String columnTypeStringForAttribute(EOAttribute eoattribute) {
			EOSQLExpression expression = _expressionForEntity(eoattribute.entity());
			return expression.columnTypeStringForAttribute(eoattribute);
		}

		@Override
		public NSArray<EOSQLExpression> statementsToConvertColumnType(String columnName, String tableName, ColumnTypes oldType, ColumnTypes newType, NSDictionary<String, String> options) {
			String columnTypeString = statementToCreateDataTypeClause(newType);
			NSArray<EOSQLExpression> statements = new NSArray<EOSQLExpression>(_expressionForString("alter column " + quoteTableName(tableName) + "." + quoteTableName(columnName) + " to " + columnTypeString));
			return statements;
		}

		@Override
		public NSArray<EOSQLExpression> statementsToModifyColumnNullRule(String columnName, String tableName, boolean allowsNull, NSDictionary<String, String> options) {
			NSArray<EOSQLExpression> statements;
			if (allowsNull) {
				if (USE_NAMED_CONSTRAINTS) {
					statements = new NSArray<EOSQLExpression>(_expressionForString("alter table " + quoteTableName(tableName) + " drop constraint " + quoteTableName(_FrontBasePlugIn.notNullConstraintName(tableName, columnName)) + " cascade"));
				}
				else {
					statements = null;
				}
			}
			else {
				if (USE_NAMED_CONSTRAINTS) {
					statements = new NSArray<EOSQLExpression>(_expressionForString("alter table " + quoteTableName(tableName) + " add constraint " + quoteTableName(_FrontBasePlugIn.notNullConstraintName(tableName, columnName)) + " check (" + quoteTableName(columnName) + " is not null)"));
				}
				else {
					statements = new NSArray<EOSQLExpression>(_expressionForString("alter table " + quoteTableName(tableName) + " add check (" + quoteTableName(columnName) + " is not null)"));
				}
			}
			return statements;
		}

		@Override
		public NSArray<EOSQLExpression> statementsToDeleteColumnNamed(String columnName, String tableName, NSDictionary<String, String> options) {
			return new NSArray<EOSQLExpression>(_expressionForString("alter table " + quoteTableName(tableName) + " drop column \"" + columnName.toUpperCase() + "\" cascade"));
		}

		@Override
		public String _columnCreationClauseForAttribute(EOAttribute attribute) {
			return addCreateClauseForAttribute(attribute).toString();
		}

		@Override
		public NSArray<EOSQLExpression> statementsToInsertColumnForAttribute(EOAttribute attribute, NSDictionary<String, String> options) {
			String clause = _columnCreationClauseForAttribute(attribute);
			return new NSArray<EOSQLExpression>(_expressionForString("alter table " + quoteTableName(attribute.entity().externalName()) + " add " + clause));
		}

		private String statementToCreateDataTypeClause(EOSchemaSynchronization.ColumnTypes columntypes) {
			switch (FrontBaseTypes.internalTypeForExternal(columntypes.name())) {
			case FrontBaseTypes.FB_Decimal:
			case FrontBaseTypes.FB_Numeric:
				int j = columntypes.precision();
				if (j == 0)
					return columntypes.name();
				int k = columntypes.scale();
				if (k == 0)
					return columntypes.name() + "(" + j + ")";
				else
					return columntypes.name() + "(" + j + "," + k + ")";

			case FrontBaseTypes.FB_Float:
			case FrontBaseTypes.FB_Bit:
			case FrontBaseTypes.FB_VBit:
			case FrontBaseTypes.FB_Character:
			case FrontBaseTypes.FB_VCharacter:
				int l = columntypes.width();
				if (l == 0)
					l = columntypes.precision();
				if (l == 0)
					return columntypes.name();
				else
					return columntypes.name() + "(" + l + ")";
			case FrontBaseTypes.FB_Timestamp:
				int m = columntypes.precision();
				if (m == 0)
					return columntypes.name();
				else
					return columntypes.name() + "(" + m + ")";
			}
			return columntypes.name();
		}

		@Override
		public NSArray<EOSQLExpression> statementsToRenameColumnNamed(String columnName, String tableName, String newName, NSDictionary<String, String> options) {
			return new NSArray<EOSQLExpression>(_expressionForString("alter column name " + quoteTableName(tableName) + "." + quoteTableName(columnName) + " to " + quoteTableName(newName)));
		}

		@Override
		public NSArray<EOSQLExpression> statementsToRenameTableNamed(String tableName, String newName, NSDictionary<String, String> options) {
			return new NSArray<EOSQLExpression>(_expressionForString("alter table name " + quoteTableName(tableName) + " to " + quoteTableName(newName)));
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
		public NSArray<EOSQLExpression> primaryKeyConstraintStatementsForEntityGroups(NSArray<NSArray<EOEntity>> entityGroups) {
			NSMutableArray<EOSQLExpression> result = new NSMutableArray<EOSQLExpression>();

			for (int i = 0; i < entityGroups.count(); i++) {
				result.addObjectsFromArray(primaryKeyConstraintStatementsForEntityGroup(entityGroups.objectAtIndex(i)));
			}

			return result;
		}

		@Override
		public NSArray<EOSQLExpression> primaryKeyConstraintStatementsForEntityGroup(NSArray<EOEntity> entityGroup) {
			if (entityGroup.count() != 0) {
				EOEntity entity = entityGroup.objectAtIndex(0);
				String tableName = entity.externalName();
				NSArray<String> keys = entity.primaryKeyAttributeNames();
				StringBuilder sql = new StringBuilder();

				if (tableName != null && keys.count() > 0) {
					sql.append("ALTER TABLE ");
					sql.append(quoteTableName(tableName.toUpperCase()));
					sql.append(" ADD");

					StringBuilder constraint = new StringBuilder(" CONSTRAINT \"PRIMARY_KEY_");
					constraint.append(tableName);

					StringBuilder pkSql = new StringBuilder(" PRIMARY KEY (");

					for (int j = 0; j < keys.count(); j++) {
						constraint.append('_');
						if (j != 0)
							pkSql.append(',');

						pkSql.append("\"");
						String columnName = entity.attributeNamed(keys.objectAtIndex(j)).columnName();
						pkSql.append(columnName.toUpperCase());
						pkSql.append("\"");
						constraint.append(columnName);
					}
					constraint.append("\"");
					pkSql.append(") NOT DEFERRABLE INITIALLY IMMEDIATE");

					if (USE_NAMED_CONSTRAINTS)
						sql.append(constraint);
					sql.append(pkSql);

					return new NSArray<EOSQLExpression>(_expressionForString(sql.toString()));
				}
			}
			return NSArray.emptyArray();
		}
	}

	public static class FrontbaseExpression extends JDBCExpression {
		private boolean _useBindVariables;
		EOQualifier _qualifier;
		NSMutableArray _lobList;

		/**
		 * Holds array of join clauses.
		 */
		private NSMutableArray<JoinClause> _alreadyJoined = new NSMutableArray<JoinClause>();

		public FrontbaseExpression(EOEntity eoentity) {
			super(eoentity);
			_useBindVariables = "true".equalsIgnoreCase(System.getProperty("FrontBasePlugIn.useBindVariables"));
			_rtrimFunctionName = null;
			_externalQuoteChar = "\"";
		}

		@Override
		public void addCreateClauseForAttribute(EOAttribute attribute) {
			StringBuilder sql = new StringBuilder();

			sql.append("\"");
			sql.append(attribute.columnName());
			sql.append("\" ");
			sql.append(columnTypeStringForAttribute(attribute));

			NSDictionary dictionary = attribute.userInfo();
			if (dictionary == null) {
				_appendNotNullConstraintIfNecessary(attribute, sql);
			}
			else {
				// Default values.
				Object defaultValue = dictionary.valueForKey("Default");
		        if (defaultValue == null) {
		            defaultValue = dictionary.valueForKey("er.extensions.eoattribute.default"); // deprecated key
		        }
		        if (defaultValue == null) {
		            defaultValue = dictionary.valueForKey("default");
		        }
				if (defaultValue != null) {
					sql.append(" DEFAULT ");
					sql.append(formatValueForAttribute(defaultValue, attribute));
				}

				// Column constraints.
				_appendNotNullConstraintIfNecessary(attribute, sql);

				if (dictionary.valueForKey("Unique") != null && dictionary.valueForKey("Unique").equals("true")) {
					sql.append(" UNIQUE");
				}
				if (dictionary.valueForKey("Check") != null) {
					sql.append(" CHECK ");
					sql.append(dictionary.valueForKey("Check"));
				}

				// Column collation.
				if (dictionary.valueForKey("Collate") != null) {
					sql.append(" COLLATE ");
					sql.append(dictionary.valueForKey("Collate"));
				}
			}
			appendItemToListString(sql.toString(), _listString());
		}

		protected boolean shouldAllowNull(EOAttribute attribute) {
			boolean shouldAllowNull = attribute.allowsNull();
			// If you allow nulls, then there's never a problem ...
			if (!shouldAllowNull) {
				EOEntity entity = attribute.entity();
				EOEntity parentEntity = entity.parentEntity();
				String externalName = entity.externalName();
				if (externalName != null) {
					// If you have a parent entity and that parent entity shares your table name, then you're single table inheritance
					boolean singleTableInheritance = (parentEntity != null && externalName.equals(parentEntity.externalName()));
					if (singleTableInheritance) {
						EOAttribute parentAttribute = parentEntity.attributeNamed(attribute.name());
						if (parentAttribute == null) {
							// If this attribute is new in the subclass, you have to allow nulls
							shouldAllowNull = true;
						}
					}
				}
			}
			return shouldAllowNull;
		}

		private void _appendNotNullConstraintIfNecessary(EOAttribute attribute, StringBuilder sql) {
			if (!shouldAllowNull(attribute)) {
				if (USE_NAMED_CONSTRAINTS) {
					sql.append(" CONSTRAINT ");
					sql.append(notNullConstraintName(attribute));
				}
				sql.append(" NOT NULL");

				if (isLOBAttribute(attribute))
					sql.append(" DEFERRABLE INITIALLY DEFERRED");
			}
		}

		@Override
		public String columnTypeStringForAttribute(EOAttribute attribute) {
			String externalTypeName = attribute.externalType();
			NSDictionary typeInfo = (NSDictionary) jdbcInfo().objectForKey(JDBCAdaptor.TypeInfoKey);
			if (typeInfo == null) {
				typeInfo = JDBCAdaptor.typeInfoForModel(((EOEntity) attribute.parent()).model());
			}
			NSDictionary externalTypeInfo = (NSDictionary) typeInfo.objectForKey(externalTypeName);
			if (externalTypeInfo == null && externalTypeName != null) {
				externalTypeInfo = (NSDictionary) typeInfo.objectForKey(externalTypeName.toUpperCase());
			}

			if (externalTypeInfo == null) {
				throw new JDBCAdaptorException("Unable to find type information for external type '" + externalTypeName + "' in attribute '" + attribute.name() + "' of entity '" + ((EOEntity) attribute.parent()).name() + "'.  Check spelling and capitalization.", null);
			}
			int createParams;
			try {
				Object createParamsObj = externalTypeInfo.objectForKey("createParams");
				if (createParamsObj instanceof Integer) {
					createParams = ((Integer) createParamsObj).intValue();
				}
				else {
					createParams = Integer.parseInt((String) createParamsObj);
				}
			}
			catch (NumberFormatException numberformatexception) {
				createParams = 0;
			}
			switch (createParams) {
			case 2:
				int precision = attribute.precision();
				if (precision == 0) {
					return attribute.externalType();
				}
				int scale = attribute.scale();
				if (scale == 0) {
					return attribute.externalType() + "(" + precision + ")";
				}
				else {
					return attribute.externalType() + "(" + precision + "," + scale + ")";
				}
			case 1:
				int length = attribute.width();
				if (length == 0) {
					length = attribute.precision();
				}
				if (length == 0) {
					return attribute.externalType();
				}
				else {
					return attribute.externalType() + "(" + length + ")";
				}
			}
			return attribute.externalType();
		}

		public Class _synchronizationFactoryClass() {
			return FrontbaseSynchronizationFactory.class;
		}

		EOQualifier qualifier() {
			return _qualifier;
		}
		
		@Override
		public String sqlStringForSelector(NSSelector selector, Object value) {
			String retStr = null;
			
			if (_frontbaseContainsOperatorFix == null) {
				retStr = sqlStringForSelectorTreatingContainsAsLike(selector, value);
			} else {
				retStr = super.sqlStringForSelector(selector, value);
			}
			
			return retStr;
		}

		protected String sqlStringForSelectorTreatingContainsAsLike(NSSelector qualifierOperator, Object value) {
			if (qualifierOperator.equals(EOQualifier.QualifierOperatorContains))
				if (value == NSKeyValueCoding.NullValue)
					return "is";
				else
					return "like";
			else
				return super.sqlStringForSelector(qualifierOperator, value);
		}

		@Override
		public String externalNameQuoteCharacter() {
			return "\"";
		}

		@Override
		public String sqlStringForAttribute(EOAttribute attribute) {
			String value = super.sqlStringForAttribute(attribute);

			if (!useAliases())
				value = "\"" + value + "\"";

			return value;
		}

		/**
		 * Overridden to not call the super implementation.
		 *
		 * @param leftName  the table name on the left side of the clause
		 * @param rightName the table name on the right side of the clause
		 * @param semantic  the join semantic
		 */
		@Override
		public void addJoinClause(String leftName, String rightName, int semantic) {
			assembleJoinClause(leftName, rightName, semantic);
		}

		/**
		 * Overridden to construct a valid SQL92 JOIN clause as opposed to
		 * the Oracle-like SQL the superclass produces.
		 *
		 * @param leftName  the table name on the left side of the clause
		 * @param rightName the table name on the right side of the clause
		 * @param semantic  the join semantic
		 * @return  the join clause
		 */
		@Override
		public String assembleJoinClause(String leftName, String rightName, int semantic) {
			// Can't handle this
			if (!useAliases()) {
				return super.assembleJoinClause(leftName, rightName, semantic);
			}

			String leftAlias = leftName.substring(0, leftName.indexOf("."));
			String rightAlias = rightName.substring(0, rightName.indexOf("."));

			NSArray k;
			EOEntity rightEntity;
			EOEntity leftEntity;
			String relationshipKey = null;
			EORelationship r;

			if (leftAlias.equals("t0")) {
				leftEntity = entity();
			}
			else {
				k = aliasesByRelationshipPath().allKeysForObject(leftAlias);
				relationshipKey = k.count() > 0 ? (String) k.lastObject() : "";
				leftEntity = entityForKeyPath(relationshipKey);
			}

			if (rightAlias.equals("t0")) {
				rightEntity = entity();
			}
			else {
				k = aliasesByRelationshipPath().allKeysForObject(rightAlias);
				relationshipKey = k.count() > 0 ? (String) k.lastObject() : "";
				rightEntity = entityForKeyPath(relationshipKey);
			}

			int dotIndex = relationshipKey.indexOf(".");
			relationshipKey = dotIndex == -1 ? relationshipKey : relationshipKey.substring(relationshipKey.lastIndexOf(".") + 1);
			r = rightEntity.anyRelationshipNamed(relationshipKey);

			// fix from Michael Müller for the case Foo.fooBars.bar has a Bar.foo relationship (instead of Bar.foos)
			if (r == null || r.destinationEntity() != leftEntity) {
				r = leftEntity.anyRelationshipNamed(relationshipKey);
			}

			String rightTable = rightEntity.valueForSQLExpression(this);
			String leftTable = leftEntity.valueForSQLExpression(this);
			JoinClause jc = new JoinClause();

			jc.setTable1(leftTable, leftAlias);
			jc.table2 = rightTable + " " + rightAlias;

			switch (semantic) {
			case EORelationship.LeftOuterJoin:
				jc.op = " LEFT OUTER JOIN ";
				break;
			case EORelationship.RightOuterJoin:
				jc.op = " RIGHT OUTER JOIN ";
				break;
			case EORelationship.FullOuterJoin:
				jc.op = " FULL OUTER JOIN ";
				break;
			case EORelationship.InnerJoin:
				jc.op = " INNER JOIN ";
				break;
			}

			NSArray joins = r.joins();
			int joinsCount = joins.count();
			NSMutableArray joinStrings = new NSMutableArray(joinsCount);
			for (int i = 0; i < joinsCount; i++) {
				EOJoin currentJoin = (EOJoin) joins.objectAtIndex(i);
				String left = leftAlias + "." + sqlStringForSchemaObjectName(currentJoin.sourceAttribute().columnName());
				String right = rightAlias + "." + sqlStringForSchemaObjectName(currentJoin.destinationAttribute().columnName());
				joinStrings.addObject(left + " = " + right);
			}
			jc.joinCondition = " ON " + joinStrings.componentsJoinedByString(" AND ");
			if (!_alreadyJoined.containsObject(jc)) {
				_alreadyJoined.insertObjectAtIndex(jc, 0);
				return jc.toString();
			}
			return null;
		}

		/**
		 * Overridden to handle correct placements of join conditions and
		 * to handle DISTINCT fetches with compareCaseInsensitiveA(De)scending sort orders.
		 *
		 * @param attributes    the attributes to select
		 * @param lock  flag for locking rows in the database
		 * @param qualifier the qualifier to restrict the selection
		 * @param fetchOrder    specifies the fetch order
		 * @param columnList    the SQL columns to be fetched
		 * @param tableList the the SQL tables to be fetched
		 * @param whereClause   the SQL where clause
		 * @param joinClause    the SQL join clause
		 * @param orderByClause the SQL sort order clause
		 * @param lockClause    the SQL lock clause
		 * @return  the select statement
		 */
		@Override
		public String assembleSelectStatementWithAttributes(NSArray attributes, boolean lock, EOQualifier qualifier, NSArray fetchOrder, String selectString, String columnList, String tableList, String whereClause, String joinClause, String orderByClause, String lockClause) {

			// Adds the labels in an order by clause to the list of columns in the select
			// clause if not already existing.
			if (orderByClause != null && orderByClause.length() > 0) {
				int i = 0;
				while (i != -1) {
					if (orderByClause.indexOf(' ', i) == i + 1)
						i += 2;

					int j = orderByClause.indexOf(' ', i);
					int k = orderByClause.indexOf(',', i);
					if (j > k && k != -1)
						j = k;
					else if (j == -1 && k == -1)
						j = orderByClause.length();

					String orderColumn = orderByClause.substring(i, j);
					if (columnList.indexOf(orderColumn) == -1)
						columnList = columnList.concat(", " + orderColumn);

					i = orderByClause.indexOf(',', i);
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append(selectString);
			sb.append(columnList);
			// AK: using DISTINCT with ORDER BY UPPER(foo) is an error if it is not also present in the columns list...
			// This implementation sucks, but should be good enough for the normal case
			if (selectString.indexOf(" DISTINCT") != -1) {
				String[] columns = orderByClause.split(",");
				for (int i = 0; i < columns.length; i++) {
					String column = columns[i].replaceFirst("\\s+(ASC|DESC)\\s*", "");
					if (columnList.indexOf(column) == -1) {
						sb.append(", ");
						sb.append(column);
					}
				}
			}

			sb.append(" FROM ");
			String fieldString;
			if (_alreadyJoined.count() > 0) {
				fieldString = joinClauseString();
			}
			else {
				fieldString = tableList;
			}
			sb.append(fieldString);

			if ((whereClause != null && whereClause.length() > 0) || (joinClause != null && joinClause.length() > 0)) {
				sb.append(" WHERE ");
				if (joinClause != null && joinClause.length() > 0) {
					sb.append(joinClause);
					if (whereClause != null && whereClause.length() > 0)
						sb.append(" AND ");
				}

				if (whereClause != null && whereClause.length() > 0) {
					sb.append(whereClause);
				}
			}

			if (orderByClause != null && orderByClause.length() > 0) {
				sb.append(" ORDER BY ");
				sb.append(orderByClause);
			}
			if (lockClause != null && lockClause.length() > 0) {
				sb.append(' ');
				sb.append(lockClause);
			}
			return sb.toString();
		}

		/**
		 * Overrides the parent implementation to compose the final string
		 * expression for the join clauses.
		 */
		@Override
		public String joinClauseString() {
			NSMutableDictionary seenIt = new NSMutableDictionary();
			StringBuilder sb = new StringBuilder();
			JoinClause jc;
			EOSortOrdering.sortArrayUsingKeyOrderArray(_alreadyJoined, new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey("sortKey", EOSortOrdering.CompareCaseInsensitiveAscending)));
			if (_alreadyJoined.count() > 0) {
				jc = _alreadyJoined.objectAtIndex(0);

				sb.append(jc);
				seenIt.setObjectForKey(Boolean.TRUE, jc.table1);
				seenIt.setObjectForKey(Boolean.TRUE, jc.table2);
			}

			for (int i = 1; i < _alreadyJoined.count(); i++) {
				jc = _alreadyJoined.objectAtIndex(i);

				sb.append(jc.op);
				if (seenIt.objectForKey(jc.table1) == null) {
					sb.append(jc.table1);
					seenIt.setObjectForKey(Boolean.TRUE, jc.table1);
				}
				else if (seenIt.objectForKey(jc.table2) == null) {
					sb.append(jc.table2);
					seenIt.setObjectForKey(Boolean.TRUE, jc.table2);
				}
				sb.append(jc.joinCondition);
			}
			return sb.toString();
		}

		@Override
		public void addOrderByAttributeOrdering(EOSortOrdering eosortordering) {
			NSSelector sortOrdering = eosortordering.selector();
			String attribute = eosortordering.key();
			String column = sqlStringForAttributeNamed(attribute);

			if (column == null)
				// Super throws exception.
				super.addOrderByAttributeOrdering(eosortordering);

			StringBuilder sql = new StringBuilder(column);

			if (sortOrdering == EOSortOrdering.CompareCaseInsensitiveAscending)
				if (entity()._attributeForPath(attribute).adaptorValueType() == 1)
					sql.append(" COLLATE INFORMATION_SCHEMA.CASE_INSENSITIVE ASC");
				else
					sql.append(" ASC");
			else if (sortOrdering == EOSortOrdering.CompareCaseInsensitiveDescending)
				if (entity()._attributeForPath(attribute).adaptorValueType() == 1)
					sql.append(" COLLATE INFORMATION_SCHEMA.CASE_INSENSITIVE DESC");
				else
					sql.append(" DESC");
			else if (sortOrdering == EOSortOrdering.CompareAscending)
				sql.append(" ASC");
			else if (sortOrdering == EOSortOrdering.CompareDescending)
				sql.append(" DESC");

			appendItemToListString(sql.toString(), _orderByString());
		}

		@Override
		public String assembleDeleteStatementWithQualifier(EOQualifier eoqualifier, String table, String qualifier) {
			if (table != null && table.indexOf('"') == -1)
				return super.assembleDeleteStatementWithQualifier(eoqualifier, quoteTableName(table), qualifier);
			else
				return super.assembleDeleteStatementWithQualifier(eoqualifier, table, qualifier);
		}

		@Override
		public String assembleInsertStatementWithRow(NSDictionary row, String table, String columns, String values) {
			if (table != null && table.indexOf('"') == -1)
				return super.assembleInsertStatementWithRow(row, quoteTableName(table), columns, values);
			else
				return super.assembleInsertStatementWithRow(row, table, columns, values);
		}

		@Override
		public String assembleUpdateStatementWithRow(NSDictionary row, EOQualifier qualifier, String table, String values, String sqlQualifier) {
			_qualifier = qualifier;
			if (table != null && table.indexOf('"') == -1)
				return super.assembleUpdateStatementWithRow(row, qualifier, quoteTableName(table), values, sqlQualifier);
			else
				return super.assembleUpdateStatementWithRow(row, qualifier, table, values, sqlQualifier);
		}

		@Override
		public String lockClause() {
			return "";
		}

		@Override
		public boolean useBindVariables() {
			return _useBindVariables;
		}

		@Override
		public boolean shouldUseBindVariableForAttribute(EOAttribute eoattribute) {
			return useBindVariables() && !isLOBAttribute(eoattribute);
		}

		private boolean isLOBAttribute(EOAttribute att) {
			int internalType = FrontBaseTypes.internalTypeForExternal(att.externalType());
			return internalType == FrontBaseTypes.FB_BLOB || internalType == FrontBaseTypes.FB_CLOB;
		}

		@Override
		public boolean mustUseBindVariableForAttribute(EOAttribute eoattribute) {
			return false;
		}

		@Override
		public String sqlStringForCaseInsensitiveLike(String value, String column) {
			StringBuilder sql = new StringBuilder();

			sql.append(column);
			sql.append(" LIKE ");
			sql.append(value);
			sql.append(" COLLATE INFORMATION_SCHEMA.CASE_INSENSITIVE");

			return sql.toString();
		}

		boolean hasLOBsToUpdate() {
			return _lobList != null && _lobList.count() > 0;
		}

		void resetlobList() {
			_lobList = null;
		}

		NSArray lobList() {
			return _lobList != null ? _lobList : NSArray.EmptyArray;
		}

		@Override
		public String sqlStringForKeyValueQualifier(EOKeyValueQualifier eokeyvaluequalifier) {
			String attrubute = eokeyvaluequalifier.key();
			String column = sqlStringForAttributeNamed(attrubute);

			if (column == null)
				throw new IllegalStateException("sqlStringForKeyValueQualifier: attempt to generate SQL for " + eokeyvaluequalifier.getClass().getName() + " " + eokeyvaluequalifier + " failed because attribute identified by key '" + attrubute + "' was not reachable from from entity '" + _entity.name() + "'");
			Object qualifier = eokeyvaluequalifier.value();
			if (qualifier instanceof EOQualifierVariable)
				throw new IllegalStateException("sqlStringForKeyValueQualifier: attempt to generate SQL for " + eokeyvaluequalifier.getClass().getName() + " " + eokeyvaluequalifier + " failed because the qualifier variable '$" + ((EOQualifierVariable) qualifier).key() + "' is unbound.");
			column = formatSQLString(column, _entity._attributeForPath(attrubute).readFormat());
			NSSelector nsselector = eokeyvaluequalifier.selector();

			boolean flag = false;
			if (_frontbaseContainsOperatorFix == null) {
				flag = nsselector.equals(EOQualifier.QualifierOperatorLike) || nsselector.equals(EOQualifier.QualifierOperatorCaseInsensitiveLike) || nsselector.equals(EOQualifier.QualifierOperatorContains);
			} else {
				flag = nsselector.equals(EOQualifier.QualifierOperatorLike) || nsselector.equals(EOQualifier.QualifierOperatorCaseInsensitiveLike);
			}

			if (flag) {
				qualifier = sqlPatternFromShellPattern(qualifier.toString());
			}

			StringBuilder sql = new StringBuilder();
			char sqlEscapeChar = sqlEscapeChar();
			String value;

			if (nsselector.equals(EOQualifier.QualifierOperatorCaseInsensitiveLike)) {
				value = sqlStringForValue(qualifier, attrubute);

				sql.append(sqlStringForCaseInsensitiveLike(value, column));
			}
			else {
				value = sqlStringForValue(qualifier, attrubute);

				sql.append(column);
				sql.append(' ');
				sql.append(sqlStringForSelector(nsselector, qualifier));
				sql.append(' ');
				sql.append(value);
			}

			if (value.indexOf(sqlEscapeChar) != -1 && flag) {
				sql.append(" ESCAPE '");
				sql.append(sqlEscapeChar);
				sql.append('\'');
			}
			return sql.toString();
		}
	    

		@Override
		public String formatValueForAttribute(Object obj, EOAttribute eoattribute) {
			if (obj != null && obj != NSKeyValueCoding.NullValue) {
				if (eoattribute.valueFactoryMethod() != null && eoattribute.valueFactoryMethod().implementedByObject(obj) && eoattribute.adaptorValueConversionMethod().implementedByObject(obj)) {
					obj = eoattribute.adaptorValueByConvertingAttributeValue(obj);
				}

				if (eoattribute.externalType() == null) {
					throw new EOGeneralAdaptorException("Attribute " + eoattribute.name() + " on entity " + eoattribute.entity().name() + " with prototype named " + eoattribute.prototypeName() + " has no external type defined");
				}

				switch (FrontBaseTypes.internalTypeForExternal(eoattribute.externalType())) {
				case FrontBaseTypes.FB_Character:
				case FrontBaseTypes.FB_VCharacter: {
					return escapedString(obj);
				}
				case FrontBaseTypes.FB_DayTime: {
					return escapedString(obj);
				}
				case FrontBaseTypes.FB_BLOB:
				case FrontBaseTypes.FB_CLOB: {
					if (!(obj instanceof String) && eoattribute.valueFactoryMethod() != null) {
						Class<?> valueClass = _NSUtilities.classWithName(eoattribute.className());
						if (valueClass.isAssignableFrom(obj.getClass())) {
							obj = eoattribute.adaptorValueByConvertingAttributeValue(obj);
						}
					}
					if (obj instanceof String)
						if (((String) obj).length() == 27 && ((String) obj).startsWith("@"))
							return (String) obj;
					if (_lobList == null)
						_lobList = new NSMutableArray();
					_lobList.addObject(eoattribute);
					_lobList.addObject(obj);
					return "NULL";
				}
				case FrontBaseTypes.FB_VBit:
				case FrontBaseTypes.FB_Bit: {
					if (obj instanceof NSData) {
						return formatBit((NSData) obj);
					}
					else {
						return "ERROR: Can not convert value from " + obj + " to Bit or Byte data type";
					}
				}
				case FrontBaseTypes.FB_Time: {
					StringBuffer time = new StringBuffer("TIME '");
					Date d = (Date)eoattribute.adaptorValueByConvertingAttributeValue(obj);
					TIME_FORMATTER.get().format(d, time, new FieldPosition(0));
					time.append('\'');
					return time.toString();
				}

				case FrontBaseTypes.FB_TimeTZ: {
					StringBuffer time = new StringBuffer("TIME '");
					Date d = (Date)eoattribute.adaptorValueByConvertingAttributeValue(obj);
					SimpleDateFormat formatter = TIME_FORMATTER.get();
					formatter.format(d, time, new FieldPosition(0));
					time.append(getTimeZone(formatter.getTimeZone()));
					time.append('\'');
					return time.toString();
				}

				case FrontBaseTypes.FB_Timestamp: {
					StringBuffer time = new StringBuffer("TIMESTAMP '");
					Date d = (Date)eoattribute.adaptorValueByConvertingAttributeValue(obj);
					TIMESTAMP_FORMATTER.get().format(d, time, new FieldPosition(0));
					time.append('\'');
					return time.toString();
				}
				case FrontBaseTypes.FB_TimestampTZ: {
					StringBuffer time = new StringBuffer("TIMESTAMP '");
					Date d = (Date)eoattribute.adaptorValueByConvertingAttributeValue(obj);
					SimpleDateFormat formatter = TIMESTAMP_FORMATTER.get();
					formatter.format(d, time, new FieldPosition(0));
					time.append(getTimeZone(formatter.getTimeZone()));
					time.append('\'');
					return time.toString();
				}
				case FrontBaseTypes.FB_Date: {
					StringBuffer time = new StringBuffer("DATE '");
					Date d = (Date)eoattribute.adaptorValueByConvertingAttributeValue(obj);
					DATE_FORMATTER.get().format(d, time, new FieldPosition(0));
					time.append('\'');
					return time.toString();
				}
				case FrontBaseTypes.FB_Boolean: {
					if (obj instanceof Boolean) {
						return obj.toString();
					}
					else if (obj instanceof String) {
						String str = (String) obj;
						if ("yes".equalsIgnoreCase(str) || "y".equalsIgnoreCase(str) || "true".equalsIgnoreCase(str) || "1".equalsIgnoreCase(str)) {
							return "TRUE";
						}
						else if ("no".equalsIgnoreCase(str) || "n".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str) || "0".equalsIgnoreCase(str)) {
							return "FALSE";
						}
						else {
							throw new IllegalArgumentException("Unknown boolean value '" + str + "' for the attribute " + eoattribute.entity().name() + "." + eoattribute.name() + ".");
						}
					}
					else if (obj instanceof NSData) {
						return (((NSData) obj).bytes()[0] == 0) ? "FALSE" : "TRUE";
					}
					else if (((Number) obj).intValue() == 0) {
						return "FALSE";
					}
					else {
						return "TRUE";
					}
				}
				case FrontBaseTypes.FB_SmallInteger:
				case FrontBaseTypes.FB_Float:
				case FrontBaseTypes.FB_Real:
				case FrontBaseTypes.FB_Double:
				case FrontBaseTypes.FB_LongInteger:
				case FrontBaseTypes.FB_TinyInteger:
				case FrontBaseTypes.FB_Numeric:
				case FrontBaseTypes.FB_Integer:
				case FrontBaseTypes.FB_Decimal: {
					if (obj instanceof BigDecimal) {
						return ((BigDecimal) obj).setScale(eoattribute.scale(), BigDecimal.ROUND_HALF_UP).toString();
					}
					else if (obj instanceof Number) {
						String valueType = eoattribute.valueType();
						if (valueType == null || "i".equals(valueType)) {
							return String.valueOf(((Number) obj).intValue());
						}
						else if ("l".equals(valueType)) {
							return String.valueOf(((Number) obj).longValue());
						}
						else if ("f".equals(valueType)) {
							return String.valueOf(((Number) obj).floatValue());
						}
						else if ("d".equals(valueType)) {
							return String.valueOf(((Number) obj).doubleValue());
						}
						else if ("s".equals(valueType)) {
							return String.valueOf(((Number) obj).shortValue());
						}
						else if ("c".equals(valueType)) {
							return String.valueOf(((Number) obj).intValue());
						}
						else {
							throw new IllegalArgumentException("Unknown number value type '" + valueType + "' for the attribute " + eoattribute.entity().name() + "." + eoattribute.name() + ".");
						}
					}
					else if (obj instanceof Boolean) {
						String valueType = eoattribute.valueType();
						return String.valueOf(((Boolean) obj).booleanValue() ? 1 : 0);
					}
					else if (obj instanceof String) {
						String str = (String) obj;
						String valueType = eoattribute.valueType();
						if (valueType == null || "i".equals(valueType)) {
							return String.valueOf(Integer.parseInt(str));
						}
						else if ("l".equals(valueType)) {
							return String.valueOf(Long.parseLong(str));
						}
						else if ("f".equals(valueType)) {
							return String.valueOf(Float.parseFloat(str));
						}
						else if ("d".equals(valueType)) {
							return String.valueOf(Double.parseDouble(str));
						}
						else if ("s".equals(valueType)) {
							return String.valueOf(Short.parseShort(str));
						}
						else if ("c".equals(valueType)) {
							return String.valueOf(Integer.parseInt(str));
						}
						else {
							throw new IllegalArgumentException("Unknown number value type '" + valueType + "' for attribute " + eoattribute.entity().name() + "." + eoattribute.name() + ".");
						}
					}
					else {
						throw new IllegalArgumentException("Unknown number value '" + obj + "' for attribute " + eoattribute.entity().name() + "." + eoattribute.name() + ".");
					}
				}
				default:
					// MS: I think we should probably throw IllegalArgumentException here, but I'm a little concerned about breaking people's apps.
					return escapedString(obj);
				}
			}
			return super.formatValueForAttribute(obj, eoattribute);
		}

		public String escapedString(Object obj) {
			String escapedStr;
			String value = obj.toString();
			if (value.indexOf("'") == -1) {
				escapedStr = "'" + value + "'";
			}
			else {
				escapedStr = "'" + addEscapeChars(value) + "'";
			}
			return escapedStr;
		}

		public String addEscapeChars(String value) {
			StringBuilder sb = new StringBuilder(value);
			int index = 0;

			for (int i = 0; i < value.length(); i++, index++) {
				index = value.indexOf("'", index);
				if (index == -1)
					break;
				sb.insert(index + i, "'");
			}

			return sb.toString();
		}

		String formatBit(NSData data) {
			char[] heximals = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
			byte[] bytes = data.bytes();
			int b;

			StringBuilder result = new StringBuilder((2 * data.length()) + 3);
			result.append("X'");

			for (int i = 0; i < data.length(); i++) {
				b = bytes[i] & 0xFF;
				result.append(heximals[b / 16]);
				result.append(heximals[b % 16]);
			}

			result.append('\'');
			return result.toString();
		}

		private String getTimeZone(java.util.TimeZone tz) {
			String sign = "+";
			int tzOffset = tz.getRawOffset();

			if (tz.useDaylightTime() && tz.inDaylightTime(new java.util.Date(System.currentTimeMillis()))) {
				tzOffset += 3600000L;
			}

			if (tzOffset < 0) {
				tzOffset = -tzOffset;
				sign = "-";
			}

			int hour = tzOffset / 3600000;
			int minute = (tzOffset % 3600000) / 60000;

			String hourString = String.valueOf(hour);
			String minuteString = String.valueOf(minute);

			if (hourString.length() < 2)
				hourString = "0" + hourString;
			if (minuteString.length() < 2)
				minuteString = "0" + minuteString;

			return sign + hourString + ":" + minuteString;
		}

		/**
		 * Utility that traverses a key path to find the last destination entity
		 *
		 * @param keyPath   the key path
		 * @return  the entity at the end of the keypath
		 */
		private EOEntity entityForKeyPath(String keyPath) {
			NSArray keys = NSArray.componentsSeparatedByString(keyPath, ".");
			EOEntity ent = entity();

			for (int i = 0; i < keys.count(); i++) {
				String k = (String) keys.objectAtIndex(i);
				EORelationship rel = ent.anyRelationshipNamed(k);
				if (rel == null) {
					// it may be an attribute
					if (ent.anyAttributeNamed(k) != null) {
						break;
					}
					throw new IllegalArgumentException("relationship " + keyPath + " generated null");
				}
				ent = rel.destinationEntity();
			}
			return ent;
		}

		/**
		 * Helper class that stores a join definition and
		 * helps <code>FrontbaseExpression</code> to assemble
		 * the correct join clause.
		 */
		public static class JoinClause {
			String table1;
			String op;
			String table2;
			String joinCondition;
	    	String sortKey;

			@Override
			public String toString() {
				return table1 + op + table2 + joinCondition;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == null || !(obj instanceof JoinClause)) {
					return false;
				}
				return toString().equals(obj.toString());
			}
			
			public void setTable1(String leftTable, String leftAlias) {
	    		table1 = leftTable + " " + leftAlias;
	    		sortKey = leftAlias.substring(1);
	    		if (sortKey.length() < 2) {
	    			// add padding for cases with >9 joins
	    			sortKey = " " + sortKey;
	    		}
	    	}

			/**
			 * Returns the table alias for the first table (e.g. returns T2 if table 1 is "Students" T2).  This makes this class "sortable"
			 * which is needed to correctly assemble a join clause.
			 *
			 * @return the table alias (e.g. returns T2 if table1 is "Students" T2)
			 */
			public String sortKey() {
				return sortKey;
			}
		}
	}
}
