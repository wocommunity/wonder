package er.cayenne;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.DeleteRule;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;
import org.apache.cayenne.util.Util;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOSortOrdering;

import er.extensions.eof.ERXGenericRecord;

/*
 * ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0
 * 
 * Copyright (c) 2006 The ObjectStyle Group and individual authors of the
 * software. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowlegement: "This product includes software
 * developed by the ObjectStyle Group (http://objectstyle.org/)." Alternately,
 * this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse or
 * promote products derived from this software without prior written permission.
 * For written permission, please contact andrus@objectstyle.org.
 * 
 * 5. Products derived from this software may not be called "ObjectStyle" nor
 * may "ObjectStyle" appear in their names without prior written permission of
 * the ObjectStyle Group.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * OBJECTSTYLE GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the ObjectStyle Group. For more information on the ObjectStyle
 * Group, please see <http://objectstyle.org/>.
 *  
 */

/**
 * 
 * <p>CayenneModeler (which comes with Cayenne) includes a tool to convert EOModels to Cayenne models: create a new project and then choose Tools -> "Import EOModel".<br>
 * I suggest trying that first. In my experience it didn't work well because the prototypes in ERPrototypes were not resolved.</p>
 *
 * <p>This framework will allow you to convert an EOModel to a Cayenne model.</p>
 *
 * <p>To use it just add the framework to your build path and then add this line to you application's constructor (replace MyModel with the name of your model):<br>
 * 
 * new er.cayenne.CayenneConverter().run(EOModelGroup.defaultGroup().modelNamed("MyModel"));</p>
 *
 * <p>Run your WO app.<br>
 * This will create a Cayenne DataMap file (called MyModel.map.xml) in the root of your Sources folder. <br>
 * To use it you will need to run CayenneModeler and create a new project.<br>
 * Then give a name to the DataDomain (top-level) node that is created in the new project<br>
 * Then choose File -> Import DataMap and select the .map.xml file that was generated.</p>
 *
 * <p>The converter does not copy the connection dictionary from your model - you will need to re-enter that information by creating a DataNode using CayenneModeler.</p>
 *
 * <p>The converter attempts to convert qualifiers for any fetch specifications you've defined in your model, but this should be considered just a best attempt, not guaranteed to be correct.</p>
 * 
 */
public class CayenneConverter {

