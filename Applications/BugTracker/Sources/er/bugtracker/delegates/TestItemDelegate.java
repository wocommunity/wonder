package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Factory;
import er.bugtracker.TestItem;

public class TestItemDelegate extends BranchDelegate {

    protected NSArray defaultBranchChoices(D2WContext context) {
        return super.defaultBranchChoices(context);
    }

    public WOComponent open(WOComponent sender) {
        return Factory.bugTracker().createBugFromTestItem((TestItem) object(sender));        
    }


    public WOComponent close(WOComponent sender) {
        return Factory.bugTracker().createBugFromTestItem((TestItem) object(sender));        
    }


    public WOComponent fileBug(WOComponent sender) {
        return Factory.bugTracker().createBugFromTestItem((TestItem) object(sender));        
    }

}
