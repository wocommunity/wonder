//
// LeaveSummarySub.java: Class file for WO Component 'LeaveSummarySub'
// Project Vacation
//
// Created by mishra on Wed Feb 19 2003
//

import com.uw.shared.*;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class LeaveSummarySub extends VacationComponent {

    /** @TypeInfo Group */
    protected EOEnterpriseObject group;
    protected Person person;
    protected boolean disabled;

    public LeaveSummarySub(WOContext context) {
        super(context);
    }

    public Group selectedGroup()
    {
        if (person.group()!=null) return (Group) EOUtilities.localInstanceOfObject(session.defaultEditingContext(), person.group());
        return null;
    }
    
    public void setSelectedGroup(Group newSelectedGroup)
    {
        if (newSelectedGroup!=null)
            person.setGroup((Group) EOUtilities.localInstanceOfObject(person.editingContext(), newSelectedGroup));
        else person.setGroup(null);
    }
    

}
