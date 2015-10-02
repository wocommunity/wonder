/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages.templates;
import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WEditableListPage;

/**
 * List page for editing all items in the list.
 * @see ERD2WEditableListPage
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
 * @d2wKey formEncoding
 * @d2wKey emptyListComponentName
 * @d2wKey repetitionComponentName
 * @d2wKey backgroundColorForTable
 * @d2wKey displayNameForEntity
 * @d2wKey textColor
 * @d2wKey pageWrapperName
 * @d2wKey showBanner
 */
public class ERD2WEditableListTemplate extends ERD2WEditableListPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WEditableListTemplate(WOContext context) {super(context);}
}
