/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;
import com.webobjects.appserver.WOContext;

/**
 * List page for editing all items in the list.<br />
 * See {@link ERD2WEditableListPage}
 * 
 * @binding backAction
 * @binding dataSource
 * @binding defaultSortKey
 * @binding isEntityInspectable
 * @binding isEntityReadOnly
 * @binding isListEmpty
 * @binding isSelecting
 * @binding listSize
 * @binding nextPage
 * @binding object
 * @binding showCancel
 */

public class ERD2WEditableListTemplate extends ERD2WEditableListPage {

    public ERD2WEditableListTemplate(WOContext context) {super(context);}
}
