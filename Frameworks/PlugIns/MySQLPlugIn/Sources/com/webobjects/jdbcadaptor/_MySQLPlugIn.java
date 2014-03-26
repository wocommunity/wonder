package com.webobjects.jdbcadaptor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.synchronization.EOSchemaGenerationOptions;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationFactory;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSProperties;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSStringUtilities;

public class _MySQLPlugIn extends JDBCPlugIn {

	private static final String DriverClassName = "com.mysql.jdbc.Driver";

	private static final String DriverProductName = "MySQL";
	
	private static final String QUERY_STRING_USE_BUNDLED_JDBC_INFO = "useBundledJdbcInfo";

	public _MySQLPlugIn(JDBCAdaptor adaptor) {
		super(adaptor);
	}

	public static class MySQLExpression extends JDBCExpression {

		// Lazy initialized constants
		private static class CONFIG {
			// Turning on identifier quoting allows the use of reserved words for identifier (table, field, etc.) names
			final static boolean ENABLE_IDENTIFIER_QUOTING = Boolean.getBoolean("com.webobjects.jdbcadaptor.MySQLExpression.enableIdentifierQuoting");
			// Inserts "\n\t" between statement clauses for log readability. Useful in development
			final static boolean LINE_PER_CLAUSE = Boolean.getBoolean("com.webobjects.jdbcadaptor.MySQLExpression.enableLinePerClause");
			
			// Length values for the string constant elements of the statement taking into account LINE_PER_CLAUSE for development and/or MySQL log readability.
			// Note that the space is needed before FROM, WHERE etc in the LINE_PER_CLAUSE variant to ensure compatibility with code that assumes a space
			// surrounding the FROM, as in er.extensions.jdbc.ERXSQLHelper.rowCountForFetchSpecification(...) for example 
			final static String FROM_STRING = ( CONFIG.LINE_PER_CLAUSE ? "\n\t FROM " : " FROM " );
			final static String WHERE_STRING = ( CONFIG.LINE_PER_CLAUSE ? "\n\t WHERE " : " WHERE " );
			final static String ORDER_BY_STRING = ( CONFIG.LINE_PER_CLAUSE ? "\n\t ORDER BY " : " ORDER BY " );
			final static String LIMIT_STRING = ( CONFIG.LINE_PER_CLAUSE ? "\n\t LIMIT " : " LIMIT " );
			final static int FROM_LENGTH = FROM_STRING.length();
			final static int WHERE_LENGTH = WHERE_STRING.length();
			final static int ORDER_BY_LENGTH = ORDER_BY_STRING.length();
			final static int LIMIT_LENGTH = LIMIT_STRING.length();
			
			/**
			 * From the MySQL Manual: &quot;An identifier may be quoted or
			 * unquoted. If an identifier contains special characters or is a
			 * reserved word, you must quote it whenever you refer to it. ...
			 * The identifier quote character is the backtick.&quot;
			 */
			final static String IDENTIFIER_QUOTE_CHARACTER = (ENABLE_IDENTIFIER_QUOTING ? "`" : "");
		}

		private int _fetchLimit;
		
		private NSRange _fetchRange;
		private final NSSelector<NSRange> _fetchRangeSelector = new NSSelector<NSRange>("fetchRange");
		
		/**
		 * Holds array of join clause definitions
		 */
		private final NSMutableArray<JoinClauseDefinition> _alreadyJoined = new NSMutableArray<JoinClauseDefinition>();
		
		public MySQLExpression(EOEntity entity) {
			super(entity);
		}

		/**
		 * http://dev.mysql.com/doc/refman/5.5/en/string-comparison-functions.html
		 * 
		 * @see com.webobjects.eoaccess.EOSQLExpression#sqlEscapeChar()
		 */
		@Override
        public char sqlEscapeChar(){
			return '|';
		}

		/**
		 * Overridden because MySQL does not use the default quote character in
		 * EOSQLExpression.externalNameQuoteCharacter() which is an empty
		 * string.
		 *
		 * Note that quoting is disabled by default and can be enabled by setting property <code>com.webobjects.jdbcadaptor.MySQLExpression.enableIdentifierQuoting</code> to true.
		 */
		@Override
		public String externalNameQuoteCharacter() {
			return CONFIG.IDENTIFIER_QUOTE_CHARACTER;
		}

