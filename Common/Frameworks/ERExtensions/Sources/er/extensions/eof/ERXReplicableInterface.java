/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * To be implemented by objects which need to be copied into new objects
 * the replicated relationships need to point towards replicable objects
 */
public interface ERXReplicableInterface {

    /**
     * returns a new object with the same attributes and replicated replationships
     */
    public EOEnterpriseObject replicate(EOEditingContext ec);
    public void deplicate();

}

