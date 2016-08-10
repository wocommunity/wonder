/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.interfaces;

/**
 * Used to identify tab edit pages, not requiring common superclass.
 */

public interface ERDTabEditPageInterface extends ERDEditPageInterface {

    public Integer tabNumber();
    public void setTabNumber(Integer newTabNumber);
}
