/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof.qualifiers;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEOAccessUtilities;

/** @deprecated use {@link ERXToManyQualifier} */
@Deprecated
public class ERXEOToManyQualifier extends ERXToManyQualifier implements Cloneable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

     public ERXEOToManyQualifier(EOEntity e,
                                String toManyKey,
                                NSArray elements) {
        super(toManyKey,elements);
    }

    /**
     * @deprecated use {@link er.extensions.eof.ERXEOAccessUtilities#primaryKeysForObjects(NSArray)}
     */
     @Deprecated
    public static NSArray primaryKeysForObjectsFromSameEntity(NSArray eos) {
        return ERXEOAccessUtilities.primaryKeysForObjects(eos);           
    }

    /**
     * @deprecated use {@link er.extensions.eof.ERXEOAccessUtilities#snapshotsForObjectsFromRelationshipNamed(NSArray, String)}
     */
     @Deprecated
    public static NSArray primaryKeysForObjectsFromSameEntity(String relKey, NSArray eos) {
        return ERXEOAccessUtilities.snapshotsForObjectsFromRelationshipNamed(eos,relKey);
    }
}