/*
 * WOTabPanel.java
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
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;

/*

 An HTML based Tab Panel

 Bindings:

 - tabs: a list of objects representing the tabs
 - tabNameKey: a string containing a key to apply to tabs to get the title of the tab
 - selectedTab: contains the selected tab
 - submitActionName: if this binding is non null, tabs will contain a submit button instead of a regular hyperlink and the action
   pointed to by the binding will be called
 - bgcolor: color to use for the selected tab and the body of the panel
 - nonSelectedBgColor: color to use for the non-selected tabs


 */


public class WOTabPanel extends WOComponent
{
    protected static String  _undefinedMarker="UNDEFINED";

    public Object  currentTab;
    protected Object  _selectedTab;
    protected String _submitActionName;
    protected String _tabNameKey;
    protected NSArray _tabs;
    public String bgcolor;
    protected String _nonSelectedBgColor;

    public WOTabPanel(WOContext aContext)  {
        super(aContext);
        _selectedTab=null;
        currentTab=null;
        _tabs=null;
        _submitActionName=_undefinedMarker;
        _tabNameKey=null;
    }

    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public Object selectedTab()  {
        if (_selectedTab==null) {
            _selectedTab=valueForBinding("selectedTab");
            if (_selectedTab==null) {
                _selectedTab=((NSArray)valueForBinding("tabs")).objectAtIndex(0);
                setValueForBinding(_selectedTab, "selectedTab");
            }
        }
        return _selectedTab;
    }
    
    public String tabNameKey()  {
        if (_tabNameKey==null) {
            _tabNameKey=(String)_WOJExtensionsUtil.valueForBindingOrNull("tabNameKey",this);
            if (_tabNameKey==null) _tabNameKey="toString";
        }
        return _tabNameKey;
    }

    public String selectedTabName()  {
        return (String)NSKeyValueCodingAdditions.Utility.valueForKeyPath(selectedTab(), tabNameKey());
    }

    public String currentTabName()  {
        return (String)NSKeyValueCodingAdditions.Utility.valueForKeyPath(currentTab, tabNameKey());
    }

    public boolean isCellShaded()  {
        return !selectedTab().equals(currentTab);
    }

    public NSArray tabs()  {
        if (_tabs==null) {
            _tabs=(NSArray)_WOJExtensionsUtil.valueForBindingOrNull("tabs",this);
        }
        return _tabs;
    }
    
    public String nonSelectedBgColor() {
        if (null==_nonSelectedBgColor) {
            if (hasBinding("nonSelectedBgColor"))
                _nonSelectedBgColor=(String)_WOJExtensionsUtil.valueForBindingOrNull("nonSelectedBgColor",this);
            else
                _nonSelectedBgColor="#B5B5B5";
        }
        return _nonSelectedBgColor;
    }

    public String tabBgColor()  {
        if (isCellShaded())
            return nonSelectedBgColor();
        else {
            if (null==bgcolor) {
                if (hasBinding("bgcolor"))
                    bgcolor=(String)_WOJExtensionsUtil.valueForBindingOrNull("bgcolor",this);
                else
                    bgcolor="#E0E0E0";
            }
            return bgcolor;
        }
    }

    public void  switchTab()  {
        _selectedTab=currentTab;
        setValueForBinding(_selectedTab, "selectedTab");
    }

    public String submitActionName()  {
        if (_submitActionName==_undefinedMarker) {
            if (hasBinding("submitActionName"))
                _submitActionName=(String)_WOJExtensionsUtil.valueForBindingOrNull("submitActionName",this);
            else
                _submitActionName=null;
        }
        return _submitActionName;
    }

    public boolean hasSubmitAction()  {
        if (submitActionName()!=null) return true;
        return false;
    }

    public void switchSubmitTab()  {
        switchTab();
        if ((submitActionName()!=null) && !submitActionName().equals(""))
            performParentAction(submitActionName());
    }

    public int contentColSpan()  {
        return 2+tabs().count();
    }

    public int rowSpan()  {
        if (isCellShaded())
            return 1;
        else
            return 2;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        _tabs=null;
        currentTab=null;
        _selectedTab=null;
        _submitActionName=_undefinedMarker;
        _tabNameKey=null;
        super.appendToResponse(aResponse, aContext);
    }
}