//
//  SAPDBPlugIn.java
//  SAPDB_PlugIns
//
//  Created by Wojtek Narczynski on Mon Nov 25 2002.
//  Developed by Marek Janukowicz
//  Copyright (c) 2002 Power Media Sp. z o.o.
//  3/4 rights given up - BSD license.
//

import com.apple.cocoa.foundation.*;
import com.apple.yellow.eoaccess.*;

import java.util.Enumeration;

public class SAPDBPlugIn extends JDBCPlugIn {

    public SAPDBPlugIn(JDBCAdaptor adaptor) {
        super(adaptor);
    }

    public Class defaultExpressionClass() {
        return SAPDBExpression.class;
    }

    public EOSynchronizationFactory createSynchronizationFactory() {
        return new SAPDBPlugIn.SAPDBSynchronizationFactory(_adaptor);
    }

    public String defaultDriverName() {
        return "com.sap.dbtech.jdbc.DriverSapDB";
    }

    public String databaseProductName () {
        return "SapDB";
    }

    public static class SAPDBExpression extends JDBCExpression {

        private static SAPDBExpression _sharedInstance = null;
        private static SAPDBSynchronizationFactory _sharedSyncFactory = null;

        public static EOSQLExpression sharedInstance() {
            if (_sharedInstance == null) {
                _sharedInstance = new SAPDBExpression();
            }
            return _sharedInstance;
        }

        public static EOSynchronizationFactory sharedSyncFactory() {
            if (_sharedSyncFactory == null) {
                _sharedSyncFactory = new SAPDBSynchronizationFactory();
            }
            return _sharedSyncFactory;
        }

        public SAPDBExpression(EOEntity entity) {
            super(entity);
        }

        public SAPDBExpression() {            
            super();
        }

        public Class _synchronizationFactoryClass() {
            return SAPDBSynchronizationFactory.class;
        }

    }


    public static class SAPDBSynchronizationFactory extends EOSynchronizationFactory {

        public SAPDBSynchronizationFactory(EOAdaptor adaptor) {
            super(adaptor);
        }

        public SAPDBSynchronizationFactory() {
            super();
        }

        public boolean  supportsSchemaSynchronization() {
            return true;
        }
        
        public NSArray primaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
            NSMutableArray statements = new NSMutableArray();
            String s = ((EOEntity)entityGroup.lastObject()).primaryKeyRootName() + "_SEQ";
            statements.addObject(_expressionForString("create sequence " + s));
            return statements;
        }

        public NSArray dropPrimaryKeySupportStatementsForEntityGroup(NSArray entityGroup) {
            if(entityGroup == null || entityGroup.count() == 0) {
                return HackedUtils.EmptyArray;
            } else {
                String s = ((EOEntity)entityGroup.lastObject()).primaryKeyRootName() + "_SEQ";
                NSArray statements = new NSArray(_expressionForString("drop sequence " + s));
                return statements;
            }
        }

