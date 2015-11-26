package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.*;
import com.webobjects.jdbcadaptor.*;
import com.webobjects.foundation.*;



public class OpenBasePKPlugIn extends _OpenBasePlugIn{

	public OpenBasePKPlugIn(JDBCAdaptor adaptor){
		super(adaptor);
		//System.out.println("In OpenBasePKPlugIn constructor...: ");
	}
	
	public NSArray newPrimaryKeys(int count, EOEntity entity, JDBCChannel adaptorChannel){
//		System.out.println("newPrimaryKeys called with: Count -> " + count + " EOEntity -> " + entity);
		NSMutableArray result = new NSMutableArray();
		if(count > 0){
		// Make sure the chonnel is open
			if ( !adaptorChannel.isOpen() ) {
				adaptorChannel.openChannel();
			}
		// Get the external table name
			String tableName = entity.externalName();
		// Get the primary key attributes
			NSArray primaryKeyAttributeNamesArray = entity.primaryKeyAttributes();
			
		// If NEWID isn't appropriate, fall back to the super class's implementation
			if (primaryKeyAttributeNamesArray.count() != 1) {
			    return super.newPrimaryKeys(count, entity, adaptorChannel);
			}
			
			EOAttribute column = (EOAttribute)primaryKeyAttributeNamesArray.objectAtIndex(0);
			String attributeName = column.name();	// the EOF name
		// Get the external primarey key name
			String columnName = column.columnName(); // the database name
			
			
			NSMutableDictionary row;
		// Loop to get count new PKs
			while(0 < count) {
			// Create the SQL query to get a new PK
			  String newidSQL;
			  
				if (count > 100) {
					newidSQL = "NEWID FOR " + tableName + " " + columnName + " 100 ";
				} else {
					newidSQL = "NEWID FOR " + tableName + " " + columnName + " " + count;
				}

				adaptorChannel.evaluateExpression(adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForString(newidSQL));
				    adaptorChannel.setAttributesToFetch(adaptorChannel.describeResults());
				// if success do this....
				while ( (row = adaptorChannel.fetchRow()) != null ) {
					Object newPK = row.objectForKey(columnName.toUpperCase());
					result.addObject(new NSDictionary(newPK, attributeName));
					count = count - 1;
				}
				// otherwise, if there is an SQL error, break out of the loop
			}
			if (count!=0) {
				//create a global variable to hold the unique key
				String varName = "unique_" + tableName + "_" + columnName;
				String newidSQL = "INCREMENT " + varName;
				
				while(0 < count) {
					adaptorChannel.evaluateExpression(adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForString(newidSQL));
						adaptorChannel.setAttributesToFetch(adaptorChannel.describeResults());
						
					if ( (row = adaptorChannel.fetchRow()) != null ) {
						Object newPK = row.objectForKey(columnName.toUpperCase());
						result.addObject(new NSDictionary(newPK, attributeName));
						count = count - 1;
					} else {
						String createSQL = "DECLARE GLOBAL " + varName + " AS INTEGER INITIALIZE 1 SILENT ";
						String messageSQL = "ALERT WARNING 'WO PK Generation: Initialize global variable " + varName + " with seed value at system startup.";
						adaptorChannel.evaluateExpression(adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForString(createSQL));
						adaptorChannel.evaluateExpression(adaptorChannel.adaptorContext().adaptor().expressionFactory().expressionForString(messageSQL));
					}
					// we need to do some error checking here so if it fails the second time through it does not continue to try creating the global variable.
				}				
			}
		}
		return result;
	}
}