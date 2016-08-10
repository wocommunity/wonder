/*
 * WOCollapsibleComponentContent.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WOCollapsibleComponentContent extends WOComponent
{
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected boolean _isVisible;
    protected boolean _isVisibleSet;
    protected String _openedImageFileName;
    protected String _closedImageFileName;
    protected String _submitActionName;
    protected String  _framework;
    protected boolean _isFrameworkSet;
    protected int _anchor;

    protected static final String _undefinedMarker="UNDEFINED";
    protected static int _counter = 0;
    
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
                setValueForBinding(Integer.valueOf(1), "visibility");
            } else {
                setValueForBinding(Integer.valueOf(0), "visibility");
            }
        }
        
        if (hasSubmitAction()) {
            performParentAction(submitActionName());
        }
        return null;
    }

    public String openedImageFileName()  {
        if (_openedImageFileName==null) {
            if (hasBinding("openedImageFileName")) {
                _openedImageFileName = (String) _WOJExtensionsUtil.valueForBindingOrNull("openedImageFileName",this);
            } else {
                _openedImageFileName = "DownTriangle.gif";
            }
        }
        return _openedImageFileName;
    }

    public String closedImageFileName()  {
        if (_closedImageFileName==null) {
            if (hasBinding("closedImageFileName")) {
                _closedImageFileName = (String) _WOJExtensionsUtil.valueForBindingOrNull("closedImageFileName",this);
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
            aLabel = (String)_WOJExtensionsUtil.valueForBindingOrNull("openedLabel",this);
        } else {
            aLabel = (String)_WOJExtensionsUtil.valueForBindingOrNull("closedLabel",this);
        }

        return aLabel;
    }

    public String helpString()  {
        String aHelpString = null;
        if (isVisible()) {
            aHelpString = (String)_WOJExtensionsUtil.valueForBindingOrNull("Click to collapse",this);
        } else {
            aHelpString = (String)_WOJExtensionsUtil.valueForBindingOrNull("Click to expand",this);
        }
        return aHelpString;
    }

    public String framework() {
        if (!_isFrameworkSet) {
            _isFrameworkSet = true;
            _framework = hasBinding("framework") ? (String) _WOJExtensionsUtil.valueForBindingOrNull("framework",this) : "JavaWOExtensions";
            if ((_framework!=null) && _framework.equalsIgnoreCase("app"))
                _framework=null;
        }
        return _framework;
    }


    public String submitActionName() {
        if (_submitActionName==_undefinedMarker) {
            if (hasBinding("submitActionName")) {
                Object value = valueForBinding("submitActionName");
                // if the value of the binding in the wod file is 'null' the association treats it as
                // a Boolean whose string value is "false". We need to check for this
                if (value instanceof String) {
                    _submitActionName= value.toString();
                } else {
                    _submitActionName=null;
                }
            }
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
