/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

/**
 * The guarded object interface is used as a way
 * to ensure that objects that are not supposed to
 * be deleted or updated don't accidently get deleted
 * or updated. Look at {@link ERXGenericRecord} for an
 * implementation. This interface is also used within
 * ERD2W applications to determine if the edit icon
 * or the trash can icon should be displayed.
 */
public interface ERXGuardedObjectInterface {

    /**
     * Should return if this object can be
     * deleted.
     * @return if it is safe to delete
     */
    public boolean canDelete();
    /**
     * Should return if this object can be
     * updated.
     * @return if it is safe to update
     */
    public boolean canUpdate();
}