		/**
		 * Overriding super here so we can grab a fetch range or fetch limit if specified in the EOFetchSpecification. If a fetchRange method
		 * returning an NSRange exists in the EOFetchSpecification subclass being passed in, the the fetchLimit will be ignored.
		 *
		 * @see com.webobjects.jdbcadaptor.JDBCExpression#prepareSelectExpressionWithAttributes(NSArray, boolean, EOFetchSpecification)
		 */
		@Override
		public void prepareSelectExpressionWithAttributes(NSArray<EOAttribute> attributes, boolean lock,
				EOFetchSpecification fetchSpec) {
			try {
				_fetchRange = _fetchRangeSelector.invoke(fetchSpec);
				// We will get an error when not using our custom ERXFetchSpecification subclass
				// We could have added ERExtensions to the classpath and checked for instanceof, but I thought
				// this is a little cleaner since people may be using this PlugIn and not Wonder in some legacy apps.
			} catch (IllegalArgumentException e) {
				;
			} catch (IllegalAccessException e) {
				;
			} catch (InvocationTargetException e) {
				;
			} catch (NoSuchMethodException e) {
				;
			}
			// Only check for fetchLimit of fetchRange is not provided.
			if (_fetchRange == null && !fetchSpec.promptsAfterFetchLimit()) {
				_fetchLimit = fetchSpec.fetchLimit();
			}
			super.prepareSelectExpressionWithAttributes(attributes, lock, fetchSpec);
		}

		/**
		 * Overriding to 
		 * <ul>
		 * <li>add LIMIT clause if _fetchLimit > 0</li>
		 * <li>support MySQL JOIN syntax (similar syntax to what PostgreSQL PlugIn generates)</li>
		 * </ul>
		 *
		 * @see com.webobjects.eoaccess.EOSQLExpression#assembleSelectStatementWithAttributes(NSArray, boolean, EOQualifier, NSArray, java.lang.String, String, String, String, String, String, String)
		 */
		@Override
		public String assembleSelectStatementWithAttributes(@SuppressWarnings("rawtypes") NSArray/*<EOAttribute>*/ attributes, boolean lock, EOQualifier qualifier,
				@SuppressWarnings("rawtypes") NSArray fetchOrder, String selectString, String columnList, String tableList, String whereClause,
				String joinClause, String orderByClause, String lockClause) {

			// When we are selecting from a single table, the joinClause will be empty and the tableList will contain the single table reference.
			// When we have joins, then both the joinClause and tableList will be passed in, however we will just be using the joinClause in the FROM clause.

			int size = selectString.length() + columnList.length() + CONFIG.FROM_LENGTH;
			if ((lockClause != null) && (lockClause.length() != 0)) {
				size += lockClause.length() + 1;
			}
			if ((whereClause != null) && (whereClause.length() != 0)) {
				size += (whereClause.length() + CONFIG.WHERE_LENGTH);
			}
			if ((joinClause != null) && (joinClause.length() != 0)) {
				size += joinClause.length();
			} else {
				size += tableList.length();
			}
			if ((orderByClause != null) && (orderByClause.length() != 0)) {
				size += (orderByClause.length() + CONFIG.ORDER_BY_LENGTH);
			}

			// If necessary, create LIMIT clause and add to buffer size
			String limitClause = null;
			// fetchRange override fetchLimit
			if (_fetchRange != null) {
				limitClause = _fetchRange.location() + ", " + _fetchRange.length();
				size += CONFIG.LIMIT_LENGTH + limitClause.length();
			} else if (_fetchLimit > 0) {
				limitClause = Integer.toString(_fetchLimit);
				size += CONFIG.LIMIT_LENGTH + limitClause.length();
			}

			StringBuilder buffer = new StringBuilder(size);
			buffer.append(selectString);
			buffer.append(columnList);
			buffer.append(CONFIG.FROM_STRING);
			
			if (joinClause != null && joinClause.length() > 0) {
				buffer.append(joinClause);
			} else {
			buffer.append(tableList);
			}

			if (whereClause != null && whereClause.length() > 0) {
				buffer.append(CONFIG.WHERE_STRING);
				buffer.append(whereClause);
			}

			if ((orderByClause != null) && (orderByClause.length() != 0)) {
				buffer.append(CONFIG.ORDER_BY_STRING);
				buffer.append(orderByClause);
			}

			// Add limit clause
			if (limitClause != null) {
				buffer.append(CONFIG.LIMIT_STRING);
				buffer.append(limitClause);
			}

			if ((lockClause != null) && (lockClause.length() != 0)) {
				buffer.append(' ');
				buffer.append(lockClause);
			}

			return buffer.toString();
		}

