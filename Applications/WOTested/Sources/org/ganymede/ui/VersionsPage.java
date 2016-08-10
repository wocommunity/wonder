
package org.ganymede.ui;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSet;

import er.extensions.components.ERXComponent;

public class VersionsPage extends ERXComponent {

    public VersionsPage(WOContext context) {
        super(context);
    }

    public EOEnterpriseObject result1 = (results().size() > 0) ? results().get(0) : null;

    public EOEnterpriseObject result2 = (results().size() > 1) ? results().get(1) : null;

    public EOEnterpriseObject aResult;

    public EOEnterpriseObject env;

    public NSArray<EOEnterpriseObject> results;
    public NSArray<EOEnterpriseObject> results() {
        if (results == null) {
            results = EOUtilities.objectsForEntityNamed(session().defaultEditingContext(), "Result");
        }
        return results;
    }

    public NSArray<String> sames() {

        NSArray<EOEnterpriseObject> digests1 = (NSArray<EOEnterpriseObject>)result1.valueForKey("digests");
        NSArray<EOEnterpriseObject> digests2 = (NSArray<EOEnterpriseObject>)result2.valueForKey("digests");

        NSMutableDictionary<String,String> dict1 = new NSMutableDictionary<String,String>();
        NSMutableDictionary<String,String> dict2 = new NSMutableDictionary<String,String>();

        for (EOEnterpriseObject eo : digests1) { dict1.setObjectForKey(eo.valueForKey("digest").toString(), eo.valueForKey("rname").toString()); }
        for (EOEnterpriseObject eo : digests2) { dict2.setObjectForKey(eo.valueForKey("digest").toString(), eo.valueForKey("rname").toString()); }

        NSMutableArray<String> same = new NSMutableArray<String>();

        for (String key : dict1.allKeys()) {
            if (dict1.objectForKey(key) != null && dict2.objectForKey(key) != null && dict1.objectForKey(key).equals(dict2.objectForKey(key))) {
                same.add(key+" "+dict1.objectForKey(key));
            }
        }

        return same.immutableClone();
    }

    public String diff;

    public NSArray<String> diffs() {

        NSArray<EOEnterpriseObject> digests1 = (NSArray<EOEnterpriseObject>)result1.valueForKey("digests");
        NSArray<EOEnterpriseObject> digests2 = (NSArray<EOEnterpriseObject>)result2.valueForKey("digests");

        NSMutableDictionary<String,String> dict1 = new NSMutableDictionary<String,String>();
        NSMutableDictionary<String,String> dict2 = new NSMutableDictionary<String,String>();

        for (EOEnterpriseObject eo : digests1) { dict1.setObjectForKey(eo.valueForKey("digest").toString(), eo.valueForKey("rname").toString()); }
        for (EOEnterpriseObject eo : digests2) { dict2.setObjectForKey(eo.valueForKey("digest").toString(), eo.valueForKey("rname").toString()); }

        NSSet<String> set1 = new NSSet<String>((NSArray<String>)digests1.valueForKey("rname"));
        NSSet<String> set2 = new NSSet<String>((NSArray<String>)digests1.valueForKey("rname"));

        NSSet<String> addedTo1 = set1.setBySubtractingSet(set2);
        NSSet<String> addedTo2 = set2.setBySubtractingSet(set1);

        NSMutableArray<String> same = new NSMutableArray<String>();

        for (String str : addedTo1) {
            same.add(str+" "+dict1.objectForKey(str)+" ADDED TO ONE");
        }

        for (String str : addedTo2) {
            same.add(str+" "+dict2.objectForKey(str)+" ADDED TO TWO");
        }

        NSSet<String> both = set1.setByIntersectingSet(set2);

        for (String str : both) {

            String obj1 = dict1.objectForKey(str);
            String obj2 = dict2.objectForKey(str);

            if (! obj1.equals(obj2)) {
                same.add(str+" "+obj1+" != "+obj2);
            }
        }

        return same.immutableClone();
    }
}
