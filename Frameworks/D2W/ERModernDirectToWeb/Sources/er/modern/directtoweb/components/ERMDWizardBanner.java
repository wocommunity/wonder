package er.modern.directtoweb.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.ERD2WContainer;
import er.extensions.components.ERXStatelessComponent;

/**
 * WizardPage banner that displays as:
 * <p>
 * "1 Foo Step 1 of n"
 * 
 * @d2wKey currentStep
 * @d2wKey currentTab
 * @d2wKey tabSectionsContents
 * 
 * @author davidleber
 *
 */
public class ERMDWizardBanner extends ERXStatelessComponent {
	
	public static interface Keys {
		 public static final String currentStep = "currentStep";
		 public static final String currentTab = "currentTab";
		 public static final String tabSectionsContents = "tabSectionsContents";
	}
	
	private ERD2WContainer _currentTab;
	private NSArray _tabSectionsContents;
	
	public ERD2WContainer tabItem;
	public int index;
	
    public ERMDWizardBanner(WOContext context) {
        super(context);
    }
    
    /** Can be used to get this instance into KVC */
    public final WOComponent self() {
        return this;
    }
    
    @Override
    public void reset() {
    	super.reset();
    	_currentTab = null;
    	_tabSectionsContents = null;
    }

	public int currentStep() {
		return intValueForBinding(Keys.currentStep, 1);
	}

	public ERD2WContainer currentTab() {
		if (_currentTab == null) {
			_currentTab = (ERD2WContainer)objectValueForBinding(Keys.currentTab);
		}
		return _currentTab;
	}
	
	@SuppressWarnings("unchecked")
	public NSArray tabSectionsContents() {
		if (_tabSectionsContents == null) {
			_tabSectionsContents = (NSArray)objectValueForBinding(Keys.tabSectionsContents);
		}
		return _tabSectionsContents;
	}
	
}