		/**
		 * Overriden to contruct a valid SQL92 JOIN clause as opposed to the
		 * Oracle-like SQL the superclass produces.
		 * 
		 * kieran copied from PostgresqlExpression
		 */
		@Override
		public String joinClauseString() {
			NSMutableDictionary<String, Boolean> seenIt = new NSMutableDictionary<String, Boolean>();
			StringBuilder sb = new StringBuilder();
			JoinClauseDefinition jc;
			EOSortOrdering.sortArrayUsingKeyOrderArray(_alreadyJoined, new NSArray<EOSortOrdering>(EOSortOrdering.sortOrderingWithKey("sortKey", EOSortOrdering.CompareAscending)));
			if (_alreadyJoined.count() > 0) {
				jc = _alreadyJoined.objectAtIndex(0);

				sb.append(jc);
				seenIt.setObjectForKey(Boolean.TRUE, jc._table1);
				seenIt.setObjectForKey(Boolean.TRUE, jc._table2);
			}

			for (int i = 1; i < _alreadyJoined.count(); i++) {
				jc = _alreadyJoined.objectAtIndex(i);

				sb.append(jc._op);
				if (seenIt.objectForKey(jc._table1) == null) {
					sb.append(jc._table1);
					seenIt.setObjectForKey(Boolean.TRUE, jc._table1);
				} else if (seenIt.objectForKey(jc._table2) == null) {
					sb.append(jc._table2);
					seenIt.setObjectForKey(Boolean.TRUE, jc._table2);
				}
				sb.append(jc._joinCondition);
			}
			return sb.toString();
		}
		
		/**
		 * Override so that the joinClause can be constructed after super has
		 * iterated though all joins and called our assembleJoinClause to create
		 * our array of JoinClauseDefinitions.
		 * 
		 * @see com.webobjects.eoaccess.EOSQLExpression#joinExpression()
		 */
		@Override
		public void joinExpression() {
			super.joinExpression();
			if (_alreadyJoined.count() > 0) {
				_joinClauseString = joinClauseString();
			} else {
				_joinClauseString = null;
			}
		}

