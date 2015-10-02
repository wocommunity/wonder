/*
 * Copyright (c) 2000 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_LICENSE_HEADER_START@
 * 
 * Portions Copyright (c) 2000 Apple Computer, Inc.  All Rights
 * Reserved.  This file contains Original Code and/or Modifications of
 * Original Code as defined in and that are subject to the Apple Public
 * Source License Version 1.1 (the "License").  You may not use this file
 * except in compliance with the License.  Please obtain a copy of the
 * License at http://www.apple.com/publicsource and read it before using
 * this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON- INFRINGEMENT.  Please see the
 * License for the specific language governing rights and limitations
 * under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */

//package com.webobjects.woextensions;

package er.extensions.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.eof.ERXConstant;

/**
 * (Back port from WO 5 WOExtensions)
 */

public class WOCollapsibleComponentContent extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private boolean _isVisible;
    private boolean _isVisibleSet;
    private String _openedImageFileName;
    private String _closedImageFileName;
    private String _submitActionName;
    private String  _framework;
    private boolean _isFrameworkSet;
    private int _anchor;

    private static final String _undefinedMarker="UNDEFINED";
    private static int _counter = 0;

    public WOCollapsibleComponentContent(WOContext aContext)  {
        super(aContext);
        _isVisibleSet = false;
        _submitActionName=_undefinedMarker;
        _isFrameworkSet = false;

        // just a hack to get a unique anchor in a thread safe manner.
        synchronized(_undefinedMarker) {
            _counter++;
            _anchor = _counter;
        }
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public boolean isVisible()  {
        if (!_isVisibleSet) {
            _isVisible = (valueForBinding("condition")!=null) ? true : false;

            Object _conditionObj = valueForBinding("condition");
            if (_conditionObj instanceof Boolean) {
                _isVisible = ((Boolean)_conditionObj).booleanValue();
            } else if (_conditionObj instanceof Number) {
                _isVisible = ((Number)_conditionObj).intValue() != 0;
            }
            _isVisibleSet = true;
        }
        return _isVisible;
    }

    public WOComponent toggleVisibilityClicked()  {
        _isVisible = isVisible() ? false : true;
        if (canSetValueForBinding("visibility")) {
            if (_isVisible) {
                setValueForBinding(ERXConstant.OneInteger, "visibility");
            } else {
                setValueForBinding(ERXConstant.ZeroInteger, "visibility");
            }
        }
        return null;
    }

    public String openedImageFileName()  {
        if (_openedImageFileName==null) {
            if (hasBinding("openedImageFileName")) {
                _openedImageFileName = (String)valueForBinding("openedImageFileName");
            } else {
                _openedImageFileName = "DownTriangle.gif";
            }
        }
        return _openedImageFileName;
    }

    public String closedImageFileName()  {
        if (_closedImageFileName==null) {
            if (hasBinding("closedImageFileName")) {
                _closedImageFileName = (String)valueForBinding("closedImageFileName");
            } else {
                _closedImageFileName = "RightTriangle.gif";
            }
        }
        return _closedImageFileName;
    }

    public String currentArrowImageName()  {
        String aCurrentArrowImageName = null;
        if (isVisible()) {
            aCurrentArrowImageName = openedImageFileName();
        } else {
            aCurrentArrowImageName = closedImageFileName();
        }
        return aCurrentArrowImageName;
    }

    public String label()  {
        String aLabel = null;
        if (isVisible()) {
            aLabel = (String)valueForBinding("openedLabel");
        } else {
            aLabel = (String)valueForBinding("closedLabel");
        }
        return aLabel;
    }

    public String helpString()  {
        String aHelpString = null;
        if (isVisible()) {
            aHelpString = (String)valueForBinding("Click to collapse");
        } else {
            aHelpString = (String)valueForBinding("Click to expand");
        }
        return aHelpString;
    }

    public String framework() {
        if (!_isFrameworkSet) {
            _isFrameworkSet = true;
            _framework = hasBinding("framework") ? (String)valueForBinding("framework") : "JavaWOExtensions";
            if ((_framework!=null) && _framework.equalsIgnoreCase("app"))
                _framework=null;
        }
        return _framework;
    }


    public String submitActionName() {
        if (_submitActionName==_undefinedMarker) {
            if (hasBinding("submitActionName"))
                _submitActionName=(String)valueForBinding("submitActionName");
            else
                _submitActionName=null;
        }
        return _submitActionName;
    }

    public boolean hasSubmitAction() {
        return (submitActionName()!=null);
    }

    public String anchor() {
        return "" + _anchor;
    }
}
