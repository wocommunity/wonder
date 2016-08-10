//
// ERNEUTabInspectPage.java: Class file for WO Component 'ERNEUTabInspectPage'
// Project ERNeutralLook
//
// Created by travis on Sun Jun 23 2002
//

package er.neutral;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;

import er.directtoweb.ERD2WFactory;
import er.directtoweb.pages.ERD2WTabInspectPage;
import er.extensions.foundation.ERXValueUtilities;

public class ERNEUTabInspectPage extends ERD2WTabInspectPage {

    public static final String IMAGE_TAB_COMPONENT_NAME = "ERXImageTabPanel";
    public static final String TEXT_TAB_COMPONENT_NAME = "ERXTabPanel";

    public ERNEUTabInspectPage(WOContext context) {
        super(context);
    }

    @Override
    public String defaultRowspan () {
        return ""+(currentSection()!=null && currentSection().keys!=null ? currentSection().keys.count() : 0)+2;
    }


    @Override
    public WOComponent printerFriendlyVersion() {
        WOComponent result= ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }

    public String currentSectionImageName() {
        String name=currentSection().name;
        name=(NSArray.componentsSeparatedByString(name," ")).componentsJoinedByString("");
        return "/nsi/section"+name+".gif";
    }

    @Override
    public String saveButtonFileName() {
        return object()!=null && object().editingContext()!=null ?
        object().editingContext().parentObjectStore() instanceof EOObjectStoreCoordinator ? "/nsi/buttonSave.gif" : "/nsi/buttonOK.gif" :
        "/nsi/buttonSave.gif";
    }

    public boolean shouldShowReturnButton() {
	Integer i=(Integer)d2wContext().valueForKey("shouldShowReturnButton");
	return i != null && i.intValue()==1;
    }

    public String cancelButtonFileName() { return shouldShowReturnButton() ? "/nsi/buttonReturn.gif" : "/nsi/buttonCancel.gif"; }

    @Override
    public boolean useTabImages() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("useTabImages")); }
    @Override
    public boolean useTabSectionImages() { return ERXValueUtilities.booleanValue(d2wContext().valueForKey("useTabSectionImages")); }
    
    @Override
    public String tabComponentName() {
	return useTabImages() ? IMAGE_TAB_COMPONENT_NAME : TEXT_TAB_COMPONENT_NAME;
    }

}
