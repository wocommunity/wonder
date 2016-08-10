package webobjectsexamples.businesslogic.movies.common;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.corebusinesslogic.ERCoreUserInterface;

public class AppUser extends _AppUser implements ERCoreUserInterface {

    private static final long serialVersionUID = 1L;

    /*
     * ERCore user interface implementation
     */

    @Override
    public void newPreference(EOEnterpriseObject pref) {
        addObjectToBothSidesOfRelationshipWithKey(pref, "preferences");
    }

    @Override
    public void setPreferences(NSArray array) {
        takeStoredValueForKey(array.mutableClone(), "preferences");
    }

    @Override
    public NSArray preferences() {
        return (NSArray) storedValueForKey("preferences");
    }

}
