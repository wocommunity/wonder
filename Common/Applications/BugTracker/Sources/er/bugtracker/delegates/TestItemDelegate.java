package er.bugtracker.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Bug;
import er.bugtracker.Component;
import er.bugtracker.People;
import er.bugtracker.Session;
import er.bugtracker.TestItem;
import er.bugtracker.TestItemState;
import er.extensions.ERXEC;

public class TestItemDelegate extends BranchDelegate {

    protected NSArray defaultBranchChoices(D2WContext context) {
        return super.defaultBranchChoices(context);
    }

    public WOComponent fileBug(WOComponent sender) {
        TestItem testItem = (TestItem) object(sender);
        Session session = session(sender);
        EditPageInterface epi = null;
        EOEditingContext peer = ERXEC.newEditingContext(testItem.editingContext().parentObjectStore());
        peer.lock();
        try {
            testItem = (TestItem) testItem.localInstanceIn(peer);
            People user = People.clazz.currentUser(peer);
            Component component = testItem.component();

            Bug bug = (Bug) Bug.clazz.createAndInsertObject(peer);
            testItem.setState(TestItemState.BUG);

            bug.setNewText("[From Test #" + testItem.primaryKey()+"]");
            bug.addToBothSidesOfTestItems(testItem);
            bug.addToBothSidesOfOriginator(user);
            bug.addToBothSidesOfComponent(component);

            epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("CreateBug",session);
            epi.setObject(bug);
            epi.setNextPage(sender.context().page());
        } finally {
            peer.unlock();
        }
         return (WOComponent)epi;        
    }

}
