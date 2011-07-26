package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;

/**
 * Wizard page banner that displays as a ul of the possible steps with the curren step 
 * identified with the class of "CurrentStep".
 * 
 * @d2wKey currentStep
 * @d2wKey currentTab
 * @d2wKey tabSectionsContents
 * 
 * @author davidleber
 *
 */
public class ERMDWizardDetailedBanner extends ERMDWizardBanner {
	
    public ERMDWizardDetailedBanner(WOContext context) {
        super(context);
    }

    /**
     * CSS class for the current step list li
     */
	public String listItemClass() {
		String result = "";
		if (tabItem != null && tabItem.equals(currentTab())) {
			result = "CurrentStep";
		} 
		if (index == tabSectionsContents().count() - 1) {
			result = "Last " + result;
		}
		if (index == 0) {
			result = "First " + result;
		}
		return result.equals("") ? null : result;
	}
	
	/**
	 * Display number for the current setp list li
	 */
	public int currentStepNumber() {
		return index + 1;
	}

}
