//
// GroupSummary.java: Class file for WO Component 'GroupSummary'
// Project Vacation
//
// Created by mishra on Mon Nov 04 2002
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class GroupSummary extends VacationComponent {

    protected Group group;

    public GroupSummary(WOContext context) {
        super(context);
    }

    public GroupEditor editGroup()
    {
        GroupEditor nextPage = (GroupEditor)pageWithName("GroupEditor");
        nextPage.setGroup(group);
        return nextPage;
    }

    public WOComponent deleteGroup()
    {
        session.defaultEditingContext().deleteObject(group);
        try {
            session.defaultEditingContext().saveChanges();
            session.groups = null;
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public GroupEditor newGroup()
    {
        GroupEditor nextPage = (GroupEditor)pageWithName("GroupEditor");
        nextPage.setGroup(new Group());
        return nextPage;
    }

}