		/**
		 * This is called by super for each join. We do not actually construct the join clause as we get called since
		 * for SQL92 JOIN syntax we need to know about all joins before we construct the complete join clause.
		 * 
		 * The objective of this implementation is to insert a new unique {@link JoinClauseDefinition} into
		 * the <code>_alreadyJoined</code> array of {@link JoinClauseDefinition} objects.
		 * 
		 * The join clause itself is assembled by <code>joinClauseString()</code>.
		 * 
		 * @param leftName
		 *            the table name on the left side of the clause
		 * @param rightName
		 *            the table name on the right side of the clause
		 * @param semantic
		 *            the join semantic
		 * @return the join clause
		 * 
		 * kieran based this on logic from PostgresqlExpression
		 */
		@SuppressWarnings("unchecked")
		@Override
		public String assembleJoinClause(String leftName, String rightName, int semantic) {
			if (!useAliases()) {
				return super.assembleJoinClause(leftName, rightName, semantic);
			}

			String leftAlias = leftName.substring(0, leftName.indexOf("."));
			String rightAlias = rightName.substring(0, rightName.indexOf("."));

			NSArray<String> k;
			EOEntity rightEntity;
			EOEntity leftEntity;
			String relationshipKey = null;
			EORelationship r;

			if (leftAlias.equals("t0")) {
				leftEntity = entity();
			} else {
				k = aliasesByRelationshipPath().allKeysForObject(leftAlias);
				relationshipKey = k.count() > 0 ? (String) k.lastObject() : "";
				leftEntity = entityForKeyPath(relationshipKey);
			}

			if (rightAlias.equals("t0")) {
				rightEntity = entity();
			} else {
				k = aliasesByRelationshipPath().allKeysForObject(rightAlias);
				relationshipKey = k.count() > 0 ? (String) k.lastObject() : "";
				rightEntity = entityForKeyPath(relationshipKey);
			}
			int dotIndex = relationshipKey.indexOf(".");
			relationshipKey = dotIndex == -1 ? relationshipKey : relationshipKey.substring(relationshipKey.lastIndexOf(".") + 1);
			r = rightEntity.anyRelationshipNamed(relationshipKey);
			// fix from Michael Müller for the case Foo.fooBars.bar has a
			// Bar.foo relationship (instead of Bar.foos)
			if (r == null || r.destinationEntity() != leftEntity) {
				r = leftEntity.anyRelationshipNamed(relationshipKey);
			}
			// timc 2006-02-26 IMPORTANT or quotes are ignored and mixed case
			// field names won't work
			String rightTable;
			String leftTable;
			if (CONFIG.ENABLE_IDENTIFIER_QUOTING) {
				rightTable = rightEntity.valueForSQLExpression(this);
				leftTable = leftEntity.valueForSQLExpression(this);
			} else {
				rightTable = rightEntity.externalName();
				leftTable = leftEntity.externalName();
			}

			// We need the numeric table by removing the leading 't' or 'T' from the table alias
			int leftTableID = Integer.parseInt(leftAlias.substring(1));
			
			// Compute left and right table references
			String leftTableNameAndAlias = leftTable + " " + leftAlias;
			String rightTableNameAndAlias = rightTable + " " + rightAlias;

			// COmpute joinOperation
			String joinOperation = null;
			switch (semantic) {
			case EORelationship.LeftOuterJoin:
				// LEFT OUTER JOIN and LEFT JOIN are equivalent in MySQL
				joinOperation = " LEFT JOIN ";
				break;
			case EORelationship.RightOuterJoin:
				// RIGHT OUTER JOIN and RIGHT JOIN are equivalent in MySQL
				joinOperation = " RIGHT JOIN ";
				break;
			case EORelationship.FullOuterJoin:
				throw new IllegalArgumentException("Unfortunately MySQL does not support FULL OUTER JOIN that is specified for " + leftName + " joining " + rightName + "!");
				//jc.op = " FULL OUTER JOIN ";
				//break;
			case EORelationship.InnerJoin:
				// INNER JOIN and JOIN are equivalent in MySQL
				joinOperation = " JOIN ";
				break;
			}

			// Compute joinCondition
			NSArray<EOJoin> joins = r.joins();
			int joinsCount = joins.count();
			NSMutableArray<String> joinStrings = new NSMutableArray<String>(joinsCount);
			for (int i = 0; i < joinsCount; i++) {
				EOJoin currentJoin = joins.objectAtIndex(i);
				String left;
				String right;
				if (CONFIG.ENABLE_IDENTIFIER_QUOTING) {
					left = leftAlias + "." + sqlStringForSchemaObjectName(currentJoin.sourceAttribute().columnName());
					right = rightAlias + "." + sqlStringForSchemaObjectName(currentJoin.destinationAttribute().columnName());
				} else {
					left = leftAlias + "." + currentJoin.sourceAttribute().columnName();
					right = rightAlias + "." + currentJoin.destinationAttribute().columnName();
				}
				joinStrings.addObject(left + " = " + right);
			}
			String joinCondition = " ON " + joinStrings.componentsJoinedByString(" AND ");
			
			JoinClauseDefinition jc = new JoinClauseDefinition(leftTableNameAndAlias, joinOperation, rightTableNameAndAlias, joinCondition, leftTableID);
			if (!_alreadyJoined.containsObject(jc)) {
				_alreadyJoined.insertObjectAtIndex(jc, 0);
			}
			return null;
		}

