//
// GroupEditor.java: Class file for WO Component 'GroupEditor'
// Project Vacation
//
// Created by mishra on Mon Nov 04 2002
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class GroupEditor extends VacationComponent {

    protected EOEditingContext localContext = new EOEditingContext();

    protected Group groupIterator;
    protected Group group;

    public GroupEditor(WOContext context) {
        super(context);
    }

    public void setGroup(Group newGroup) {
        
        if (newGroup.editingContext()==null) {
            group=newGroup;
            localContext.insertObject(group);
        }
        else group = (Group) EOUtilities.localInstanceOfObject(localContext, newGroup);

        // remove the current group from the list of groups chooseable, to partially avoid recursion
        NSMutableArray tempGroups = new NSMutableArray(session.groups);
        tempGroups.removeObject(group);

        session.groups = tempGroups;
    }

    public GroupSummary saveChanges()
    {
        try {
            localContext.saveChanges();
            session.groups=null;
        }
        catch (Exception e) {
            System.out.println(e);
        }
        
        GroupSummary nextPage = (GroupSummary)pageWithName("GroupSummary");
        return nextPage;
    }

    public Group selectedGroup()
    {
       if (group.parentGroup()!=null) return (Group) EOUtilities.localInstanceOfObject(session.defaultEditingContext(), group.parentGroup());
        return null;
    }

    public void setSelectedGroup(Group newSelectedGroup)
    {
        if (newSelectedGroup!=null) group.setParentGroup((Group) EOUtilities.localInstanceOfObject(group.editingContext(), newSelectedGroup));
        else group.setParentGroup(null);
    }

}
