/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.eocontrol.EODataSource;

/**
 * A delegate interface that allows the implementation to customize the {@link ERD2WQueryPage#queryDataSource()} 
 * that will be passed from the <code>sender</code> Query page to its List results page and whose 
 * <code>fetchObjects</code> method will be invoked to display the query results.
 */
public interface ERDQueryDataSourceDelegateInterface {
    
    public EODataSource queryDataSource(ERD2WQueryPage sender);
    
}