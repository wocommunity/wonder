/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.ERXConstant;

public class FileBugFromTestItem extends WOComponent {

    public FileBugFromTestItem(WOContext aContext) {
        super(aContext);
    }

    public TestItem object;
    public String key;
    public Object extraBindings;

    // FIXME: This could be much cleaner.
    public WOComponent fileBug() {
        EOEntity entity=EOModelGroup.defaultGroup().entityNamed("Bug");
        EOClassDescription aClassDesc=entity.classDescriptionForInstances();
        EOEditingContext peerContext=new EOEditingContext(object.editingContext().parentObjectStore());
        EOEnterpriseObject localObject=EOUtilities.localInstanceOfObject(peerContext,object);
        localObject.addObjectToBothSidesOfRelationshipWithKey(TestItemState.BUG,"state");
        EOEnterpriseObject aNewEO=(EOEnterpriseObject)aClassDesc.createInstanceWithEditingContext(peerContext, null);
        peerContext.insertObject(aNewEO);
        String entityName = object.entityName();
        localObject.addObjectToBothSidesOfRelationshipWithKey(aNewEO,"bugs");
        EOEnterpriseObject localUser=EOUtilities.localInstanceOfObject(aNewEO.editingContext(),
                                                                       ((Session)session()).getUser());
        aNewEO.addObjectToBothSidesOfRelationshipWithKey(localUser,"originator");
        aNewEO.addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject)localObject.valueForKey("component"),"component");
        String pKey=object.primaryKey();
        aNewEO.takeValueForKey("[From Test #"+pKey+"]","textDescription");
        //aNewEO.takeValueForKey("[From Test #"+pKey+"]","subject");
        
        EditPageInterface epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditNewBug",session());
        epi.setObject(aNewEO);
        epi.setNextPage(context().page());
        /*
        if(0)     {
            EOEditingContext peer = new EOEditingContext(object.editingContext().parentObjectStore());
            TestItem testItem = (TestItem)EOUtilities.localInstanceOfObject(peer,object);
            People user = (People)EOUtilities.localInstanceOfObject(peer,((Session)session()).getUser());
            Component component = valueForKey("component");

            Bug bug = new Bug();
            peer.insertObject(bug);
            testItem.setState(TestItem.BUG_STATE);

            bug.setTextDescription("[From Test #"testItem.primaryKey()+"]");
            bug.addtoBothSidesOfTestItems(testItem);
            bug.addtoBothSidesOfOriginator(user);
            bug.addtoBothSidesOfComponents(component);

            EditPageInterface epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("EditNewBug",session());
            epi.setObject(bug);
            epi.setNextPage(context().page());

            return (WOComponent)epi;
        }*/
        
        return (WOComponent)epi;        
    }
    
    
}