		/**
		 * Utility that traverses a key path to find the last destination entity
		 * 
		 * @param keyPath
		 *            the key path
		 * @return the entity at the end of the keypath
		 */
		private EOEntity entityForKeyPath(String keyPath) {
			NSArray<String> keys = NSArray.componentsSeparatedByString(keyPath, ".");
			EOEntity ent = entity();

			for (int i = 0; i < keys.count(); i++) {
				String k = keys.objectAtIndex(i);
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
		 * Overriden to not call the super implementation. This simply calls our custom assembleJoinClause
		 * 
		 * @param leftName
		 *            the table name on the left side of the clause
		 * @param rightName
		 *            the table name on the right side of the clause
		 * @param semantic
		 *            the join semantic
		 *            
		 * kieran copied from PostgresqlExpression
		 */
		@Override
		public void addJoinClause(String leftName, String rightName, int semantic) {
			assembleJoinClause(leftName, rightName, semantic);
		}

		/**
		 * Helper class that stores a join definition and helps
		 * <code>MySQLExpression</code> to assemble the correct join
		 * clause.
		 * 
		 * kieran copied from PostgreSQLPlugIn's JoinClause helper class
		 */
		public static final class JoinClauseDefinition {
			private final String _table1;
			private final String _op;
			private final String _table2;
			private final String _joinCondition;
			private final int _leftTableID;
			private final String _toString;

			public JoinClauseDefinition(String leftTableNameAndAlias, String joinOperation, String rightTableNameAndAlias, String joinCondition2, int leftTableID) {
				_table1 = leftTableNameAndAlias;
				_op = joinOperation;
				_table2 = rightTableNameAndAlias;
				_joinCondition = joinCondition2;
				_leftTableID = leftTableID;
				
				_toString = _table1 + _op + _table2 + _joinCondition;
			}

			@Override
			public String toString() {
				return _toString;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == null || !(obj instanceof JoinClauseDefinition)) {
					return false;
				}
				return _toString.equals(obj.toString());
			}

			/* Effective Java #9 : Must override hashCode when overriding equals.
			 * 
			 * (non-Javadoc)
			 * @see java.lang.Object#hashCode()
			 */
			@Override
			public int hashCode() {
				return _toString.hashCode() + 43;
			}

			/**
			 * Property that makes this class "sortable" by left table ID. Needed to correctly
			 * assemble a join clause.
			 * 
			 * @return sort key
			 */
			public int sortKey() {
				return _leftTableID;
			}
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
			statements.addObject(_expressionForString(new StringBuilder().append("CREATE TABLE ").append(pkTable).append(" (NAME CHAR(40) PRIMARY KEY, PK INT)").toString()));
			return statements;
		}
		
		@Override
        public NSArray<EOSQLExpression> statementsToDeleteColumnNamed(String columnName, String tableName, EOSchemaGenerationOptions options) {
            return new NSArray<EOSQLExpression>(_expressionForString(new StringBuilder().append("ALTER TABLE ").append(tableName).append(" DROP COLUMN ").append(columnName).toString()));
        }
		
		@Override
        public NSArray<EOSQLExpression> statementsToInsertColumnForAttribute(EOAttribute attribute, EOSchemaGenerationOptions options) {
            String columnCreationClause = _columnCreationClauseForAttribute(attribute);
            return new NSArray<EOSQLExpression>(_expressionForString(new StringBuilder().append("ALTER TABLE ").append(attribute.entity().externalName()).append(_alterPhraseInsertionClausePrefixAtIndex(0)).append(columnCreationClause).toString()));
        }
		
		@Override
    public NSArray<EOSQLExpression> statementsToModifyColumnNullRule(String columnName, String tableName, boolean allowsNull, EOSchemaGenerationOptions options) {
		    String nullStatement = allowsNull ? " NULL" : " NOT NULL";
		    EOAttribute attribute = attributeInEntityWithColumnName(entityForTableName(tableName), columnName);
		    String externalType = columnTypeStringForAttribute(attribute);
		    return new NSArray<EOSQLExpression>(_expressionForString(new StringBuilder().append("ALTER TABLE ").append(formatTableName(tableName)).append(" MODIFY ").append(formatColumnName(columnName)).append(' ').append(externalType).append(nullStatement).toString()));
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
		    return new NSArray<EOSQLExpression>(_expressionForString(new StringBuilder().append("ALTER TABLE ").append(formatTableName(tableName)).append(" CHANGE ").append(formatColumnName(columnName)).append(' ').append(formatColumnName(newName)).append(' ').append(externalType).append(nullStatement).toString()));
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
			return new NSArray<EOSQLExpression>(_expressionForString(new StringBuilder().append("DROP TABLE ").append(((JDBCAdaptor)adaptor()).plugIn().primaryKeyTableName()).append(" CASCADE").toString()));
		}

		@Override
		public NSArray<EOSQLExpression> _statementsToDropPrimaryKeyConstraintsOnTableNamed(String tableName) {
			return new NSArray<EOSQLExpression>(_expressionForString(new StringBuilder().append("alter table ").append(tableName).append(" drop primary key").toString()));
		}

		@Override
		public NSArray<EOSQLExpression> foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
			return null;
		}

		@Override
		public NSArray<EOSQLExpression> statementsToRenameTableNamed(String tableName, String newName, EOSchemaGenerationOptions options) {
			return new NSArray<EOSQLExpression>(_expressionForString(new StringBuilder().append("rename table ").append(tableName).append(" to ").append(newName).toString()));
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
	public Class<MySQLExpression> defaultExpressionClass() {
		try {
			if (NSProperties.booleanForKey("com.webobjects.jdbcadaptor.MySQLExpression.enable")) {
				return com.webobjects.jdbcadaptor.MySQLPlugIn.MySQLExpression.class;
			}
		} catch (NullPointerException ex) {
			// property was not set
		} 
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
			} finally {
				try { jdbcInfoStream.close(); } catch (IOException e) {}
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
