/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.util.*;
import er.extensions.*;

/**
 * Button used to apply one modification to a bunch of objects.<br />
 * 
 * @binding d2wContext
 * @binding list
 */

public class ERDMassModifyButton extends WOComponent {

    public ERDMassModifyButton(WOContext context) { super(context); }

    public boolean isStateless() { return true; }
    public D2WContext d2wContext() { return (D2WContext)valueForBinding("d2wContext"); }

    private static class _MassModificatorConfirmationDelegate implements NextPageDelegate {
        public EOEnterpriseObject eo;
        public NSArray displayPropertyKeys;
        public NSArray list;
        public String entityName;
        public WOComponent nextPage;
        public WOComponent nextPage(WOComponent sender) {
            WOComponent result=nextPage;
            if (eo.editingContext()!=null) { // save was clicked
                ConfirmPageInterface confirmPage = (ConfirmPageInterface)D2W.factory().confirmPageForEntityNamed(entityName,
                                                                                                                 sender.session());
                _MassModificatorDelegate cb=new _MassModificatorDelegate();
                cb.eo=eo; cb.displayPropertyKeys=displayPropertyKeys; cb.nextPage=nextPage; cb.list=list;
                confirmPage.setConfirmDelegate(cb);
                confirmPage.setCancelDelegate(new ERDPageDelegate(nextPage));
                StringBuffer message=new StringBuffer("You are about to modify <b>"+list.count()+"</b> "+entityName+"(s) in the following manner:<br><br>");
                for (Enumeration e= displayPropertyKeys.objectEnumerator(); e.hasMoreElements();) {
                    String key=(String)e.nextElement();
                    Object value=eo.valueForKey(key);
                    if (value!=null && (!(value instanceof String) || ((String)value).length()>0)) { // for text areas which return ""
                        message.append(key);
                        message.append("<br>");
                    }
                }
                message.append("<br><br>Are you sure you want to proceed?");                
                confirmPage.setMessage(message.toString());    
                result=(WOComponent)confirmPage;
            }
            return result;
        }
    }

    private static class _MassModificatorDelegate extends _MassModificatorConfirmationDelegate {
        public WOComponent nextPage(WOComponent sender) {
            WOComponent result=nextPage;
            if (eo.editingContext()!=null) { // save was clicked
                EOEditingContext ec=ERXEC.newEditingContext();
                for (Enumeration ob=list.objectEnumerator(); ob.hasMoreElements();) {
                    EOEnterpriseObject eoItem=(EOEnterpriseObject)ob.nextElement();
                    EOEnterpriseObject localEOItem=EOUtilities.localInstanceOfObject(ec,eoItem);
                    System.out.println(localEOItem);
                    for (Enumeration e= displayPropertyKeys.objectEnumerator(); e.hasMoreElements();) {
                        String key=(String)e.nextElement();
                        Object value=eo.valueForKey(key);
                        if (value!=null) {
                            System.out.println(key+" --> "+value);
                            if (value instanceof EOEnterpriseObject) { // assume it is a rel
                                EOEnterpriseObject localValue=EOUtilities.localInstanceOfObject(ec,(EOEnterpriseObject)value);
                                localEOItem.addObjectToBothSidesOfRelationshipWithKey(localValue,key);
                            } else if (!(value instanceof String) || ((String)value).length()>0)
                                localEOItem.takeValueForKey(value,key);
                        }
                    }
                }
                try {
                    ec.saveChanges();
                } catch (NSValidation.ValidationException e) {
                    String errorMessage = " Could not save your changes: "+e.getMessage()+" ";
                    ErrorPageInterface epf=D2W.factory().errorPage(sender.session());
                    if(epf instanceof ERDErrorPageInterface) {
                    	((ERDErrorPageInterface)epf).setException(e);
                    }
                    epf.setMessage(errorMessage);
                    epf.setNextPage(nextPage);
                    result=(WOComponent)epf;
                }
            }
            return result;
        }
    }


    public WOComponent massModify() {
        EOClassDescription cd = EOClassDescription.classDescriptionForEntityName(d2wContext().entity().name());
        EOEditingContext localContext = ERXEC.newEditingContext(false); // we will never validate or save this one
        EOEnterpriseObject newEO = cd.createInstanceWithEditingContext(localContext, null);
        localContext.insertObject(newEO);
        EditPageInterface epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed((String)d2wContext().valueForKey("massModificationPageConfiguration"),
                                                                                         session());
        epi.setObject(newEO);
        _MassModificatorConfirmationDelegate cb=new _MassModificatorConfirmationDelegate();
        cb.eo=newEO;
        cb.nextPage=context().page();
        cb.entityName=d2wContext().entity().name();
        cb.list=(NSArray)valueForBinding("list");
        epi.setNextPageDelegate(cb);
        WOComponent result=(WOComponent)epi;
        D2WContext editContext=(D2WContext)result.valueForKey("d2wContext");
        editContext.takeValueForKey("massModify", "subTask");

        // we then wipe any default value that might have been put in the EO
        NSArray dpk=(NSArray)editContext.valueForKey("displayPropertyKeys");
        cb.displayPropertyKeys=dpk;
        for (Enumeration e=dpk.objectEnumerator(); e.hasMoreElements();) {
            String key=(String)e.nextElement();
            newEO.takeValueForKey(null,key); // we don't care much about back relationships..
        }
        return result;
    }
}
