/*
 PostgresqlPluginBundle v1.2
 Copyright (C) 2001 Kenny Leung

 This bundle is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License version 2.1 as published by the Free Software Foundation.

 This bundle is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

// OS X
import com.apple.cocoa.foundation.*;

// Windows
// import com.apple.yellow.foundation.*;

import com.apple.yellow.eoaccess.*;


public class PostgresqlSynchronizationFactory extends EOSynchronizationFactory {

    public PostgresqlSynchronizationFactory(EOAdaptor adaptor) {
        super(adaptor);
    }

    // BEGIN_BRIDGE
    public PostgresqlSynchronizationFactory() {
        super();
    }
    // END_BRIDGE

    public boolean  supportsSchemaSynchronization() {
        return false;
    }


    public NSArray dropTableStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;

        results = new NSMutableArray();
        count = entityGroup.count();
        for ( i = 0 ; i < count ; i++ ) {
            entity = (EOEntity)entityGroup.objectAtIndex(i);
            if (entity.parentEntity() != null && entity.parentEntity().externalName().equals(entity.externalName())) continue;
            //results.addObject(new PostgresqlExpression("DROP TABLE \"" + entity.externalName().toUpperCase() + "\" CASCADE"));
            //results.addObject(new PostgresqlExpression("DROP TABLE \"" + entity.externalName() + "\" CASCADE"));
            results.addObject(new PostgresqlExpression("DROP TABLE " + entity.externalName() + " CASCADE"));
        }
        return results;
    }

    public NSArray primaryKeyConstraintStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;
        String statement;
        NSArray priKeyAttributes;
        EOAttribute priKeyAttribute;
        int priKeyAttributeCount;
        int j;

        results = new NSMutableArray();
        count = entityGroup.count();
        for ( i = 0 ; i < count ; i++ ) {
            entity = (EOEntity)entityGroup.objectAtIndex(i);
            if (entity.parentEntity() != null && entity.externalName().equals(entity.parentEntity().externalName())) {
                System.out.println("skipping "+entity.name()+", its parent entity has the same table name");
                continue;
            }
            //statement = "ALTER TABLE \"" + entity.externalName() + "\" ADD CONSTRAINT " + entity.externalName().toLowerCase() + "_pk PRIMARY KEY (";
            //statement = "ALTER TABLE \"" + entity.externalName() + "\" ADD CONSTRAINT " + entity.externalName() + "_pk PRIMARY KEY (";
            statement = "ALTER TABLE " + entity.externalName() + " ADD CONSTRAINT " + entity.externalName() + "_pk PRIMARY KEY (";
            priKeyAttributes = entity.primaryKeyAttributes();
            priKeyAttributeCount = priKeyAttributes.count();
            for ( j = 0 ; j < priKeyAttributeCount ; j++ ) {
                priKeyAttribute = (EOAttribute)priKeyAttributes.objectAtIndex(j);
                //statement = statement + "\"" + priKeyAttribute.columnName() + "\"";
                statement = statement + priKeyAttribute.columnName();
                if ( j < priKeyAttributeCount - 1 ) {
                    statement += ", ";
                } else {
                    statement += ")";
                }
            }
            results.addObject(new PostgresqlExpression(statement));

            //now the indexes
            //String table = entity.externalName().toLowerCase();
            String table = entity.externalName();
            for ( j = 0 ; j < priKeyAttributeCount ; j++ ) {
                priKeyAttribute = (EOAttribute)priKeyAttributes.objectAtIndex(j);
                String pk = priKeyAttribute.columnName();

                //String s = "create index "+table+"__"+pk.toLowerCase()+"__pk_index on \""+table.toUpperCase()+"\" (\""+pk+"\")";
                //String s = "create index "+table+"__"+pk+"__pk_index on \""+table+"\" (\""+pk+"\")";
                String s = "create index "+table+"__"+pk+"__pk_index on "+table+" ("+pk+")";
                results.addObject(new PostgresqlExpression(s));
            }
            if (priKeyAttributeCount > 1) {
                //create a compound index, too
                //String s = "create index "+table+"__compound__pk_index on \""+table.toUpperCase()+"\" (";
                //String s = "create index "+table+"__compound__pk_index on \""+table+"\" (";
                String s = "create index "+table+"__compound__pk_index on "+table+" (";
                for ( j = 0 ; j < priKeyAttributeCount ; j++ ) {
                    priKeyAttribute = (EOAttribute)priKeyAttributes.objectAtIndex(j);
                    //s = s + "\"" + priKeyAttribute.columnName() + "\"";
                    s = s + priKeyAttribute.columnName();
                    if ( j < priKeyAttributeCount - 1 ) {
                        s += ", ";
                    } else {
                        s += ")";
                    }
                }
                results.addObject(new PostgresqlExpression(s));
            }
        }
        return results;
    }

    public NSArray primaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;
        NSArray priKeyAttributes;
        EOAttribute priKeyAttribute;
        String sequenceName;

        results = new NSMutableArray();
        count = entityGroup.count();
        for ( i = 0 ; i < count ; i++ ) {
            entity = (EOEntity)entityGroup.objectAtIndex(i);
            priKeyAttributes = entity.primaryKeyAttributes();
            if ( priKeyAttributes.count() == 1 ) {
                // If you have a single primary key, we can add some smarts. If not, you're on your own.
                // We'd like to add comments into the stream, but EOModeler freezes when trying to execute comments to PosgreSQL.
                //results.addObject(new PostgresqlExpression("/* Create primary key support for " + entity.name() + " */"));
                priKeyAttribute = (EOAttribute)priKeyAttributes.objectAtIndex(0);
                results.addObject(new PostgresqlExpression("CREATE FUNCTION ID_MAX() RETURNS " + priKeyAttribute.externalType() + "\n    AS 'SELECT MAX(" + priKeyAttribute.columnName() +") FROM " + entity.externalName() + "'\n    LANGUAGE 'sql'"));
                sequenceName = entity.primaryKeyRootName() + "_SEQ";
                results.addObject(new PostgresqlExpression("CREATE SEQUENCE " + sequenceName));
                results.addObject(new PostgresqlExpression("SELECT SETVAL('" + sequenceName + "', ID_MAX()) INTO TEMP TMP_TABLE"));
                results.addObject(new PostgresqlExpression("DROP TABLE TMP_TABLE"));
                // We'd like to add comments into the stream, but EOModeler freezes when trying to execute comments to PosgreSQL.
                //results.addObject(new PostgresqlExpression("/* End create primary key support for " + entity.name() + " */"));
                results.addObject(new PostgresqlExpression("DROP FUNCTION ID_MAX()"));
            }
        }
        return results;
    }

    public NSArray createTableStatementsForEntityGroup(NSArray nsarray) {
        //for(int i = 0; i < nsarray.count(); i++) {
        //   EOEntity eoentity = (EOEntity)nsarray.objectAtIndex(i);
        //    eoentity.setExternalName("\""+eoentity.externalName()+"\"");
        //}
        NSArray _a = super.createTableStatementsForEntityGroup(nsarray);
        NSMutableArray a = new NSMutableArray();
        a.addObjectsFromArray(_a);
        
        //for(int i = 0; i < nsarray.count(); i++) {
        //    EOEntity eoentity = (EOEntity)nsarray.objectAtIndex(i);
        //    eoentity.setExternalName(eoentity.externalName().substring(1, eoentity.externalName().length() - 1));
        //}

        //now create the additional indexes
        for(int i = 0; i < nsarray.count(); i++) {
            EOEntity eoentity = (EOEntity)nsarray.objectAtIndex(i);
            String table = eoentity.externalName();
            
            NSDictionary userInfo = eoentity.userInfo();
            if (userInfo == null) continue;
            
            String indexes = (String)userInfo.objectForKey("additionalIndexes");
            if (indexes == null) continue;
            
            boolean lockingIndexSet = false;
            NSArray indexesA = NSArray.componentsSeparatedByString(indexes, " ");
            for (int i1 = 0; i1 < indexesA.count(); i1++) {
                String indexesS = (String)indexesA.objectAtIndex(i1);
                if (indexesS.equals("id,log_modified")) lockingIndexSet = true;
                NSArray indexesSA = NSArray.componentsSeparatedByString(indexesS, ",");

                //String indexSQL = "create index "+table.toLowerCase()+"__additional_"+i1+"__index on \""+table.toUpperCase()+"\" (";
                //String indexSQL = "create index "+table+"__additional_"+i1+"__index on \""+table+"\" (";
                String indexSQL = "create index "+table+"__additional_"+i1+"__index on "+table+" (";
                for ( int j = 0 ; j < indexesSA.count() ; j++ ) {
                    String n = ""+indexesSA.objectAtIndex(j);
                    EOAttribute attri = eoentity.anyAttributeNamed(n);
                    if (attri != null) {
                        //the user used the attribute name in the additionalIndexes userInfo entry, not the column name...
                        n = attri.columnName();
                    }
                    //indexSQL = indexSQL + "\"" + n.toUpperCase() + "\"";
                    //indexSQL = indexSQL + "\"" + n + "\"";
                    indexSQL = indexSQL + n;
                    if ( j < indexesSA.count() - 1 ) {
                        indexSQL += ", ";
                    } else {
                        indexSQL += ")";
                    }
                }
                a.addObject(new PostgresqlExpression(indexSQL));
                
            }
            if (!lockingIndexSet) {
                if (eoentity.attributeNamed("id") != null && eoentity.attributeNamed("logModified") != null) {
                    //String s = "create index "+table.toLowerCase()+"__locking_index on \""+table.toUpperCase()+"\" (\"ID\", \"LOG_MODIFIED\")";
                    //String s = "create index "+table+"__locking_index on \""+table+"\" (\"id\", \"log_modified\")";
                    String s = "create index "+table+"__locking_index on "+table+" (id, log_modified)";
                    a.addObject(new PostgresqlExpression(s));
                }
            }
        }
        
        return a;
    }

    public NSArray dropPrimaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
        EOEntity entity;
        int count;
        int i;
        NSMutableArray results;
        String sequenceName;

        results = new NSMutableArray();
        count = entityGroup.count();
        for ( i = 0 ; i < count ; i++ ) {
            entity = (EOEntity)entityGroup.objectAtIndex(i);
            sequenceName = entity.primaryKeyRootName() + "_SEQ";
            results.addObject(new PostgresqlExpression("DROP SEQUENCE " + sequenceName));
        }
        return results;
    }

    public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
        NSArray superResults;
        NSMutableArray results;
        int count;
        int i;
        EOSQLExpression expression;

        results = new NSMutableArray();
        EOEntity entity = relationship.entity();
        superResults = super.foreignKeyConstraintStatementsForRelationship(relationship);
        count = superResults.count();
        for ( i = 0 ; i < count ; i++ ) {
            expression = (EOSQLExpression)superResults.objectAtIndex(i);
            String statement = expression.statement();

            //we are looking for 'ALTER TABLE TABLENAME ADD ...' and want to replace it with 'ALTER "TABLENAME" ADD ...
            //statement = "ALTER TABLE \""+entity.externalName().toUpperCase()+"\" "+statement.substring(statement.indexOf("ADD CON"));

            //statement = "ALTER TABLE \""+entity.externalName()+"\" "+statement.substring(statement.indexOf("ADD CON"));

            //we are looking for 'REFERENCES TABLENAME ('
            //int index = statement.indexOf("REFERENCES "+relationship.destinationEntity().externalName());
            //statement = statement.substring(0, index) + "REFERENCES \"" + relationship.destinationEntity().externalName() + "\"";
            
            //we are lookgin for 'FOREIGN KEY (...)'
            //index = statement.indexOf("FOREIGN KEY (") + "FOREIGN KEY (".length();
            //statement = statement.substring(0, index) + "\"" + statement.substring(index, statement.indexOf(")")) + "\"" + statement.substring(statement.indexOf(")"));

            results.addObject(new PostgresqlExpression(statement + " DEFERRABLE INITIALLY DEFERRED"));
            
            //now the indexes...
            String table = relationship.entity().externalName();
            NSArray sourceAttributes = relationship.sourceAttributes();
            if (sourceAttributes.count() > 1) {
                //do nothing
            } else {
                EOAttribute att = (EOAttribute)sourceAttributes.objectAtIndex(0);
                String fk = att.columnName();
                //String s = "create index "+table.toLowerCase()+"__"+fk.toLowerCase()+"__fk_index on \""+table.toUpperCase()+"\" (\""+fk+"\")";
                String s = "create index "+table+"__"+fk+"__fk_index on "+table+" ("+fk+")";
                results.addObject(new PostgresqlExpression(s));
            }
        }
        return results;
    }

}
