/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* NSArrayPropertyAccessor.java created by max on Fri 28-Sep-2001 */
package ognl.webobjects;

import ognl.DynamicSubscript;
import ognl.NoSuchPropertyException;
import ognl.OgnlException;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

public class NSArrayPropertyAccessor extends NSObjectPropertyAccessor {

    @Override
    public Object getProperty( Object target, Object name ) throws OgnlException {
        if ( name instanceof String )
            return super.getProperty(target, name);
        NSArray array = (NSArray)target;
        if ( name instanceof Number ) {
            return array.objectAtIndex(((Number)name).intValue());
        }
        if ( name instanceof DynamicSubscript ) {
            int len = array.count();
            switch (((DynamicSubscript)name).getFlag()) {
                case DynamicSubscript.FIRST:    return len > 0 ? array.objectAtIndex(0) : null;
                case DynamicSubscript.MID:      return len > 0 ? array.objectAtIndex(len/2) : null;
                case DynamicSubscript.LAST:     return len > 0 ? array.lastObject() : null;
                case DynamicSubscript.ALL:	return array.clone();
            }
        }
        throw new NoSuchPropertyException(target, name);
    }

    @Override
    public void setProperty(Object target, Object name, Object value) throws OgnlException {
        if (name instanceof String ) {
            super.setProperty(target, name, value);
            return;
        }
        NSMutableArray array = (NSMutableArray)target;
        if (name instanceof Number ) {
            array.replaceObjectAtIndex(value, ((Number)name).intValue());
            return;
        }
        if ( name instanceof DynamicSubscript ) {
            int len = array.count();
            switch ( ((DynamicSubscript)name).getFlag() )
            {
                case DynamicSubscript.FIRST:    if ( len > 0 )
                    array.replaceObjectAtIndex(value, 0); return;
                case DynamicSubscript.MID:      if ( len > 0 )
                    array.replaceObjectAtIndex(value, len/2); return;
                case DynamicSubscript.LAST:     if ( len > 0 )
                    array.replaceObjectAtIndex(value, len-1); return;
                case DynamicSubscript.ALL:
                    array.setArray( NSPropertyListSerialization.arrayForString( (String) value ) );
                    return;
            }
        }
        throw new NoSuchPropertyException( target, name );
    }
}
