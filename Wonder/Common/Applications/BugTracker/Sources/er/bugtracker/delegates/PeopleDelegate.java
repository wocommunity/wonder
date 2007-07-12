package er.bugtracker.delegates;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.People;
import er.extensions.ERXEOControlUtilities;

public class PeopleDelegate extends BranchDelegate {

    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        People people = (People) object(context);
        if(ERXEOControlUtilities.eoEquals(people, People.clazz.currentUser(people.editingContext()))) {
            result = choiceByRemovingKeys(new NSArray("delete"), result);
            result = choiceByRemovingKeys(new NSArray("view"), result);
        }
        return result;
    }
}
