//
//  OpenBasePlugIn.java
//
//  Copyright 2004 OpenBase International Ltd. All rights reserved.
//
package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.synchronization.EOSchemaGeneration;
import com.webobjects.eoaccess.synchronization.EOSchemaGenerationOptions;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronization;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationColumnChanges;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationFactory;
import com.webobjects.eoaccess.synchronization.EOSchemaSynchronizationModelChanges;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import java.util.*;
import java.sql.*;

public class _OpenBasePlugIn extends JDBCPlugIn {
	
	public static class OpenBaseExpression extends JDBCExpression {
		
        /**
         * Fetch spec limit ivar
         */
        private int _fetchLimit;
        
		public OpenBaseExpression(EOEntity entity) {
			super(entity);
			_rtrimFunctionName = null;
		}
		
		public char sqlEscapeChar() {
			return '\0';
		}
		
		public String sqlStringForCaseInsensitiveLike(String valueString, String keyString) {
			return _NSStringUtilities.concat( keyString, " LIKE ", valueString);
		}
		
		public String assembleSelectStatementWithAttributes(NSArray attributes, boolean lock, EOQualifier qualifier, NSArray fetchOrder, String selectString, String columnList, String tableList, String whereClause, String joinClause, String orderByClause, String lockClause) {
			StringBuffer statement = new StringBuffer(2048);
			statement.append(selectString);
			statement.append(columnList);
			statement.append(" FROM ");
			statement.append(tableList);
			if(lockClause != null && lockClause.length() != 0) {
				statement.append(" ");
				statement.append(lockClause);
			}
			if(whereClause != null && whereClause.length() != 0) {
				statement.append(" WHERE ");
				statement.append(whereClause);
			}
			if(joinClause != null && joinClause.length() != 0) {
				if(whereClause != null && whereClause.length() != 0)
					statement.append(" AND ");
				else
					statement.append(" WHERE ");
				statement.append(joinClause);
			}
			if(orderByClause != null && orderByClause.length() != 0) {
				statement.append(" ORDER BY ");
				statement.append(orderByClause);
			}
            if (_fetchLimit != 0) {
                statement.append(" RETURN RESULTS ");
                statement.append(_fetchLimit);
            } 
            return statement.toString();
		}
		
		public String assembleJoinClause(String leftName, String rightName, int semantic) {
			switch(semantic) {
				case EORelationship.FullOuterJoin: // '\001'
					throw new JDBCAdaptorException("OpenBase does not support full outer joins: unable to join " + leftName + " and " + rightName, null);
					
				case EORelationship.LeftOuterJoin: // '\002'
					return leftName + " * " + rightName;
					
				case EORelationship.RightOuterJoin: // '\003'
					throw new JDBCAdaptorException("OpenBase does not support right outer joins: unable to join " + leftName + " and " + rightName, null);
			}
			return super.assembleJoinClause(leftName, rightName, semantic);
		}

        public void prepareSelectExpressionWithAttributes(NSArray nsarray, boolean flag, EOFetchSpecification eofetchspecification) {
            if(!eofetchspecification.promptsAfterFetchLimit()) {
                _fetchLimit = eofetchspecification.fetchLimit();
            }
            super.prepareSelectExpressionWithAttributes(nsarray, flag, eofetchspecification);
        }
        
		// -------------------------
		// CREATE INSERT STATEMENT
		// -------------------------
		
		public void prepareInsertExpressionWithRow(NSDictionary row) {
			EOAttribute attribute;
			Object value;
			for(Enumeration enumeration = row.keyEnumerator(); enumeration.hasMoreElements(); this.addInsertListAttribute(attribute, value)) {
				String attributeName = (String)enumeration.nextElement();
				attribute = this.entity().anyAttributeNamed(attributeName);
				if(attribute == null)
					throw new IllegalStateException("prepareInsertExpressionWithRow: row argument contains key '" + attributeName + "' which does not have corresponding attribute on entity '" + this.entity().name() + "'");
				value = row.objectForKey(attributeName);
			}
            
			String tableList = tableListWithRootEntity(_rootEntityForExpression());
			_statement = this.assembleInsertStatementWithRow(row, tableList, new String(_listString), new String(_valueListString));
		}
		
