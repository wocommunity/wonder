//
//  SAPDBPlugIn.java
//  SAPDB_PlugIns
//
//  Created by Wojtek Narczynski on Mon Nov 25 2002.
//  Copyright (c) 2002 Power Media Sp. z o.o.
//  3/4 rights given up - BSD license.
//

package com.webobjects.jdbcadaptor;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;


public class SAPDBPlugIn extends JDBCPlugIn{

 public SAPDBPlugIn( JDBCAdaptor theAdaptor) {
        super( theAdaptor );
        
        //TODO Remove this debug
        System.out.println( databaseProductName() );
    }
    
    
    
    /**
    * Returns a string identifying the database.
    */
    // TODO Check if it makes sense to override
    // Current value printed in constructor
    //public String databaseProductName()
    


    /**
    * Returns a fully qualified name of the driver class that this
    * plugin prefers to use. The adaptor will attempt to load this
    * class when making a connection.
    */
    public String defaultDriverName() {
        return "com.sap.dbtech.jdbc.DriverSapDB";
    }
    
    
    /** 
    * Default returns "EO_PK_TABLE".  Subclasses typically don't override
    * this.  See also newPrimaryKeys().
    */
    //TODO - See if this doesn't throw in normal usage
    //public String primaryKeyTableName() {
    // throw new IllegalStateException( "SAPDB PlugIn is supposed to use SEQUENCE");
    //}
    
    public NSArray newPrimaryKeys( int keysNeeded, EOEntity theEntity, JDBCChannel theChannel) {
    
        // Get primary key attrs array...
        NSArray pkAttrArray = theEntity.primaryKeyAttributes();
        // ...and check if the PK is not compound
        if ( pkAttrArray == null || pkAttrArray.count() != 1)
            return null;
            
        // Get primary key attribute...
        EOAttribute pkAttr = (EOAttribute) pkAttrArray.objectAtIndex(0);
        // ...and check if it is number
        if (pkAttr.adaptorValueType() != EOAttribute.AdaptorNumberType)
            return null;
            
        // Build SQL expression
        String query = "SELECT " + theEntity.primaryKeyRootName() + "_SEQ.NEXTVAL FROM DUAL";
        EOSQLExpression expression = expressionFactory().expressionForString( query );
        
        NSMutableArray generatedKeys = new NSMutableArray( keysNeeded );
        
        for ( int ii = 0; ii < keysNeeded; ii++) {
        
            // Do the actual fetch
            NSArray keyArray = theChannel._fetchRowsForSQLExpressionAndAttributes( expression, pkAttrArray);
            
            // Escape if something went wrong
            if ( keyArray == null || keyArray.count() == 0)
                return null;
            
            generatedKeys.addObject( keyArray.lastObject() );
        
        }
        // Things went well if we are still here
        return generatedKeys;
    }

}
