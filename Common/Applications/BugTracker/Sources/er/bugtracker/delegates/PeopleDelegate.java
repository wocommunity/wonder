package er.bugtracker.delegates;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

public class PeopleDelegate extends BranchDelegate {

    protected NSArray defaultBranchChoices(D2WContext context) {
        return super.defaultBranchChoices(context);
    }
}
