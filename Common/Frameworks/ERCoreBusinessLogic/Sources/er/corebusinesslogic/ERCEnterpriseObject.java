/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.*;
import er.extensions.*;

public abstract class ERCEnterpriseObject extends ERXGenericRecord {

    public String encryptedPrimaryKey() {
        String pk = ERXExtensions.primaryKeyForObject(this);
        return pk==null ? null : ERXCrypto.blowfishEncode(pk);
    }

    // This is used for objects that need different configurations based on the entities of some of their relationships.
    // An example is that a SpaceDisposalOffer needs different configurations based on the entity of the property relationship.
    public String uniqueEntityName() { return entityName(); }

    public String contentTeamEntityName(){return entityName(); }

    public String dummy(){  return " "; }
    public String notApplicable() { return "N/A"; }
}
