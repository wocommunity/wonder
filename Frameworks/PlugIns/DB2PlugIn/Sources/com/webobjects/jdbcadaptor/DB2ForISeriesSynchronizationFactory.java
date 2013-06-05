package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DB2ForISeriesSynchronizationFactory extends
		DB2SynchronizationFactory {
	
	

	public DB2ForISeriesSynchronizationFactory(EOAdaptor adaptor) {
		super(adaptor);
	}
	
    @Override
    public NSArray statementsToRenameTableNamed(String tableName, String newName, NSDictionary options) {
    	return noopExpressions();
    }


    @Override
    public NSArray statementsToRenameColumnNamed(String columnName,
    		String tableName, String newName, NSDictionary nsdictionary) {
    	return noopExpressions();
    }
    
	protected NSArray noopExpressions() {
      return new NSArray(_expressionForString("--"));
    }

}
