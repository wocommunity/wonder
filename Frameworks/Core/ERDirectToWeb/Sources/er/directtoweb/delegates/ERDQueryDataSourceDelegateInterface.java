/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.
 */

package er.directtoweb.delegates;

import com.webobjects.eocontrol.EODataSource;

import er.directtoweb.pages.ERD2WQueryPage;

/**
 * A delegate interface that allows the implentation to customize the {@link er.directtoweb.pages.ERD2WQueryPage#queryDataSource()} 
 * that will be passed from the <code>sender</code> Query page to its List results page and invoked by it to 
 * display the query results.
 */
public interface ERDQueryDataSourceDelegateInterface {
    
    public EODataSource queryDataSource(ERD2WQueryPage sender);
    
}