		// [PJYF Nov 5 2004]
		// This is major Hack to generate a diffenrent bind dictionary for update and insert form the one for select and delete
		public String assembleInsertStatementWithRow(NSDictionary row, String tableList, String columnList, String valueList) {
			if(columnList != null)
				return _NSStringUtilities.concat("INSERT INTO ", tableList, "(", columnList, ")", " VALUES ", "(", valueList, ")");
			else
				return _NSStringUtilities.concat("INSERT INTO ", tableList, " VALUES ", "(", valueList, ")");
		}
		
		// [PJYF Nov 5 2004]
		// This is major Hack to generate a diffenrent bind dictionary for update and insert form the one for select and delete
		public void addInsertListAttribute(EOAttribute attribute, Object value) {
			this.appendItemToListString(this.sqlStringForAttribute(attribute), this._listString());
			String attributeValue = this.sqlStringForForInsertOrUpdateValue(value, attribute.name());
			attributeValue = this.formatSQLString(attributeValue, attribute.writeFormat());
			this.appendItemToListString(attributeValue, _valueList());
		}
		
		// -------------------------
		// CREATE UPDATE STATEMENT
		// -------------------------
		
		public void prepareUpdateExpressionWithRow(NSDictionary row, EOQualifier qualifier) {
			EOAttribute attribute;
			Object value;
			for(Enumeration enumeration = row.keyEnumerator(); enumeration.hasMoreElements(); addUpdateListAttribute(attribute, value)) {
				String attributeName = (String)enumeration.nextElement();
				attribute = this.entity().anyAttributeNamed(attributeName);
				if(attribute == null)
					throw new IllegalStateException("prepareUpdateExpressionWithRow: row argument contains key '" + attributeName + "' which does not have corresponding attribute on entity '" + this.entity().name() + "'");
				value = row.objectForKey(attributeName);
			}
            
			_whereClauseString = EOQualifierSQLGeneration.Support._sqlStringForSQLExpression(qualifier, this);
			String tableList = tableListWithRootEntity(_rootEntityForExpression());
			_statement = assembleUpdateStatementWithRow(row, qualifier, tableList, new String(_listString), _whereClauseString);
		}
		
		public String assembleUpdateStatementWithRow(NSDictionary row, EOQualifier qualifier, String tableList, String updateList, String whereClause) {
			return _NSStringUtilities.concat("UPDATE ", tableList, " SET ", updateList, " WHERE ", whereClause);
		}
		
		// [PJYF Nov 5 2004]
		// This is major Hack to generate a diffenrent bind dictionary for update and insert form the one for select and delete
		public void addUpdateListAttribute(EOAttribute attribute, Object value) {
			String attributeName = this.sqlStringForAttribute(attribute);
			String attributeValue = this.sqlStringForForInsertOrUpdateValue(value, attribute.name());
			attributeValue = this.formatSQLString(attributeValue, attribute.writeFormat());
			this.appendItemToListString(_NSStringUtilities.concat(attributeName, " = ", attributeValue), this._listString());
		}
		
		// [PJYF Nov 5 2004]
		// This is major Hack to generate a diffenrent bind dictionary for update and insert form the one for select and delete
		public String sqlStringForForInsertOrUpdateValue(Object value, String keyPath) {
			EOAttribute attribute = this.entity()._attributeForPath(keyPath);
			if(value != NSKeyValueCoding.NullValue && (useBindVariables() && this.shouldUseBindVariableForAttribute(attribute) || this.mustUseBindVariableForAttribute(attribute))) {
				NSMutableDictionary bindVariableDictionary = this.bindVariableDictionaryForAttribute(attribute, value);
				this.addBindVariableDictionary(bindVariableDictionary);
				return (String)bindVariableDictionary.objectForKey(BindVariablePlaceHolderKey);
			} else {
				return this.formatValueForAttribute(value, attribute);
			}
		}
		
		// [PJYF Nov 5 2004]
		// We need to prepend the question mark with a type attribute to get the jdbc driver to do the right thing.
		// @ (Object) T (Text) or B (Binary)
		public NSMutableDictionary bindVariableDictionaryForInsertOrUpdateAttribute(EOAttribute attribute, Object value) {
			String prepend = "";
			String externalType = ( attribute.externalType() != null ? attribute.externalType().toLowerCase() : "");
			if ( "binary".equals(externalType) ) {
				prepend = "B";
			} else if ( "text".equals(externalType) ) {
				prepend = "T";
			} else if ( "object".equals(externalType) ) {
				prepend = "@";
			}
			return new NSMutableDictionary(new Object[] { attribute.name(), prepend + "?", attribute, value }, new Object[] { BindVariableNameKey, BindVariablePlaceHolderKey, BindVariableAttributeKey, BindVariableValueKey });
		}

