/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import java.util.*;
import er.extensions.*;

public class HomePage extends WOComponent {

    public HomePage(WOContext aContext) {
        super(aContext);
    }

    protected NSArray totalMyBugs;
    protected NSArray totalNewBugs;
    protected NSArray totalAllRequirements;
    protected NSArray totalAllMyRequirements;
    protected NSArray totalTestItems;

    protected NextPageDelegate _nextPageCallback;

    protected int totalBugs;
    protected int newBugs;
    protected int testItemsCount;
    protected int requirementsCount;
    protected int totalRequirements;
    protected int isEngineering;
    private String headForAll;
    private String headForNew;
    
    public WOComponent refresh() {
        EOEnterpriseObject userObject = (EOEnterpriseObject)session().valueForKey("user");
        if (userObject!=null) {
            People user = (People)userObject;
            
            totalMyBugs = user.openBugs();
            totalNewBugs = user.unreadBugs();
            totalAllRequirements = user.openRequirements();
            totalAllMyRequirements = user.allRequirements();
            totalTestItems = user.openTestItems();

            totalBugs = totalMyBugs.count();
            newBugs = totalNewBugs.count();
            totalRequirements = totalAllRequirements.count();
            testItemsCount= totalTestItems.count();
            requirementsCount = totalAllMyRequirements.count();
        }
        return null;
    }

    ERXLocalizer localizer() {
        return ((Session)session()).localizer();
    }
    String plurifiedString(String template, String entity, NSArray arr) {
        String localizedEntityName = localizer().localizedStringForKeyWithDefault(entity);
        return localizer().plurifiedStringWithTemplateForKey(template, localizedEntityName, arr.count(), session());
    }
    public String headForAll() {
        return plurifiedString("HomePage.ListForOpenBugs", "Bug", totalMyBugs);
    }
    public String headForNew() {
        return plurifiedString("HomePage.ListForUnreadBugs", "Bug", totalNewBugs);
    }
    public String headForTestItems() {
        return localizer().localizedTemplateStringForKeyWithObject("List @user.openTestItems.count@ open TestItem(s) assigned to you", session());
    }
    public String headForRequirements() {
        return localizer().localizedTemplateStringForKeyWithObject("List @user.openRequirements.count@ open Requirements(s) assigned to you", session());
    }
    public String headForTotalRequirements() {
        return localizer().localizedTemplateStringForKeyWithObject("List @user.allRequirements.count@ total Requirements(s) assigned to you", session());
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        if (totalMyBugs==null) refresh();
        super.appendToResponse(r,c);
    }
}
