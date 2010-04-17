/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.Services;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.eof.ERXConstant;

public class TextForEdit extends TextForNew {

    public TextForEdit(WOContext context) {
        super(context);
    }
    
    public String entry;

    public boolean last() { return index!=null &&
        formattedEntries()!=null && formattedEntries().count()>1 && index.intValue()==formattedEntries().count()-1 ;}
    public boolean notFirstnotLast() { return index!=null && index.intValue()!=0 && !last();}
    
    
    public static String breakDownEntries(String inputString) {
        // this result would be better achieved by making a better eomodel, but in the meantime..
        if (inputString==null) return null;
        int totalLength=inputString.length();
        StringBuffer result=new StringBuffer();
        int position=0;
        int nextOn=0;
        boolean finished=false;
        while (position<totalLength && nextOn!=-1 && !finished) {
            nextOn=inputString.indexOf("On ",position);
            if (nextOn!=-1) {
                int nextWrote=inputString.indexOf("wrote:",nextOn);
                if (nextWrote!=-1) {
                    int nextReturn=inputString.indexOf('\n',nextWrote);
                    if (nextReturn==nextWrote+6) {
                        result.append(inputString.substring(position, nextOn));
                        result.append("<hr>");
                        result.append(inputString.substring(nextOn, nextReturn));
                        position=nextReturn;
                    } else
                        finished=true;
                } else
                    finished=true;
            }
        }
        result.append(inputString.substring(position, totalLength));
        result.append("<hr>");
        return result.toString();
    }
    public static NSArray breakDownEntriesAsArray(String inputString) {
        // this result would be better achieved by making a better eomodel, but in the meantime..
        if (inputString==null) return null;
        NSMutableArray resultingArray=new NSMutableArray();
        int totalLength=inputString.length();
        StringBuffer result=new StringBuffer();
        int position=0;
        int nextOn=0;
        boolean finished=false;
        while (position<totalLength && nextOn!=-1 && !finished) {
            nextOn=inputString.indexOf(position!=0 ? "\nOn " : "On ",position);
            if (nextOn!=-1) {
                int nextWrote=inputString.indexOf("wrote:",nextOn);
                if (nextWrote!=-1) {
                    int nextReturn=inputString.indexOf('\n',nextWrote);
                    if (nextReturn==nextWrote+6) {
                        if (position!=nextOn) {
                            result.append(inputString.substring(position, nextOn));
                            resultingArray.addObject(result.toString());
                        }
                        result=new StringBuffer();
                        result.append(inputString.substring(nextOn, nextReturn));
                        position=nextReturn;
                    } else
                        finished=true;
                } else
                    finished=true;
            }
        }
        result.append(inputString.substring(position, totalLength));
        resultingArray.addObject(result.toString());
        return resultingArray;
    }

    // prevent the same text to be appended multiple times
    private String _textAlreadyAdded=null;
    public void setNewText(String newValue) {
        if (_textAlreadyAdded==null || newValue!=null && !newValue.equals(_textAlreadyAdded)) {
                super.setNewText(newValue);
                // if someone is adding comments, mark the bug unread!
                EOEnterpriseObject bugOwner=(EOEnterpriseObject)object.valueForKey("owner");
                EOEnterpriseObject sessionOwner=((Session)session()).getUser();
                if (bugOwner!=sessionOwner) object.takeValueForKey(ERXConstant.ZeroInteger, "read");
                _textAlreadyAdded=newValue;
            }       
    }
    

    public String formattedDescription() {
        //String unformatted=object.valueForKey("textDescription");
        return breakDownEntries(Services.breakDown((String)object.valueForKey("textDescription"), 75));
    }

    private NSArray _formattedEntries;
    public NSArray formattedEntries() {
        if (_formattedEntries==null) {
            _formattedEntries=breakDownEntriesAsArray(Services.breakDown((String)object.valueForKey("textDescription"), 75));
        }
        return _formattedEntries;
    }
}