		// [PJYF Oct 19 2004]
		// We need to prepend the question mark with a type attribute to get the jdbc driver to do the right thing.
		// only for B (Binary)
		public NSMutableDictionary bindVariableDictionaryForAttribute(EOAttribute attribute, Object value) {
			String prepend = "";
			String externalType = ( attribute.externalType() != null ? attribute.externalType().toLowerCase() : "");
			if ( "binary".equals(externalType) ) {
				prepend = "B";
			}
			return new NSMutableDictionary(new Object[] { attribute.name(), prepend + "?", attribute, value }, new Object[] { BindVariableNameKey, BindVariablePlaceHolderKey, BindVariableAttributeKey, BindVariableValueKey });
		}
	}
	
	public static class OpenBaseSynchronizationFactory extends EOSchemaSynchronizationFactory implements EOSchemaGeneration {
		
		public OpenBaseSynchronizationFactory(EOAdaptor adaptor) {
			super(adaptor);
		}
		
		public NSArray dropPrimaryKeySupportStatementsForEntityGroups(NSArray entityGroups) {
			return new NSArray(this._expressionForString("DROP TABLE " + ((JDBCAdaptor)this.adaptor()).plugIn().primaryKeyTableName() ));
		}
		
		public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
		  System.err.println("trying to run this but we're returning an empty array");
			return NSArray.EmptyArray;
		}
		
		public NSArray primaryKeySupportStatementsForEntityGroups(NSArray entityGroups) {
			String primaryKeyTableName = ((JDBCAdaptor)this.adaptor()).plugIn().primaryKeyTableName();
			NSMutableArray primaryKeyExpressions = new NSMutableArray();
			primaryKeyExpressions.addObject(this._expressionForString("CREATE TABLE " + primaryKeyTableName + " (NAME char(40), PK long)"));
			primaryKeyExpressions.addObject(this._expressionForString("ALTER TABLE " + primaryKeyTableName + " ADD PRIMARY KEY (NAME)"));
			primaryKeyExpressions.addObject(this._expressionForString("CREATE UNIQUE INDEX " + primaryKeyTableName + " NAME"));
			return primaryKeyExpressions.immutableClone();
		}
		
		public String _alterPhraseCoercingColumnsWithNames(NSArray columnNames, NSDictionary updates, NSArray entityGroup, EOSchemaGenerationOptions options) {
		  return this._alterPhraseInsertingColumnsWithNames(columnNames, entityGroup, options);
		}
		
		public String _alterPhraseDeletingColumnsWithNames(NSArray columnNames, NSArray entityGroup, EOSchemaGenerationOptions options) {
			StringBuffer phrase = new StringBuffer();
			int j = columnNames.count();
			for(int i = 0; i < j; i++) {
				phrase.append("" + (i == 0 ? "" : this._alterPhraseJoinString()) + "remove column " + columnNames.objectAtIndex(i));
			}
			return phrase.toString();
		}
		
		public String _alterPhraseInsertionClausePrefixAtIndex(int columnIndex) {
			return "add column";
		}
		
		public String _alterPhraseJoinString() {
			return " ";
		}
		
		/*
		 * [PJYF Oct 19 2004]
		 * This is a bad hack to get WO to create indes for external keys.
		 * We use the primary key constrain generation to create our indexes.
		 * But we need to be carefull not to overwrite previous constrains
		 *
		 */
		protected boolean isSinglePrimaryKeyAttribute(EOAttribute attribute) {
			if (attribute == null) return false;
			EOEntity entity = (EOEntity)attribute.entity();
			if ( (entity == null) || entity.isAbstractEntity() || (entity.externalName() == null) ) return false;
			NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
			if (primaryKeyAttributes.count() != 1) return false;
			return attribute.name().equals(((EOAttribute)primaryKeyAttributes.lastObject()).name());
		}
		
