package er.extensions.components;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.lang.CharEncoding;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.eof.ERXS;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Editor page for the localized files that are supplied in your settings. Note that the "save"
 * overwrites the files in the bundle, so be sure to have it pointed to the original file and not
 * the built bundle.
 * 
 * @author ak
 */
public class ERXLocalizationEditor extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public String currentLanguage;
	public String currentFilename;
	public String currentFramework;
	public NSMutableArray<NSMutableDictionary<String, Object>> data;
	public NSMutableDictionary currentEntry;
	public String selectedFramework;
	public String selectedFilename;
	public String UNSET = "***UNSET***";
	public WODisplayGroup displayGroup;
	public String keyToAdd;
	
    public ERXLocalizationEditor(WOContext context) {
        super(context);
        displayGroup = new WODisplayGroup();
        displayGroup.setSortOrderings(ERXS.ascInsensitives("key"));
        displayGroup.setNumberOfObjectsPerBatch(20);
        displayGroup.setDefaultStringMatchFormat("*%@*");
        displayGroup.setDefaultStringMatchOperator(EOQualifier.QualifierOperatorCaseInsensitiveLike.name());
    }

    @Override
    public void awake() {
    	super.awake();
    	keyToAdd = null;
    	if (displayGroup != null) {
    		displayGroup.setSelectedObject(null);
    	}
    }

    public NSArray<String> availableLanguages() {
    	return ERXLocalizer.availableLanguages();
    }

    public NSArray<String> frameworkSearchPath() {
    	return ERXLocalizer.frameworkSearchPath();
    }

    public NSArray<String> fileNamesToWatch() {
    	return ERXLocalizer.fileNamesToWatch();
    }

    public URL urlForCurrentFile() {
    	return ERXFileUtilities.pathURLForResourceNamed(currentFilename, currentFramework, new NSArray<String>(currentLanguage));
    }

    public URL urlForSelectedFile() {
    	return ERXFileUtilities.pathURLForResourceNamed(selectedFilename, selectedFramework, new NSArray<String>(currentLanguage));
    }

    public boolean fileExistsInLanguage() {
    	return urlForCurrentFile() != null;
    }

    public void editFramework() {
    	data = new NSMutableArray<NSMutableDictionary<String, Object>>();
    	NSMutableDictionary dataDictionary = new NSMutableDictionary();
       	selectedFilename = currentFilename;
       	selectedFramework = currentFramework;
    	NSMutableSet<String> allKeys = new NSMutableSet<String>();
    	for (String language : availableLanguages()) {
			NSArray<String> languageArray = new NSArray<String>(language);
			URL url = ERXFileUtilities.pathURLForResourceNamed(currentFilename, currentFramework, languageArray);
			if (url != null) {
				NSDictionary<String, Object> dict = (NSDictionary<String, Object>)ERXFileUtilities.readPropertyListFromFileInFramework(currentFilename, currentFramework, languageArray);
				allKeys.addObjectsFromArray(dict.allKeys());
				for (String key : dict.allKeys()) {
					NSMutableDictionary<String, Object> entry = (NSMutableDictionary<String, Object>) dataDictionary.objectForKey(key);
					if(entry == null) {
						entry = new NSMutableDictionary<String, Object>();
						entry.setObjectForKey(key, "key");
						dataDictionary.setObjectForKey(entry, key);
						data.addObject(entry);
					}
					entry.setObjectForKey(dict.objectForKey(key), language);
				}
			}
		}
    	for (String key : allKeys) {
			NSMutableDictionary<String, Object> entry = (NSMutableDictionary<String, Object>) dataDictionary.objectForKey(key);
			for (String language : availableLanguages()) {
				if (entry.objectForKey(language) == null) {
					entry.setObjectForKey(UNSET, language);
				}
			}
		}
    	displayGroup.setObjectArray(data);
    }

    public boolean isLargeEntry() {
    	String language = currentLanguage;
    	Object object = currentEntry.objectForKey(language);
		if (object != null && (object.toString().length() > 25
    			|| object.toString().indexOf('\n') >= 0 || !(object instanceof String))) {
    		
    		return true;
    	}
    	return false;
    }

    /**
     * Returns a colored border style for unset values
     * @return CSS class name
     */
    public String highlightClass() {
    	if (!hasCurrentValue()) {
    		return "unset";
    	}
    	return "inputfield";
    }

    /**
     * Returns a width for the current columns
     * @return width string
     */
    public String valueCellWidth() {
    	int width = 100 / availableLanguages().count();
    	return "" + width + "%";
    }

    public int colspanForBatchNavigation() {
    	if (availableLanguages() != null && availableLanguages().count() > 1) {
    		return availableLanguages().count() - 1;
    	}
    	return 1;
    }

    public String valueComponentName() {
    	return isLargeEntry()? "WOText" : "WOTextField";
    }

    public void saveFramework() throws IOException {
    	for (String language : availableLanguages()) {
			NSArray<String> languageArray = new NSArray<String>(language);
			URL url = ERXFileUtilities.pathURLForResourceNamed(selectedFilename, selectedFramework, languageArray);
			NSMutableDictionary dict = new NSMutableDictionary();
     		for (Enumeration entries = data.objectEnumerator(); entries.hasMoreElements();) {
     			NSDictionary entry = (NSDictionary) entries.nextElement();
     			String key = (String) entry.objectForKey("key");
				Object value = entry.objectForKey(language);
				if (value != null && !value.equals(UNSET)) {
					dict.setObjectForKey(value, key);
				}
			}
     		String result = ERXStringUtilities.stringFromDictionary(dict);
     		NSDictionary newDict = (NSDictionary) NSPropertyListSerialization.propertyListFromString(result);
     		if (!newDict.equals(dict)) {
     			throw new IllegalStateException("Data wasn't equal when comparing before save");
     		} else if (url != null) {
     			ERXFileUtilities.stringToFile(result, new File(url.getFile()), ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizationEditor.endoding", CharEncoding.UTF_16BE));
     		}
    	}
    }

    private Object currentValueObject() {
    	Object result = null;
    	if (currentEntry != null) {
    		result = currentEntry.objectForKey(currentLanguage);
     	}
    	return result;
    }

    public boolean hasCurrentValue() {
    	return !UNSET.equals(currentValueObject());
    }

    public String currentValue() {
    	String result = null;
    	if(currentEntry != null) {
    		Object item = currentEntry.objectForKey(currentLanguage);
    		if (item instanceof String) {
				result = (String)item;
			} else {
	    		result = NSPropertyListSerialization.stringFromPropertyList(item);
			}
    	}
    	return result;
    }

    public void setCurrentValue(String value) {
    	if(currentEntry != null) {
    		Object item = currentEntry.objectForKey(currentLanguage);
    		Object newValue;
    		if (item instanceof String) {
    			newValue = value;
			} else {
				newValue = NSPropertyListSerialization.propertyListFromString(value);
			}
    		if (newValue == null) {
    			newValue = UNSET;
    		}
    		currentEntry.setObjectForKey(newValue, currentLanguage);
     	}
    }

    /**
     * Sorts the entries ascending with the selected language, brings empty entries to the first batches
     * 
     * @return current page
     */
    public WOComponent sortEntries() {
    	displayGroup.setSortOrderings(ERXS.ascs(currentLanguage));
    	displayGroup.qualifyDisplayGroup();
    	
    	return context().page();
    }

    /**
     * Add an entry to the array of objects
     * 
     * @return current page
     */
    public WOComponent addEntry() {
    	if (keyToAdd != null && data != null && displayGroup != null) {
	    	NSMutableDictionary entry = new NSMutableDictionary();
	    	
	    	entry.setObjectForKey(keyToAdd, "key");
	    	
	    	for (String language : availableLanguages()) {
	    		entry.setObjectForKey(UNSET, language);
	    	}
	    	
	    	data.addObject(entry);
	    	displayGroup.setObjectArray(data);
	    	displayGroup.qualifyDataSource();
	    	displayGroup.setSelectedObject(entry);
	    	displayGroup.displayBatchContainingSelectedObject();
    	}
    	return context().page();
    }

    /**
     * Removes the current entry from all languages
     * 
     * @return current page
     */
    public WOComponent removeEntry() {
    	if (currentEntry != null) {
    		data.removeObject(currentEntry);
    		displayGroup.setObjectArray(data);
    		displayGroup.qualifyDisplayGroup();
    	}
    	return context().page();
    }
}