	public static void main(String[] args) {
		try {
			EOModelGroup.defaultGroup().addModelWithPath(args[0]);
			new CayenneConverter().run(EOModelGroup.defaultGroup().modelWithPath(args[0]));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts an EOModel to a Cayenne model / project. See the class docs for more info.
	 * @param model
	 */
	public void run(EOModel model) {
		try {
			DataMap dataMap = new DataMap(model.name());
			String sampleEntityClassName = model.entities().get(0).className();
			if (sampleEntityClassName.contains(".")) {
				sampleEntityClassName = sampleEntityClassName.substring(0, sampleEntityClassName.lastIndexOf('.'));
			}
			dataMap.setDefaultPackage(sampleEntityClassName);
			dataMap.setDefaultSuperclass(ERXGenericRecord.class.getName());
			for (EOEntity entity : model.entities()) {
				convertEntity(dataMap, entity);
			}
			
			File projectFolder;
			if (model.path().contains(".woa")) {
				projectFolder = new File(model.path())
					.getParentFile()  // Resources
					.getParentFile()  // Contents
					.getParentFile()  // App.woa
					.getParentFile()  // build
					.getParentFile(); // project folder
			} else if (model.path().contains(".framework")) {
				projectFolder = new File(model.path())
					.getParentFile()  // Resources
					.getParentFile()  // App.framework
					.getParentFile()  // build
					.getParentFile(); // project folder
			} else {
				projectFolder = new File(model.path())
					.getParentFile()  // Resources
					.getParentFile(); // project folder
			}
			
			File sourcesFolder = new File(projectFolder.getAbsolutePath(), "Sources");
			File newModelFile = new File(sourcesFolder.getAbsolutePath(), model.name() + ".map.xml");
			try (PrintWriter writer = new PrintWriter(newModelFile, "UTF8")) {
				dataMap.encodeAsXML(writer);
			}
			
			System.err.println("\nWrote cayenne map (model) file to: " +  newModelFile.getCanonicalPath() + "\n");
			
//			DataNode dataNode = new DataNode(model.name());
//			dataNode.setSchemaUpdateStrategy(new SkipSchemaUpdateStrategy());
//			
//			DriverDataSource dataSource = (DriverDataSource) dataNode.getDataSource();
//			dataSource.setConnectionUrl((String) model.connectionDictionary().get("URL"));
//			dataSource.setDriverClassName((String)model.connectionDictionary().get("driver"));
//			dataSource.setUserName((String)model.connectionDictionary().get("username"));
//			dataSource.setPassword((String)model.connectionDictionary().get("password"));
//			
//			dataNode.addDataMap(dataMap);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String dbEntityName(EOEntity entity) {
		String dbEntityName = entity.externalName();
		
		if (entity.name().equalsIgnoreCase(entity.externalName())) {
			dbEntityName = entity.name(); // make the DbEntity match the case
		} else if ((entity.name() + "s").equalsIgnoreCase(entity.externalName())) {
			dbEntityName = entity.name() + "s"; // make the DbEntity match the case
		}
		
		return dbEntityName;
	}
	
	private void convertEntity(DataMap dataMap, EOEntity entity) {
		DbEntity dbEntity = new DbEntity(dbEntityName(entity));
		dbEntity.setDataMap(dataMap);
		dataMap.addDbEntity(dbEntity);


		ObjEntity objEntity = new ObjEntity(entity.name());
		objEntity.setDbEntity(dbEntity);
		objEntity.setClassName(entity.className());
		objEntity.setClientClassName(entity.clientClassName());
		objEntity.setSuperClassName("er.extensions.eof.ERXGenericRecord");
		objEntity.setAbstract(entity.isAbstractEntity());
		objEntity.setReadOnly(entity.isReadOnly());
		objEntity.setDataMap(dataMap);
		objEntity.setDeclaredLockType(ObjEntity.LOCK_TYPE_OPTIMISTIC);
		dataMap.addObjEntity(objEntity);
		
		for (EOAttribute attribute : entity.attributes()) {
			convertAttribute(entity, dbEntity, objEntity, attribute);
		}
		
		for (EORelationship relationship : entity.relationships()) {
			convertRelationship(entity, dbEntity, objEntity, relationship);
		}
		
		for (String fetchSpecName : entity.fetchSpecificationNames()) {
			convertFetchSpecification(entity, dataMap, fetchSpecName);
		}
	}

	private void convertAttribute(EOEntity entity, DbEntity dbEntity, ObjEntity objEntity, EOAttribute attribute) {
		DbAttribute dbAttribute = null;
		if (!attribute.isDerived()) {
			dbAttribute = new DbAttribute(attribute.columnName());
			dbAttribute.setEntity(dbEntity);
			String javaClass = getJavaClassName(attribute);
			int jdbcType = getSqlTypeByJava(javaClass);
			if (jdbcType == NOT_DEFINED) {
				if (javaClass.endsWith("Boolean") && attribute.width() == 5) {
					jdbcType = Types.VARCHAR;
				} else {
					System.out.println("Unable to find JDBC type for attribute: " + attribute);
				}
			}
			dbAttribute.setType(jdbcType);
			dbAttribute.setMaxLength(attribute.width());
			if (attribute.precision() != 0) {
				dbAttribute.setMaxLength(attribute.precision());
			}
			dbAttribute.setScale(attribute.scale());
			dbAttribute.setMandatory(!attribute.allowsNull());
			dbAttribute.setPrimaryKey(entity.primaryKeyAttributes().contains(attribute));
			dbEntity.addAttribute(dbAttribute);
		}
		
		if (entity.classPropertyNames().contains(attribute.name())) {
			ObjAttribute objAttribute = new ObjAttribute(attribute.name());
			objAttribute.setDbAttributePath(dbAttribute.getName());
			objAttribute.setEntity(objEntity);
			objAttribute.setType(getJavaClassName(attribute)); // .replaceFirst("^java.lang.", "")
			objAttribute.setUsedForLocking(entity.attributesUsedForLocking().contains(attribute));
			objEntity.addAttribute(objAttribute);
		}
	}
	
	private void convertRelationship(EOEntity entity, DbEntity dbEntity, ObjEntity objEntity, EORelationship relationship) {
		DbRelationship dbRelationship = new DbRelationship(relationship.name());
		dbRelationship.setSourceEntity(dbEntity);
		dbRelationship.setTargetEntityName(dbEntityName(relationship.destinationEntity()));
		dbRelationship.setToMany(relationship.isToMany());
		dbRelationship.setToDependentPK(relationship.propagatesPrimaryKey());
		dbEntity.addRelationship(dbRelationship);
		// isMandatory?
		// relationship.joinSemantic() ?

		for (EOJoin join : relationship.joins()) {
			DbJoin dbJoin = new DbJoin(dbRelationship, join.sourceAttribute().columnName(), join.destinationAttribute().columnName());
			dbRelationship.addJoin(dbJoin);
		}
		
		if (entity.classPropertyNames().contains(relationship.name())) {
			ObjRelationship objRelationship = new ObjRelationship(relationship.name());
			objRelationship.setSourceEntity(objEntity);
			objRelationship.setTargetEntityName(relationship.destinationEntity().name());
			objRelationship.setUsedForLocking(entity.attributesUsedForLocking().containsAll(relationship.sourceAttributes()));
			objRelationship.setDbRelationshipPath(relationship.name());
			
			int deleteRule = 0;
			switch (relationship.deleteRule()) {
			case EOClassDescription.DeleteRuleCascade:
				deleteRule = DeleteRule.CASCADE;
				break;
			case EOClassDescription.DeleteRuleDeny:
				deleteRule = DeleteRule.DENY;
				break;
			case EOClassDescription.DeleteRuleNoAction:
				deleteRule = DeleteRule.NO_ACTION;
				break;
			case EOClassDescription.DeleteRuleNullify:
				deleteRule = DeleteRule.NULLIFY;
				break;
			}
			objRelationship.setDeleteRule(deleteRule);
			
			if (relationship.isToMany()) {
				objRelationship.setDeleteRule( DeleteRule.CASCADE);
			}
			
			objEntity.addRelationship(objRelationship);
		}
	}

	private void convertFetchSpecification(EOEntity entity, DataMap dataMap, String fetchSpecName) {
		EOFetchSpecification fetchSpec = entity.fetchSpecificationNamed(fetchSpecName);

		SelectQuery query = new SelectQuery(dataMap.getObjEntity(entity.name()));
		query.setName(fetchSpecName);

		query.setDistinct(fetchSpec.usesDistinct());
		query.setFetchingDataRows(fetchSpec.fetchesRawRows());
		
		String qualString = fetchSpec.qualifier().toString();
		qualString = qualString.replace(" caseinsensitivelike ", " likeIgnoreCase ");
		qualString = qualString.replace(" caseInsensitiveLike ", " likeIgnoreCase ");
		qualString = qualString.replace(" AND ", " and ");
		qualString = qualString.replace(" OR ", " or ");
		if (qualString.contains(" like ") || qualString.contains(" likeIgnoreCase ")) {
			qualString = qualString.replace("*", "%");
		}
		
		try {
			query.setQualifier(Expression.fromString(qualString));
		} catch (Exception e) {
			System.out.println("unable to parse qualifier for fetchSpec '" + fetchSpecName + "'. qual=" + qualString + "\n" + e.getMessage());
		}
		query.setFetchLimit(fetchSpec.fetchLimit());
		//query.setResolvingInherited(fetchSpec.isDeep());
		query.setStatementFetchSize(fetchSpec.fetchLimit());
		
		for (String	keyPath : fetchSpec.prefetchingRelationshipKeyPaths()) {
			query.addPrefetch(keyPath);
		}
		
		for (EOSortOrdering sortOrdering : fetchSpec.sortOrderings()) {
			SortOrder order = SortOrder.ASCENDING;
			if (sortOrdering.selector().equals(EOSortOrdering.CompareCaseInsensitiveAscending)) {
				order = SortOrder.ASCENDING_INSENSITIVE;
			} else if (sortOrdering.selector().equals(EOSortOrdering.CompareCaseInsensitiveDescending)) {
				order = SortOrder.DESCENDING_INSENSITIVE;
			} else if (sortOrdering.selector().equals(EOSortOrdering.CompareDescending)) {
				order = SortOrder.DESCENDING;
			}
			query.addOrdering(sortOrdering.key(), order);
		}
		
		dataMap.addQuery(query);
	}
	
	public String getJavaClassName(EOAttribute attr) {
		String className = attr.valueTypeClassName();

		if ("java.lang.Number".equals(className) || "Number".equals(className) || "NSNumber".equals(className)) {
			String valueType = attr.valueType();
			if (valueType == null || valueType.length() == 0) {
				className = java.lang.Integer.class.getName();
			} else if ("B".equals(valueType)) {
				className = java.math.BigDecimal.class.getName();
			} else if ("b".equals(valueType)) {
				className = java.lang.Byte.class.getName();
			} else if ("d".equals(valueType)) {
				className = java.lang.Double.class.getName();
			} else if ("f".equals(valueType)) {
				className = java.lang.Float.class.getName();
			} else if ("i".equals(valueType)) {
				className = java.lang.Integer.class.getName();
			} else if ("l".equals(valueType)) {
				className = java.lang.Long.class.getName();
			} else if ("s".equals(valueType)) {
				className = java.lang.Short.class.getName();
			} else if ("c".equals(valueType)) {
				className = java.lang.Boolean.class.getName();
			}
		} else if ("NSString".equals(className)) {
			className = java.lang.String.class.getName();
		} else if ("NSCalendarDate".equals(className) || "com.webobjects.foundation.NSTimestamp".equals(className)) {
			className = java.sql.Timestamp.class.getName(); // "com.webobjects.foundation.NSTimestamp";
		} else if ("NSDecimalNumber".equals(className)) {
			String valueType = attr.valueType();
			if (valueType == null || valueType.length() == 0) {
				className = java.lang.Integer.class.getName();
			} else {
				className = java.math.BigDecimal.class.getName();
			}
		}
		return className;
	}
	
	
	/*****************************************************************
	 *  
	 *  CODE BELOW IS COPIED FROM org.apache.cayenne.dba.TypesMapping.
	 *  WITH THE FOLLOWING LICENSE:
	 *  
	 *  Licensed to the Apache Software Foundation (ASF) under one
	 *  or more contributor license agreements.  See the NOTICE file
	 *  distributed with this work for additional information
	 *  regarding copyright ownership.  The ASF licenses this file
	 *  to you under the Apache License, Version 2.0 (the
	 *  "License"); you may not use this file except in compliance
	 *  with the License.  You may obtain a copy of the License at
	 *
	 *    http://www.apache.org/licenses/LICENSE-2.0
	 *
	 *  Unless required by applicable law or agreed to in writing,
	 *  software distributed under the License is distributed on an
	 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	 *  KIND, either express or implied.  See the License for the
	 *  specific language governing permissions and limitations
	 *  under the License.
	 ****************************************************************/
	
    /**
     * Returns default java.sql.Types type by the Java type name.
     * 
     * @param className Fully qualified Java Class name.
     * @return The SQL type or NOT_DEFINED if no type found.
     */
    public static int getSqlTypeByJava(String className) {
        if (className == null) {
            return NOT_DEFINED;
        }

        Integer type = javaSqlEnum.get(className);
        if (type != null) {
            return type.intValue();
        }

        // try to load a Java class - some nonstandard mappings may work

        Class<?> aClass;
        try {
            aClass = Util.getJavaClass(className);
        }
        catch (Throwable th) {
            return NOT_DEFINED;
        }

        return getSqlTypeByJava(aClass);
    }
    
    /**
     * Guesses a default JDBC type for the Java class.
     * 
     * @since 1.1
     */
    public static int getSqlTypeByJava(Class<?> javaClass) {
        if (javaClass == null) {
            return NOT_DEFINED;
        }

        // check standard mapping of class and superclasses
        Class<?> aClass = javaClass;
        while (aClass != null) {

            String name;

            if (aClass.isArray()) {
                name = aClass.getComponentType().getName() + "[]";
            }
            else {
                name = aClass.getName();
            }

            Object type = javaSqlEnum.get(name);
            if (type != null) {
                return ((Number) type).intValue();
            }

            aClass = aClass.getSuperclass();
        }

        // check non-standard JDBC types that are still supported by JPA
        if (javaClass.isArray()) {

            Class<?> elementType = javaClass.getComponentType();
            if (Character.class.isAssignableFrom(elementType)
                    || Character.TYPE.isAssignableFrom(elementType)) {
                return Types.VARCHAR;
            }
            else if (Byte.class.isAssignableFrom(elementType)
                    || Byte.TYPE.isAssignableFrom(elementType)) {
                return Types.VARBINARY;
            }
        }

        if (Calendar.class.isAssignableFrom(javaClass)) {
            return Types.TIMESTAMP;
        }
        else if (BigInteger.class.isAssignableFrom(javaClass)) {
            return Types.BIGINT;
        }
        // serializable check should be the last one when all other mapping attempts
        // failed
        else if (Serializable.class.isAssignableFrom(javaClass)) {
            return Types.VARBINARY;
        }

        return NOT_DEFINED;
    }
    
    // Never use "-1" or any other normal integer, since there
    // is a big chance it is being reserved in java.sql.Types
    public static final int NOT_DEFINED = Integer.MAX_VALUE;
    
    // char constants for Java data types
    public static final String JAVA_LONG = "java.lang.Long";
    public static final String JAVA_BYTES = "byte[]";
    public static final String JAVA_BOOLEAN = "java.lang.Boolean";
    public static final String JAVA_STRING = "java.lang.String";
    public static final String JAVA_SQLDATE = "java.sql.Date";
    public static final String JAVA_UTILDATE = "java.util.Date";
    public static final String JAVA_BIGDECIMAL = "java.math.BigDecimal";
    public static final String JAVA_DOUBLE = "java.lang.Double";
    public static final String JAVA_FLOAT = "java.lang.Float";
    public static final String JAVA_INTEGER = "java.lang.Integer";
    public static final String JAVA_SHORT = "java.lang.Short";
    public static final String JAVA_BYTE = "java.lang.Byte";
    public static final String JAVA_TIME = "java.sql.Time";
    public static final String JAVA_TIMESTAMP = "java.sql.Timestamp";
    public static final String JAVA_BLOB = "java.sql.Blob";
    
    /**
     * Keys: java class names, Values: SQL int type definitions from java.sql.Types
     */
    private static final Map<String, Integer> javaSqlEnum = new HashMap<>();

    static {
        javaSqlEnum.put(JAVA_LONG, Integer.valueOf(Types.BIGINT));
        javaSqlEnum.put(JAVA_BYTES, Integer.valueOf(Types.BINARY));
        javaSqlEnum.put(JAVA_BOOLEAN, Integer.valueOf(Types.BIT));
        javaSqlEnum.put(JAVA_STRING, Integer.valueOf(Types.VARCHAR));
        javaSqlEnum.put(JAVA_SQLDATE, Integer.valueOf(Types.DATE));
        javaSqlEnum.put(JAVA_UTILDATE, Integer.valueOf(Types.DATE));
        javaSqlEnum.put(JAVA_TIMESTAMP, Integer.valueOf(Types.TIMESTAMP));
        javaSqlEnum.put(JAVA_BIGDECIMAL, Integer.valueOf(Types.DECIMAL));
        javaSqlEnum.put(JAVA_DOUBLE, Integer.valueOf(Types.DOUBLE));
        javaSqlEnum.put(JAVA_FLOAT, Integer.valueOf(Types.FLOAT));
        javaSqlEnum.put(JAVA_INTEGER, Integer.valueOf(Types.INTEGER));
        javaSqlEnum.put(JAVA_SHORT, Integer.valueOf(Types.SMALLINT));
        javaSqlEnum.put(JAVA_BYTE, Integer.valueOf(Types.SMALLINT));
        javaSqlEnum.put(JAVA_TIME, Integer.valueOf(Types.TIME));
        javaSqlEnum.put(JAVA_TIMESTAMP, Integer.valueOf(Types.TIMESTAMP));

        // add primitives
        javaSqlEnum.put("byte", Integer.valueOf(Types.TINYINT));
        javaSqlEnum.put("int", Integer.valueOf(Types.INTEGER));
        javaSqlEnum.put("short", Integer.valueOf(Types.SMALLINT));
        javaSqlEnum.put("char", Integer.valueOf(Types.CHAR));
        javaSqlEnum.put("double", Integer.valueOf(Types.DOUBLE));
        javaSqlEnum.put("long", Integer.valueOf(Types.BIGINT));
        javaSqlEnum.put("float", Integer.valueOf(Types.FLOAT));
        javaSqlEnum.put("boolean", Integer.valueOf(Types.BIT));
    }
    
}