		public NSArray primaryKeyConstraintStatementsForEntityGroup(NSArray entityGroup) {
			if(entityGroup == null) return NSArray.EmptyArray;
			
			NSMutableDictionary columnNameDictionary = new NSMutableDictionary();
			NSMutableArray primaryKeyConstraintExpressions = new NSMutableArray();
			
			for (Enumeration enumerator = entityGroup.objectEnumerator(); enumerator.hasMoreElements(); ) {
			  EOEntity entity = (EOEntity)enumerator.nextElement();
				String tableName = entity.externalName();
				NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
				boolean singlePrimaryKey = primaryKeyAttributes.count() == 1;
				if( (tableName != null) && ( ! "".equals(tableName) ) && (primaryKeyAttributes.count() > 0) ) {
					NSArray expressions = super.primaryKeyConstraintStatementsForEntityGroup(entityGroup);
					if( (expressions != null) && (expressions.count() > 0) ) primaryKeyConstraintExpressions.addObjectsFromArray(expressions);
					for (Enumeration attributeEnumerator = primaryKeyAttributes.objectEnumerator(); attributeEnumerator.hasMoreElements(); ) {
						String columnName = ((EOAttribute)attributeEnumerator.nextElement()).columnName();
						columnNameDictionary.setObjectForKey(columnName, entity.externalName() + "." + columnName);
						EOSQLExpression expression = this._expressionForString("create " + ( singlePrimaryKey ? "unique" : "" ) + " index " + entity.externalName() + " " + columnName);
						if(expression != null) primaryKeyConstraintExpressions.addObject( expression );
					}
				}
			}
			
			for (Enumeration enumerator = entityGroup.objectEnumerator(); enumerator.hasMoreElements(); ) {
				EOEntity entity = (EOEntity)enumerator.nextElement();
				String tableName = entity.externalName();
				if( (tableName != null) && ( ! "".equals(tableName) ) ) {
					for (Enumeration relationshipEnumerator = entity.relationships().objectEnumerator(); relationshipEnumerator.hasMoreElements(); ) {
						EORelationship relationship = (EORelationship)relationshipEnumerator.nextElement();
						if( ! relationship.isFlattened() ) {
							NSArray destinationAttributes = relationship.destinationAttributes();
							
							// First exclude all the destination entity primary keys
							for (Enumeration attributeEnumerator = relationship.destinationEntity().primaryKeyAttributes().objectEnumerator(); attributeEnumerator.hasMoreElements(); ) {
								EOAttribute attribute = (EOAttribute)attributeEnumerator.nextElement();
								columnNameDictionary.setObjectForKey(attribute.columnName(), relationship.destinationEntity().externalName() + "." + attribute.columnName());
							}
							// Then deal with our end of things
							for (Enumeration attributeEnumerator = relationship.sourceAttributes().objectEnumerator(); attributeEnumerator.hasMoreElements(); ) {
								EOAttribute attribute = (EOAttribute)attributeEnumerator.nextElement();
								if( (! this.isSinglePrimaryKeyAttribute(attribute)) && (columnNameDictionary.objectForKey(tableName + "." + attribute.columnName()) != null) ) {
									columnNameDictionary.setObjectForKey(attribute.columnName(), tableName + "." + attribute.columnName());
									EOSQLExpression expression = this._expressionForString("create index " + tableName + " " + attribute.columnName());
									if(expression != null) primaryKeyConstraintExpressions.addObject( expression );
								}
							}
							// Then deal with the other side
							if(entity.model() == relationship.destinationEntity().model()) {
								for (Enumeration attributeEnumerator = relationship.destinationAttributes().objectEnumerator(); attributeEnumerator.hasMoreElements(); ) {
									EOAttribute attribute = (EOAttribute)attributeEnumerator.nextElement();
									String destinationTableName = relationship.destinationEntity().externalName();
									if( (destinationTableName != null) && ( ! "".equals(destinationTableName) ) ) {
										if( (! this.isSinglePrimaryKeyAttribute(attribute)) && (columnNameDictionary.objectForKey(destinationTableName + "." + attribute.columnName()) != null) ) {
											columnNameDictionary.setObjectForKey(attribute.columnName(), destinationTableName + "." + attribute.columnName());
											EOSQLExpression expression = this._expressionForString("create index " + destinationTableName + " " + attribute.columnName());
											if(expression != null) primaryKeyConstraintExpressions.addObject( expression );
										}
										if( (! relationship.isCompound() ) && (relationship.sourceAttributes().count() == 1) && (relationship.destinationAttributes().count() == 1) ) {
											String semantics;
											switch(relationship.joinSemantic()) {
												case EORelationship.FullOuterJoin: // '\001'
												case EORelationship.LeftOuterJoin: // '\002'
												case EORelationship.RightOuterJoin: // '\003'
													semantics = "*";
													break;
													
												default:
													semantics = "=";
													break;
											}
											String sourceColumn = ((EOAttribute)relationship.sourceAttributes().objectAtIndex(0)).columnName();
											String destinationColumn = ((EOAttribute)relationship.destinationAttributes().objectAtIndex(0)).columnName();
											EOSQLExpression expression = this._expressionForString("delete from _SYS_RELATIONSHIP where relationshipName = '" + relationship.name() + "' and source_table = '" + tableName + "' ");
											if(expression != null) primaryKeyConstraintExpressions.addObject( expression );
											expression = this._expressionForString("insert into _SYS_RELATIONSHIP (relationshipName, source_table, source_column, dest_table, dest_column, operator, one_to_many) values ('" + relationship.name() + "','" + tableName + "','" + sourceColumn + "','" + destinationTableName + "','" + destinationColumn + "','" + semantics + "'," + (relationship.isToMany() ? 1 : 0) + ")");
											if(expression != null) primaryKeyConstraintExpressions.addObject( expression );
										}
									}
								}
							}
						}
					}
				}				
			}
			return primaryKeyConstraintExpressions.immutableClone();
		}
		