        //TODO: needs testing (especially multi-key relationships)
        public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship relationship) {
            if(!relationship.isToMany() && _matchesPrimaryKeyAttributes(relationship.destinationEntity(), relationship.destinationAttributes())) {
                
                String tableName = relationship.entity().externalName().toUpperCase();
                
                NSMutableArray statements = new NSMutableArray ();
                StringBuffer stringbuffer = new StringBuffer ();
                stringbuffer.append ("ALTER TABLE ");
                //TODO: maybe the tablename should be quoted
                stringbuffer.append ( tableName );
                stringbuffer.append (" ADD FOREIGN KEY (");

                NSArray sourceAttributes = relationship.sourceAttributes();
                for(int i = 0; i < sourceAttributes.count(); i++) {
                    if(i != 0) stringbuffer.append(", ");
                    stringbuffer.append("\"");
                    stringbuffer.append(((EOAttribute)sourceAttributes.objectAtIndex(i)).columnName().toUpperCase());
                    stringbuffer.append("\"");
                }

                stringbuffer.append(") REFERENCES ");
                stringbuffer.append (relationship.destinationEntity().externalName().toUpperCase());
                stringbuffer.append(" (");
                
                NSArray destinationAttributes = relationship.destinationAttributes();
                for (int j = 0; j < destinationAttributes.count(); j++) {
                    if(j != 0) stringbuffer.append (", ");
                    stringbuffer.append ("\"");
                    stringbuffer.append (((EOAttribute)destinationAttributes.objectAtIndex(j)).columnName().toUpperCase());
                    stringbuffer.append ("\"");
                }
                
                stringbuffer.append(")");
                
                statements.addObject ( _expressionForString (stringbuffer.toString()) );

                //TODO: check if creation of index necessary 

                Enumeration sourceAttrEnumerator = sourceAttributes.objectEnumerator();
                while ( sourceAttrEnumerator.hasMoreElements() ) {
                    
                    EOAttribute attribute = (EOAttribute) sourceAttrEnumerator.nextElement();
                    if ( ! _belongsToPrimaryKeyAttributes ( relationship.entity(), attribute ) ) {
                        //create index only if the attribute is not a part of primary key
                        String fieldName = attribute.columnName().toUpperCase();
                        StringBuffer indexBuffer = new StringBuffer();
                        indexBuffer.append ( "CREATE INDEX " );
                        indexBuffer.append ( tableName+"_"+fieldName+"_INDEX" );
                        indexBuffer.append ( " ON "+tableName );
                        indexBuffer.append (" ( "+fieldName+" )");
                        statements.addObject ( _expressionForString ( indexBuffer.toString() ) );
                    } 
                }
                
                return statements;
                
            } else {
                return HackedUtils.EmptyArray;
            }
        }

        public NSArray createTableStatementsForEntityGroup(NSArray entityGroup) {

            EOSQLExpression expression = null;
            EOEntity entity = null;

            if ( entityGroup == null || entityGroup.count() == 0 ) return HackedUtils.EmptyArray;
            StringBuffer buffer = new StringBuffer();
            expression = _expressionForEntity((EOEntity)entityGroup.lastObject());

            entity = (EOEntity)entityGroup.lastObject();
            boolean flag = true;
            
            if ( entity.attributes() != null ) {

                Enumeration attributesEnumerator = entity.attributes().objectEnumerator();
                while ( attributesEnumerator.hasMoreElements() ) {

                    EOAttribute attribute = (EOAttribute)attributesEnumerator.nextElement();
                    String s = attribute.columnName();
                    if (!attribute.isDerived() && !attribute.isFlattened() && s != null && s.length() > 0 ) {
                        if( flag) {
                            buffer.append(" (\n\t");
                            flag = false;
                        } else buffer.append(",\n\t");
                        buffer.append ( addCreateClauseForAttribute ( attribute ));
                    }

                }
            }

            StringBuffer createTableBuffer = new StringBuffer();
            createTableBuffer.append("CREATE TABLE ");
            createTableBuffer.append(entity.externalName());
            createTableBuffer.append(buffer.toString());
            createTableBuffer.append("\n)");
            expression.setStatement(createTableBuffer.toString());
            return new NSArray ( expression );
        }
        

        public StringBuffer addCreateClauseForAttribute(EOAttribute attribute) {
                      
            StringBuffer buffer = new StringBuffer();
            buffer.append("\"");
            buffer.append(attribute.columnName());
            buffer.append("\" ");
            buffer.append(columnTypeStringForAttribute(attribute));
            NSDictionary userInfoDictionary = attribute.userInfo();
            if(userInfoDictionary == null) {
                buffer.append(attribute.allowsNull() ? "" : " NOT NULL");
                return buffer;
            }
            return buffer;
        }
        

        public String columnTypeStringForAttribute(EOAttribute attribute) {
            String s = attribute.externalType();
            NSDictionary dictionary = JDBCAdaptor.typeInfoForModel(((EOEntity)attribute.parent()).model());
            NSDictionary typeInfoDictionary = (NSDictionary)dictionary.objectForKey(s);
            if ( typeInfoDictionary == null )
                throw new JDBCAdaptorException
                    ("Unable to find type information for external type '" + s + "' in attribute '" + attribute.name() + "' of entity '"
                     + ((EOEntity)attribute.parent()).name() + "'.  Check spelling and capitalization.", null);
            
            int i;
            try {
                i = Integer.parseInt((String)typeInfoDictionary.objectForKey("createParams"));
            } catch(NumberFormatException numberformatexception) {
                i = 0;
            }
            
            switch(i) {
                case 2: 
                    int j = attribute.precision();
                    if (j == 0) return attribute.externalType();
                        
                    int k = attribute.scale();
                    if ( k == 0 ) return _columnTypeStringForExternalTypeAndParameters ( attribute.externalType(),j,0 );
                    else return _columnTypeStringForExternalTypeAndParameters ( attribute.externalType(),j,k );

                case 1: 
                    int l = attribute.width();
                    if(l == 0) l = attribute.precision();
                        
                    if(l == 0) return attribute.externalType();
                    else return _columnTypeStringForExternalTypeAndParameters ( attribute.externalType(),l,0 );
            }
            return attribute.externalType();
        }

        private String _columnTypeStringForExternalTypeAndParameters ( String externalType, int firstParam, int secondParam ) {
            String parameterString = secondParam == 0 ? "(" + firstParam + ")" : "(" + firstParam + "," +secondParam + ")";
            StringBuffer buffer  = new StringBuffer ( externalType );
            int beginParenIndex = externalType.indexOf ( '(' );
            int endParenIndex = externalType.indexOf ( ')' );
            //TODO: check if ( OR ) absent - theoretically impossible, but better be sure
            if ( beginParenIndex != -1 && endParenIndex != -1 ) buffer.replace ( beginParenIndex,endParenIndex+1, parameterString );
            else buffer.append ( parameterString );
            return buffer.toString ();
        }
        
            
        private boolean _matchesPrimaryKeyAttributes(EOEntity entity, NSArray nsarray) {
            NSArray nsarray1 = entity.primaryKeyAttributes();
            if ( nsarray.count() != nsarray1.count() ) return false;
            for(int i = 0; i < nsarray1.count(); i++) {
                if ( ! nsarray.containsObject ( nsarray1.objectAtIndex (i) ) ) return false;                
            }

            return true;                     
        }

        private boolean _belongsToPrimaryKeyAttributes ( EOEntity entity, EOAttribute attribute ) {
            return entity.primaryKeyAttributes().containsObject ( attribute );
        }
        
    }

}
