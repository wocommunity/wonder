/*
 * WOCollapsibleComponentContent.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;

public class WOCollapsibleComponentContent extends WOComponent
{
    protected boolean _isVisible;
    protected boolean _isVisibleSet;
    protected String _openedImageFileName;
    protected String _closedImageFileName;
    protected String _submitActionName;
    protected String  _framework;
    protected boolean _isFrameworkSet;
    protected int _anchor;

    protected static String _undefinedMarker="UNDEFINED";
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
                setValueForBinding(new Integer(1), "visibility");
            } else {
                setValueForBinding(new Integer(0), "visibility");
            }
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