		public boolean isColumnTypeEquivalentToColumnType(EOSchemaSynchronization.ColumnTypes candidate, EOSchemaSynchronization.ColumnTypes columnType, EOSchemaGenerationOptions options) {
			return candidate.name().equals(columnType.name()) && ( candidate.width() == columnType.width() );
		}
		
		public NSArray statementsToDropForeignKeyConstraintsOnEntityGroups(NSArray entityGroups, NSDictionary changes, NSDictionary options) {
			return NSArray.EmptyArray;
		}
		
		public NSArray statementsToDropPrimaryKeyConstraintsOnEntityGroups(NSArray entityGroups, EOSchemaSynchronizationModelChanges changes, EOSchemaGenerationOptions options) {
			if(entityGroups == null) return NSArray.EmptyArray;
			if(changes == null) changes = newChanges();
			
			NSMutableArray expressions = new NSMutableArray();
			for (Enumeration enumerator = entityGroups.objectEnumerator(); enumerator.hasMoreElements(); ) {
				NSArray entities = (NSArray)enumerator.nextElement();
				EOEntity _last = (EOEntity)entities.lastObject(); //only need entity to get the table name for the group
				String nameInObjectStore = _nameInObjectStoreForEntityGroupWithChangeDictionary(entities, changes.changesForTableNamed(_last.externalName()));
				if ( (nameInObjectStore != null) && ( ! "".equals(nameInObjectStore) ) ) {
					expressions.addObject(this._expressionForString("delete from _SYS_RELATIONSHIP where source_table = '" + nameInObjectStore + "' or dest_table = '" + nameInObjectStore + "'"));
				}
			}
			return expressions.immutableClone();
		}
		
		public NSArray statementsToImplementPrimaryKeyConstraintsOnEntityGroups(NSArray entityGroups, EOSchemaSynchronizationModelChanges changes, EOSchemaGenerationOptions options) {
			NSArray primaryKeyExpressions = this.primaryKeyConstraintStatementsForEntityGroups(entityGroups);
			NSMutableArray createStatements = new NSMutableArray();
			NSMutableArray otherStatements = new NSMutableArray();
			for (Enumeration enumerator = primaryKeyExpressions.objectEnumerator(); enumerator.hasMoreElements(); ) {
				EOSQLExpression expression = (EOSQLExpression)enumerator.nextElement();
				String statement = expression.statement();
				if(statement.startsWith("create")) {
					createStatements.addObject(expression);
				} else if( ! statement.startsWith("delete from _SYS_RELATIONSHIP") ) {
					otherStatements.addObject(expression);
				}
			}
			return createStatements.arrayByAddingObjectsFromArray(otherStatements);
		}
		
		public NSArray statementsToModifyColumnNullRule(String columnName, String tableName, boolean allowsNull, EOSchemaGenerationOptions options) {
			return new NSArray(this._expressionForString("alter table " + tableName + " add column " + columnName + " set " + (allowsNull ? "null" : "not null")));
		}
		
