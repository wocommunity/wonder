//
// ERWizardCreationPage.java: Class file for WO Component 'ERWizardCreationPage'
// Project ERNeutralLook
//
// Created by travis on Mon Jul 01 2002
//

package er.neutral;

import com.webobjects.appserver.WOContext;

import er.directtoweb.ERD2WWizardCreationPage;

public class ERNEUWizardCreationPage extends ERD2WWizardCreationPage {

    public ERNEUWizardCreationPage(WOContext context) {
        super(context);
    }
    
    public String currentSectionImageName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String defaultRowspan() {
	return ""+(currentSection()!=null && currentSection().keys!=null ? currentSection().keys.count() : 0)+4;
	//return ""+(currentSection().keys.count()+4);
    }

}
