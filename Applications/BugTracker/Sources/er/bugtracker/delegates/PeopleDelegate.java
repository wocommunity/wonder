package er.bugtracker.delegates;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.People;
import er.extensions.eof.ERXEOControlUtilities;

public class PeopleDelegate extends BranchDelegate {

    @Override
    protected NSArray defaultBranchChoices(D2WContext context) {
        NSArray result = super.defaultBranchChoices(context);
        People people = (People) object(context);
        if(ERXEOControlUtilities.eoEquals(people, People.clazz.currentUser(people.editingContext())) || people.editingContext().globalIDForObject(people).isTemporary()) {
            result = choiceByRemovingKeys(new NSArray("delete"), result);
            result = choiceByRemovingKeys(new NSArray("view"), result);
        }
        return result;
    }
}