		public NSArray statementsToRenameColumnNamed(String columnName, String tableName, String newName, EOSchemaGenerationOptions options) {
			return new NSArray(this._expressionForString("alter table " + tableName + " rename " + columnName + " to " + newName));
		}
		
		public NSArray statementsToRenameTableNamed(String tableName, String newName, EOSchemaGenerationOptions options) {
			return new NSArray(this._expressionForString("rename " + tableName + " " + newName));
		}
		
		public boolean supportsDirectColumnCoercion() {
			return true;
		}
		
		public boolean supportsDirectColumnDeletion() {
			return true;
		}
		
		public boolean supportsDirectColumnInsertion() {
			return true;
		}
		
		public boolean supportsDirectColumnNullRuleModification() {
			return true;
		}
		
		public boolean supportsDirectColumnRenaming() {
			return true;
		}
		
		public boolean supportsSchemaSynchronization() {
			return true;
		}
		
		public NSArray primaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
			return NSArray.EmptyArray;
		}
		
		public NSArray dropPrimaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
			return NSArray.EmptyArray;
		}
		
		public EOSchemaSynchronizationColumnChanges objectStoreChangesFromAttributeToAttribute(EOAttribute schemaAttribute, EOAttribute modelAttribute) {
		  EOSchemaSynchronizationColumnChanges objectStoreChanges = super.objectStoreChangesFromAttributeToAttribute(schemaAttribute, modelAttribute);
			if(objectStoreChanges.valueForKey("precision") != null || objectStoreChanges.valueForKey("scale") != null) {
				objectStoreChanges.clearPrecision();
				objectStoreChanges.clearScale();
			}
			if( ! modelAttribute.externalType().equals(schemaAttribute.externalType()) ) {
				if(modelAttribute.externalType().equals("varchar") && schemaAttribute.externalType().equals("char"))
				  objectStoreChanges.clearExternalType();
			} else {
				if(schemaAttribute.externalType().equals("object") && objectStoreChanges.valueForKey("width") != null)
					objectStoreChanges.clearWidth();
			}
			if((modelAttribute.externalType().equals("char") || modelAttribute.externalType().equals("varchar")) && modelAttribute.width() == 1024 && schemaAttribute.width() == 1023)
				objectStoreChanges.clearWidth();
			
			return objectStoreChanges;
		}

    public String schemaCreationScriptForEntities(NSArray<EOEntity> arg0, NSDictionary<String, String> arg1) {
      System.err.println("calling OpenBaseSynchronizationFactory.schemaCreationScriptForEntities");
      return null;
    }

    public NSArray<EOSQLExpression> schemaCreationStatementsForEntities(NSArray<EOEntity> arg0, NSDictionary<String, String> arg1) {
      System.err.println("calling OpenBaseSynchronizationFactory.schemaCreationStatementsForEntities");
      return null;
    }
	}
	
	public _OpenBasePlugIn(JDBCAdaptor adaptor) {
		super(adaptor);
	}
	
	public String connectionURL() {
		return super.connectionURL() + ":wo";
	}
	
	public String defaultDriverName() {
		return DriverClassName;
	}
	
	public String databaseProductName() {
		return DriverProductName;
	}
	
	public Class defaultExpressionClass() {
		return _OpenBasePlugIn.OpenBaseExpression.class;
	}
	
	public EOSchemaSynchronizationFactory createSchemaSynchronizationFactory() {
		return new OpenBaseSynchronizationFactory(this.adaptor());
	}
	
	public boolean isPseudoColumnName(String columnName) {
		return columnName.equalsIgnoreCase("_timestamp") || columnName.equalsIgnoreCase("_version") || super.isPseudoColumnName(columnName);
	}
	
	public NSDictionary jdbcInfo() {
		NSDictionary jdbcInfo = super.jdbcInfo();
		JDBCContext jdbccontext = this.adaptor()._cachedAdaptorContext();
		try {
			jdbccontext.connection().commit();
		} catch(SQLException exception) {
			if(NSLog.debugLoggingAllowedForLevelAndGroups(3, 0x0L)) NSLog.debug.appendln(exception);
		}
		return jdbcInfo;
	}
	
	private static final String DriverClassName = "com.openbase.jdbc.ObDriver";
	private static final String DriverProductName = "OpenBase";
}
