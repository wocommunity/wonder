/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import com.webobjects.foundation.NSDictionary;


/**
 * Enterprise objects that need to generate their own primary keys
 * should implement this interface. This interface works in conjunction
 * with the {@link ERXDatabaseContextDelegate}.
 * <p>
 * Note that {@link ERXGenericRecord} implements a default implementation
 * of this interface.
 */
public interface ERXGeneratesPrimaryKeyInterface {

    /**
     * This method is called by the ERXDatabaseContextDelegate when
     * in the middle of a transaction. This is signaled by passing in
     * the boolean <code>true</code> into the method. If the object
     * returns <code>null</code> then a new primary key is generated.
     */
    public NSDictionary primaryKeyDictionary(boolean inTransaction);
}
