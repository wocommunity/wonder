// Release.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

public class Release extends _Release {
    public Release() {
        super();
    }

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
        setIsOpen(true);
    }
    
    // Class methods go here
    
    public static class ReleaseClazz extends _ReleaseClazz {
        
        public Release defaultRelease(EOEditingContext ec) {
            EOQualifier qualifier = new EOKeyValueQualifier(Key.IS_OPEN, EOQualifier.QualifierOperatorEqual, Boolean.TRUE);
            NSArray sorting = new NSArray(new Object[]{
                    EOSortOrdering.sortOrderingWithKey(Key.NAME, EOSortOrdering.CompareDescending)
            });
            EOFetchSpecification fs = new EOFetchSpecification(entityName(), qualifier, sorting);
            return (Release) ec.objectsWithFetchSpecification(fs).lastObject();
        }
    }

    public NSArray openBugs() {
        return Bug.clazz.openBugsWithTargetRelease(editingContext(), this);
    }

    public NSArray openRequirements() {
        return Requirement.clazz.openBugsWithTargetRelease(editingContext(), this);
    }
    
    public static final ReleaseClazz clazz = new ReleaseClazz();
}
